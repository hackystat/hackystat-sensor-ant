package org.hackystat.sensor.ant.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.hackystat.sensorshell.SensorShell;
import org.hackystat.sensorshell.SensorShellException;
import org.hackystat.sensorshell.SensorShellProperties;
import org.hackystat.sensorshell.usermap.SensorShellMap;
import org.hackystat.sensorshell.usermap.SensorShellMapException;
import org.hackystat.utilities.tstamp.TstampSet;

/**
 * An abstract superclass containing instance variables and helper methods used
 * by Hackystat Sensors implemented as Ant tasks.  This includes almost all of the 
 * sensors except for the Ant Build sensor, which is implemented as a listener.
 * 
 * @author Philip Johnson
 */
public abstract class HackystatSensorTask extends Task {
  
  /** The list of all XML file sets generated by the subtask. */
  protected List<FileSet> filesets = new ArrayList<FileSet>();
  
  /** The list of datafile element instances found in the task. */
  protected List<DataFiles> dataFilesList = new ArrayList<DataFiles>();

  /** The list of sourcefile element instances found in this task. */
  protected List<SourceFiles> sourceFilesList = new ArrayList<SourceFiles>();

  /** Whether or not to print out messages during sensor execution. */
  protected boolean verbose = false;

  /** Provides a fixed runtime value for use in sensors that need one.  */
  protected long runtime = new Date().getTime();

  /** The sensor shell instance used by this sensor. */
  protected SensorShell sensorShell;

  /** Sensor properties to be used with the sensor. */
  protected SensorShellProperties sensorProps;

  /** Supports generation of unique timestamps. */
  protected TstampSet tstampSet = new TstampSet();

  /** The name of this tool, passed to SensorShell and also looked for in UserMap. */
  protected String tool;

  /** Tool account in the UserMap to use, or null if not using UserMap. */
  protected String toolAccount;
  
  /** The string that prefixes all error messages from this sensor. */
  protected String errMsgPrefix;
  
  /** The string that prefixes all normal messages from this sensor. */
  protected String msgPrefix;
  
  /** Prevent no-arg constructor from being instantiated externally by accident. */
  @SuppressWarnings("unused")
  private HackystatSensorTask() { }
  
  /**
   * The standard constructor, which will instantiate a SensorShell using the configuration data
   * in sensorshell.properties. 
   * @param tool The tool associated with this sensor. 
   */
  protected HackystatSensorTask (String tool) {
    this.tool = tool;
    this.msgPrefix = "[Hackystat " + tool + " Sensor] "; 
    this.errMsgPrefix = msgPrefix + "ERROR: ";
  }

  /**
   * A special constructor used for testing purposes, which will use the supplied SensorBase
   * host, email, and password. Automatically sets verbose mode to true.
   * @param host The host.
   * @param email The email.
   * @param password The password. 
   * @param tool The tool associated with this sensor.
   */
  protected HackystatSensorTask(String host, String email, String password, String tool) {
    this(tool);
    this.verbose = true;
    verboseInfo("Creating a test sensorshell...");
    try {
      this.sensorProps = SensorShellProperties.getTestInstance(host, email, password);
    }
    catch (SensorShellException e) {
      throw new BuildException(errMsgPrefix + "Problem making test sensorshell.properties", e);
    }
    this.sensorShell = new SensorShell(this.sensorProps, false, tool);
  }
  
  /**
   * Instantiates the internal sensorshell using either the data in the UserMap or the data
   * in sensorshell.properties. Does not instantiate the sensorshell if it already has been done
   * in the testing constructor. 
   *   
   * DO NOT call this method in the constructor. The toolAccount property which controls where
   * the SensorShell data is obtained is not set until after the 
   * tool and tool account do not get set until after the constructor is done.
   */
  protected void setupSensorShell() {
    Properties preferMultiShell = new Properties();
    // Enabling multishell adds almost 5 seconds of startup time. Therefore, I'm not going to
    // enable it by default.  I'm leaving this in here as commented out code in case someone
    // in future wants to see how to create a custom default for all Ant sensors that can still
    // be overridden in the user's sensorshell.properties file. 
    // For now, I just pass in an empty properties instance, which does nothing. 
   //preferMultiShell.setProperty(SensorShellProperties.SENSORSHELL_MULTISHELL_ENABLED_KEY, "true");
    
    if (isUsingUserMap()) {
      verboseInfo("Creating a SensorShell based upon UserMap data...");
      try {
        SensorShellMap map = new SensorShellMap(this.tool);
        this.sensorShell = map.getUserShell(this.toolAccount, preferMultiShell);
        this.sensorProps = this.sensorShell.getProperties();
      }
      catch (SensorShellMapException e) {
        throw new BuildException(errMsgPrefix + "Problem finding account info in UserMaps.", e);
      }
    }
    // Instantiate the sensorshell using sensorshell.properties, unless we've already created one
    // using the test constructor.
    else if (this.sensorProps == null && this.sensorShell == null) {
      verboseInfo("Creating a SensorShell using sensorshell.properties data...");
      // use the sensor.properties file
      try {
        this.sensorProps = new SensorShellProperties(preferMultiShell, false);
        this.sensorShell = new SensorShell(this.sensorProps, false, this.tool);
      }
      catch (SensorShellException e) {
        throw new BuildException(errMsgPrefix + "Problem creating Hackystat Sensor: " + 
            e.getMessage(), e);
      }
    }
    verboseInfo(this.sensorShell.getProperties().toString());
  }
  
  /**
   * Set the verbose attribute to "on", "true", or "yes" to enable trace messages while the
   * sensor is running.
   * 
   * @param mode The new verbose value: should be "on", "true", or "yes" to enable.
   */
  public void setVerbose(String mode) {
    this.verbose = Project.toBoolean(mode);
    verboseInfo("Verbose mode enabled.");
  }
  
  /**
   * Allows the user to override the default tool string to be used to retrieve data from the 
   * UserMap. Note that UserMap processing is only enabled when the toolAccount value is provided.  
   * 
   * @param tool The tool containing the tool account to be used when sending data.
   */
  public void setUserMapTool(String tool) {
    this.tool = tool;
  }
  
  /**
   * If the user specifies a toolAccount, then the UserMap is consulted to specify the sensorshell
   * host, user, and password data rather than sensorshell.properties.
   * Note that the user does not need to specify the tool explicitly, it will be provided with a
   * default value by the sensor, although specifying it explicitly might be good for 
   * documentation purposes. 
   * 
   * @param toolAccount The tool account in the UserMap to use when sending data.
   */
  public void setUserMapToolAccount(String toolAccount) {
    this.toolAccount = toolAccount;
  }
  
  /**
   * Allows the sensor to support a fileset element. This may have no effect if the sensor
   * does not process an internal fileset.
   * 
   * @param fs The file set, if needed by the sensor. 
   */
  public void addFileSet(FileSet fs) {
    filesets.add(fs);
  }
  
  /**
   * Creates a returns a new SourceFiles instance to Ant.  Ant will then populate this guy with
   * its internal file set. 
   * @return The SourceFiles instance. 
   */
  public SourceFiles createSourceFiles() {
    SourceFiles newSourceFiles = new SourceFiles();
    this.sourceFilesList.add(newSourceFiles);
    return newSourceFiles;
  }
  
  /**
   * Creates a returns a new DataFiles instance to Ant.  Ant will then populate this guy with
   * its internal file set. 
   * @return The DataFiles instance. 
   */
  public DataFiles createDataFiles() {
    DataFiles newDataFiles = new DataFiles();
    this.dataFilesList.add(newDataFiles);
    return newDataFiles;
  }
  
  /**
   * Helper method for converting a fileset into a list of files.
   *  
   * @return All files in the file set.
   */
  protected ArrayList<File> getFiles() {
    return getFiles(this.filesets);
  }
  
  /**
   * Returns the list of files indicated in the sourcefiles element. 
   * @return The list of files in the sourcefiles element. 
   */
  protected ArrayList<File> getSourceFiles() {
    List<FileSet> filesets = new ArrayList<FileSet>();
    // Create our list of filesets from all nested SourceFiles.
    for (SourceFiles sourceFiles : this.sourceFilesList) {
      filesets.addAll(sourceFiles.getFileSets());
    }
    return getFiles(filesets);
  }
  
  /**
   * Returns the list of files indicated in the datafiles element. 
   * @return The list of files in the datafiles element. 
   */
  protected ArrayList<File> getDataFiles() {
    List<FileSet> filesets = new ArrayList<FileSet>();
    // Create our list of filesets from all nested SourceFiles.
    for (DataFiles dataFiles : this.dataFilesList) {
      filesets.addAll(dataFiles.getFileSets());
    }
    return getFiles(filesets);
  }
  
  /**
   * Converts a list of FileSets into a list of their associated files. 
   * @param filesets The filesets of interest.
   * @return The files of interest. 
   */
  protected ArrayList<File> getFiles(List<FileSet> filesets) {
    ArrayList<File> fileList = new ArrayList<File>();
    final int size = filesets.size();
    for (int i = 0; i < size; i++) {
      FileSet fs = filesets.get(i);
      DirectoryScanner ds = fs.getDirectoryScanner(getProject());
      ds.scan();
      String[] f = ds.getIncludedFiles();

      for (int j = 0; j < f.length; j++) {
        String pathname = f[j];
        File file = new File(ds.getBasedir(), pathname);
        file = getProject().resolveFile(file.getPath());
        fileList.add(file);
      }
    }
    verboseInfo("Found the following files in the fileset: " + fileList);
    return fileList;
  }
  
  /**
   * Returns true if the user has indicated they want to use the UserMap to obtain the SensorBase
   * host, user, and password. If false, then the sensorshell.properties file should be 
   * consulted. 
   * 
   * @return Returns true if UserMap processing is enabled, false otherwise.
   */
  protected boolean isUsingUserMap() {
    return (this.tool != null && this.toolAccount != null);
  }
  
  /**
   * Output a verbose message if verbose is enabled.
   * @param msg The message to print if verbose mode is enabled.
   */
  protected final void verboseInfo(String msg) { 
    if (this.verbose) {
      System.out.println(msg);
    }
  }

  /**
   * Output an informational message from sensor.
   * @param msg The message.
   */
  protected final void info(String msg) {
    System.out.println(msg);
  }
  
  /**
   * Logs a final summary message regarding the number of entries sent and the elapsed time. 
   * @param startTime The start time of the sensor. 
   * @param sdt The type of data sent.
   * @param numEntries The number of entries sent. 
   */
  protected void summaryInfo(Date startTime, String sdt, int numEntries) {
    Date endTime = new Date();
    long elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
    info(numEntries + " " + sdt + " entries sent to " + this.sensorProps.getSensorBaseHost()
        + " (" + elapsedTime + " secs.)");
  }
  
  /**
   * Sends any accumulated data in the SensorShell to the server.
   * 
   * @return Returns the number of entries sent.
   */
  protected int send() {
    try {
      return this.sensorShell.send();
    }
    catch (SensorShellException e) {
      throw new BuildException(errMsgPrefix + "Problem sending data: " + e.getMessage(), e);
    }
  }

}
