package org.hackystat.sensor.ant.perforce;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.perforce.api.Change;
import com.perforce.api.Client;
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
 * Note that you must create a P4Environment instance and initialize it properly before 
 * invoking the PerforceCommitProcessor.  
 * 
 * See the main() method for example usage of this class. 
 * 
 * @author Philip Johnson
 */
public class PerforceCommitProcessor {

  /** The Perforce depotPath associated with this processor. */
  private String depotPath;
  /** The Perforce Java API Env instance initialized in setupEnvironment. */
  private Env env;
  /** The Client workspace that will be created based upon the depotPath. */
  private Client client;
  /** The set of changelists and associated information we will build in this processor. */  
  private List<PerforceChangeListData> changeListDataList = new ArrayList<PerforceChangeListData>();
  
  /** Controls whether the -dw option is passed to diff2. */
  private boolean ignoreWhitespace = false;
  
  /** Disable the default public no-arg constructor. */
  @SuppressWarnings("unused")
  private PerforceCommitProcessor () {
    // Disable default constructor.
  }
  
  /**
   * Instantiates a PerforceCommitProcessor with the passed P4Environment instance and depotPath. 
   * @param p4Environment The p4Environment. 
   * @param depotPath The depot path this processor will work on. 
   * @throws Exception If problems occur instantiating this environment. 
   */
  public PerforceCommitProcessor(P4Environment p4Environment, String depotPath) throws Exception {
    this.env = p4Environment.getEnv();
    this.depotPath = depotPath;
    this.client = createClient(env, depotPath);
    this.env.setClient(this.client.getName());
  }
  

  /**
   * Creates a Perforce Client that maps the depotPath to a local workspace.
   * This Client has a unique name which enables concurrent execution of this sensor. 
   * It will be deleted during cleanup.
   * @param env The Env instance to be used to create this client.
   * @param depotPath The depotPath this Client will map.   
   * @return The newly created Client.  
   * @throws Exception if problems occur. 
   */
  private Client createClient(Env env, String depotPath) throws Exception {
    String hackystatSensorClientName = "hackystat-sensor-" + (new Date()).getTime();
    Client client = new Client(env, hackystatSensorClientName);
    client.setRoot(System.getProperty("user.home") + "/perforcesensorsketch");
    client.addView(depotPath, "//" + hackystatSensorClientName + "/...");
    client.commit();
    return client;
  }

  
  /**
   * Processes any ChangeLists that were submitted between startData and endDate to the depotPath.
   * @param startDate The start date, in YYYY/MM/DD format. 
   * @param endDate The end date, in YYYY/MM/DD format. 
   * @throws Exception If problems occur. 
   */
  @SuppressWarnings("unchecked") // Vector in Perforce Java API is not generic.
  public void processChangeLists(String startDate, String endDate) throws Exception {
    int maximumChanges = 1000;
    boolean useIntegrations = true;
    String startTime = startDate + " 00:00:00";
    String endTime = endDate + " 23:59:59";
    Change[] changes = Change.getChanges(env, depotPath, maximumChanges, startTime, endTime,
        useIntegrations, null);
    for (Change changelist : changes) {
      String owner = changelist.getUser().getId();
      PerforceChangeListData changeListData = new PerforceChangeListData(owner, changelist
          .getNumber(), changelist.getModtimeString());
      changelist.sync();
      Vector<FileEntry> files = changelist.getFileEntries();
      for (FileEntry fileEntry : files) {
        //fileEntry.sync(); // not sure if this is needed. Maybe changelist.sync() is good enough.
        
        // Changelists can contain files not in the user-specified depotPath, so only process 
        // files in the changelist that match the depotPath. 
        if (Utils.wildPathMatch(this.depotPath, fileEntry.getDepotPath())) {
          // Set up defaults for size info for binary files.
          Integer[] lineInfo = { 0, 0, 0 };
          int totalLoc = 0;
          // Calculate real values for text files.
          if (isTextFile(fileEntry.getDepotPath(), changelist.getNumber())) {
            lineInfo = getFileChangeInfo(fileEntry);
            totalLoc = getFileSize(changelist.getNumber(), fileEntry.getDepotPath());
          }
          changeListData.addFileData(fileEntry.getDepotPath(), lineInfo[0], lineInfo[1],
              lineInfo[2], totalLoc);
        }
      }
      this.changeListDataList.add(changeListData);
    }
  }
  
  /**
   * Retrieve the list of PerforceChangeListData instances associated with this instance. 
   * @return The list of PerforceChangeListData instances.  
   */
  public List<PerforceChangeListData> getChangeListDataList() {
    return this.changeListDataList;
  }
  
  /**
   * Controls whether the diff2 command will be passed the -dw option so that whitespace changes
   * in the file are ignored. 
   * @param ignoreWhitespace True if whitespace changes in the file should be ignored. 
   */
  public void setIgnoreWhitespace(boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
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
    Integer[] ints = {0, 0, 0};
    int revision = entry.getHeadRev();
    String difference = runDiff2Command(depotPath, revision);
    ints = processDiff2Output(difference);
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
    int priorRevision = revision - 1;
    List<String> cmd = new ArrayList<String>();
    cmd.add("p4");
    cmd.add("diff2");
    cmd.add("-ds");
    if (this.ignoreWhitespace) {
      cmd.add("-dw");
    }
    cmd.add(file + "#" + priorRevision);
    cmd.add(file + "#" + revision);
    String[] args = cmd.toArray(new String[cmd.size()]); 
    return runP4Command(args);
  }
  
  
  /**
   * Invokes the p4 program with the specified arguments, and returns the output as a string.
   * @param cmd The command to be invoked. 
   * @return The output from the P4 program. 
   * @throws Exception If problems occur. 
   */
  private String runP4Command (String[] cmd) throws Exception {
    String l;
    StringBuffer sb = new StringBuffer();
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
    String[] cmd = { "p4", "client", "-d", client.getName() };
    return runP4Command(cmd);
  }
  
  /**
   * Calls the p4 program to get file size in LOC for the specified file. 
   * (This should really be part of the official Perforce Java API.)
   * (We also shouldn't have to retrieve the whole darn file just to count the number of lines.)
   * Only call this if the file is text.
   * @param changelist The changelist revision we want file size for. 
   * @param file The file we want stats for. Should be in the form of a depot path.  
   * @return The number of lines in this file. 
   * @throws Exception if problems occur. 
   */
  private int getFileSize(int changelist, String file) throws Exception {
    String[] cmd = { "p4", "print", file + "@" + changelist };
    P4Process p = new P4Process(this.env);
    p.exec(cmd);
    int loc = 0;
    while (null != p.readLine()) {
      loc++;
    }
    p.close();
    return loc;
  }
  
  /**
   * Calls the p4 program to get file type for the specified file, and returns true if
   * the file type is 'text'. 
   * (This should really be part of the official Perforce Java API.)
   * @param file The file we want stats for. Should be in the form of a depot path.  
   * @param changelist The changelist revision we want file size for. 
   * @return True if the file type is text.
   * @throws Exception if problems occur. 
   */
  private boolean isTextFile(String file, int changelist) throws Exception {
    String[] cmd = { "p4", "files", file + "@" + changelist };
    String fileInfo = runP4Command(cmd);
    return fileInfo.contains("(text)");
  }
  
  
  
  /**
   * Takes the diff2 command output, and parses it to produce an array of three integers: the
   * lines added, the lines deleted, and the lines changed. 
   * 
   * Typical diff2 output might look like this:
   * <pre>
   * Diffing file: //depot/project/Foo.java
   * ==== //depot/project/Foo.java#1 (text) - //depot/project/Foo.java#2 (text) ==== content
   * add 2 chunks 6 lines
   * deleted 0 chunks 0 lines
   * changed 3 chunks 8 / 10 lines
   * </pre>
   * 
   * Note that the diff2 command returns two numbers for "changed" regions of a file.  The first
   * number is the number of lines deleted from the changed region, while the second number
   * is the number of lines added to the changed region.   We will take the smaller of the 
   * two numbers to be the "changed" count, and then the difference between the two numbers
   * will be added to the "add" count.  (Recommended by Greg Bylenok.)
   *   
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
        int changedLinesDeleted = Integer.valueOf(tokens[3]);
        int changedLinesAdded = Integer.valueOf(tokens[5]);
        int min = Math.min(changedLinesAdded, changedLinesDeleted);
        int diff = Math.abs(changedLinesDeleted - changedLinesAdded);
        ints[2] += min;
        ints[0] += diff;
      }
    }
    return ints;
  }
  
  /**
   * This method should be invoked at the end of the sensor run, and will delete the client
   * created for this task as well as invoke the Perforce library cleanUp() method.
   * @throws Exception If problems occur. 
   */
  public void cleanup() throws Exception {
    this.runDeleteClientCommand(this.client);
    Utils.cleanUp();
  }

  /**
   * Exercises the methods in this class manually.  Useful as a way to check that you have
   * configured your p4 environment correctly if you are having problems with the sensor. 
   * Supply arguments in the following order. Examples given in parentheses:
   * <ul>
   * <li> port ("public.perforce.com:1666")
   * <li> user ("philip_johnson")
   * <li> password ("foo")
   * <li> depotPath ("//guest/philip_johnson/...")
   * <li> startDate ("2008/07/14")
   * <li> endDate ("2008/07/15")
   * </ul>
   * @param args Arguments are: port, user, password, depotPath, startDate, endDate.
   * @throws Exception If problems occur. 
   */
  public static void main(String[] args) throws Exception {
    System.out.printf("Starting PerforceCommitProcessor. %nSetting up environment...");
    P4Environment p4Env = new P4Environment();
    p4Env.setP4Port(args[0]);
    p4Env.setP4User(args[1]);
    p4Env.setP4Password(args[2]);
    p4Env.setVerbose(false); // could set this to true for lots of debugging output. 
    PerforceCommitProcessor processor = new PerforceCommitProcessor(p4Env, args[3]);
    System.out.printf("done. %nNow retrieving change lists...");
    processor.processChangeLists(args[4], args[5]);
    System.out.printf("found %d changelists. %n", processor.changeListDataList.size());
    for (PerforceChangeListData data : processor.getChangeListDataList()) {
      System.out.println(data);
      for (PerforceChangeListData.PerforceFileData fileData : data.getFileData()) {
        System.out.printf("Size of %s is %d%n", fileData.getFileName(), 
            processor.getFileSize(data.getId(), fileData.getFileName()));
      }
    }
    // Always make sure you call cleanup() at the end. 
    processor.cleanup();
    System.out.printf("Finished PerforceCommitProcessor.");
  }
  
}
