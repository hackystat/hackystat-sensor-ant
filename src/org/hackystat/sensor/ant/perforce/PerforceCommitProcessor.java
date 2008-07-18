package org.hackystat.sensor.ant.perforce;

import java.util.Date;
import java.util.Vector;

import com.perforce.api.Change;
import com.perforce.api.Client;
import com.perforce.api.Debug;
import com.perforce.api.Env;
import com.perforce.api.FileEntry;
import com.perforce.api.P4Process;
import com.perforce.api.Utils;

/**
 * Provides the interface to Perforce for the sensor. This code accomplishes the following:
 * <ul>
 * <li> Defines a new Client with a view that maps to the Perforce depot directory of interest.  
 * <li> Obtains the set of ChangeLists for a specified date interval.
 * <li> Finds the set of files committed in each change list. 
 * <li> Performs a diff on each file to get a count of lines added, modified, and changed. 
 * </ul>
 *
 * @author Philip Johnson
 * 
 */
public class PerforceCommitProcessor {
  // START CONFIGURATION SECTION
  // Edit the following String variables to conform to your local environment.
  /** The path to the p4 executable. */
  private String p4Executable = "C:\\Program Files\\Perforce\\P4.EXE";
  /** The hostname and port for the perforce server. */
  private String p4Port = "public.perforce.com:1666";
  /** The authorized Perforce user for this server. */
  private String p4User = "philip_johnson";
  /** The password for this user on this server. */
  private String p4Password = "clearsnow";
  /** If running on windows, need this for p4. */
  private String p4SysRoot = "C:\\WINDOWS";
  /** If running on windows, need this for p4. */
  private String p4SysDrive = "C:";
  /** The depot file specification for the files we want to analyze for this sensor. */
  private String depotPath = "//guest/philip_johnson/...";
  /** The start date for getting Commit data. */
  private String startDate = "2008/07/14";
  /** The end date for getting Commit data. */
  private String endDate = "2008/07/16";
  // END CONFIGURATION SECTION

  /** The Env instance initialized in setupEnvironment. */
  private Env env;
  /** The Client workspace that will be created based upon the depotPath. */
  private Client client;
  /** The set of Change instances we will retrieve from the server. */
  Change[] changes;

  /**
   * Setting up the Perforce environment involves two tasks: (1) Create an Env instance that holds
   * the various properties required by the P4 Java API. (2) Create a Client instance that maps the
   * depotPath to a local workspace. Note that we won't actually populate that local workspace with
   * any files, but we need to create one in order to do any manipulations in Perforce.
   * @throws Exception if problems occur. 
   */
  private void setupEnvironment() throws Exception {
    // First, initialize the Env instance with the configuration data.
    this.env = new Env();
    this.env.setExecutable(p4Executable);
    this.env.setPort(p4Port);
    this.env.setUser(p4User);
    this.env.setPassword(p4Password);
    this.env.setSystemRoot(p4SysRoot);
    this.env.setSystemDrive(p4SysDrive);
    Debug.setDebugLevel(Debug.VERBOSE); // Seems like a good idea.
    // Uncomment the following to get lots of output if the p4 command is failing. 
    //Debug.setLogLevel(Debug.LOG_SPLIT);

    // Next, create a unique Client that maps the depotPath. We will delete this Client during
    // cleanup. By creating a unique Client, we support concurrent invocation of this sensor
    // by the same client with different depotPaths. 
    String hackystatSensorClientName = "hackystat-sensor-" + (new Date()).getTime();
    this.client = new Client(env, hackystatSensorClientName);
    this.client.setRoot(System.getProperty("user.home") + "/perforcesensorsketch");
    this.client.addView(depotPath, "//" + hackystatSensorClientName + "/...");
    this.client.commit();
    // Now that we've defined a client, add it to our environment.
    this.env.setClient(hackystatSensorClientName);
  }

  /**
   * Gets the set of ChangeLists for the specified depotPath between start and end date.
   * @throws Exception If problems occur. 
   */
  private void getChangeLists() throws Exception {
    this.changes = Change.getChanges(env, depotPath, 100, startDate, endDate, true, null);
  }

  /**
   * Finds out the lines added, deleted, and changed for the passed file.
   * 
   * @param entry The FileEntry from the change list.
   * @return A three-tuple containing the lines added, deleted, and modified for this file.
   * @throws Exception If problems occur.   
   */
  private Integer[] getFileChangeInfo(FileEntry entry) throws Exception {
    entry.sync();
    String depotPath = entry.getDepotPath();
    boolean isTextFile = "text".equals(entry.getHeadType());
    Integer[] ints = {0, 0, 0};
    if (!isTextFile) {
      return ints;
    }
    int revision = entry.getHeadRev();
    System.out.printf("Diffing file: %s%n", depotPath);
    String difference = runDiff2Command(depotPath, revision);
    System.out.println(difference);
    ints = processDiff2Output(difference);
    System.out.printf("Lines added/deleted/changed: %d %d %d %n", ints[0], ints[1], ints[2]);
    return ints;
  }

  /**
   * Invokes the p4 diff2 command to obtain summary information about the differences between
   * the two files.  Returns the output of the command.
   * @param file The file to be diffed.
   * @param revision The original revision number. Will be diffed against the prior revision number.
   * @return The output from running the command. 
   * @throws Exception If problems occur.
   */
  private String runDiff2Command(String file, int revision) throws Exception {
    String l;
    StringBuffer sb = new StringBuffer();
    int priorRevision = revision - 1;
    String[] cmd = { "p4", "diff2", "-ds", file + "#" + revision, file + "#" + priorRevision };
    P4Process p = new P4Process(this.env);
    p.exec(cmd);
    while (null != (l = p.readLine())) {
      sb.append(l);
      sb.append('\n');
    }
    p.close();
    return sb.toString();
  }

  /**
   * Calls the p4 program to delete the specified client instance. 
   * (This should really be part of the official Perforce Java API.)
   * @param client The client to be deleted. 
   * @return The string returned by perforce. 
   * @throws Exception if problems occur. 
   */
  private String runDeleteClientCommand(Client client) throws Exception {
    String l;
    StringBuffer sb = new StringBuffer();
    String[] cmd = { "p4", "client", "-d", client.getName() };
    P4Process p = new P4Process(this.env);
    p.exec(cmd);
    while (null != (l = p.readLine())) {
      sb.append(l);
      sb.append('\n');
    }
    p.close();
    return sb.toString();
  }
  
  
  
  /**
   * Takes the diff2 command output, and parses it to produce an array of three integers: the
   * lines added, the lines deleted, and the lines changed. 
   * @param output The diff2 command output
   * @return An array of three integers containing added, deleted, and changed lines. 
   * @throws Exception If problems occur. 
   */
  private Integer[] processDiff2Output(String output) throws Exception {
    Integer[] ints = { 0, 0, 0 };
    String[] lines = output.split("\\n");
    for (String line : lines) {
      String[] tokens = line.split("\\s");
      String changeType = tokens[0];
      if ("add".equals(changeType)) {
        ints[0] += Integer.valueOf(tokens[3]);
      }
      if ("deleted".equals(changeType)) {
        ints[1] += Integer.valueOf(tokens[3]);
      }
      if ("changed".equals(changeType)) {
        ints[2] += Integer.valueOf(tokens[3]);
      }
    }
    return ints;
  }
  
  /**
   * This method should be invoked at the end of the sensor run, and will delete the client
   * created for this task as well as invoke the Perforce library cleanUp() method.
   * @throws Exception If problems occur. 
   */
  private void cleanup() throws Exception {
    this.runDeleteClientCommand(this.client);
    Utils.cleanUp();
  }

  /**
   * Exercises the methods in this class. 
   * @param args Ignored. 
   * @throws Exception If problems occur. 
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    System.out.printf("Starting PerforceSensorSketch%n  Setting up environment...");
    PerforceCommitProcessor sketch = new PerforceCommitProcessor();
    sketch.setupEnvironment();
    System.out.printf("done.%n  Now retrieving change lists...");
    sketch.getChangeLists();
    System.out.printf("found %d.%n  Now iterating through files...%n", sketch.changes.length);

    for (Change changelist : sketch.changes) {
      changelist.sync();
      Vector<FileEntry> files = changelist.getFileEntries();
      for (FileEntry fileEntry : files) {
        sketch.getFileChangeInfo(fileEntry);
      }
    }
    sketch.cleanup();
    System.out.printf("Finished PerforceSensorSketch.");
  }
}
