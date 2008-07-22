package org.hackystat.sensor.ant.perforce;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hackystat.sensorshell.SensorShell;
import org.hackystat.sensorshell.SensorShellException;
import org.hackystat.sensorshell.SensorShellProperties;
import org.hackystat.sensorshell.usermap.SensorShellMap;
import org.hackystat.sensorshell.usermap.SensorShellMapException;
import org.hackystat.utilities.email.ValidateEmailSyntax;
import org.hackystat.utilities.time.period.Day;
import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.utilities.tstamp.TstampSet;

/**
 * A sensor for collecting Commit information from the Perforce CM system. 
 * @author Philip Johnson
 */
public class PerforceSensor extends Task {
  private String depotPath;
  private String port;
  private String userName;
  private String password;
  private String fileNamePrefix;
  private String p4ExecutablePath;
  private String defaultHackystatAccount = "";
  private String defaultHackystatPassword = "";
  private String defaultHackystatSensorbase = "";
  private String p4SysRoot = "C:\\WINDOWS";
  private String p4SysDrive = "C:";
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
  private String fromDateString, toDateString;
  private Date fromDate, toDate;
  private boolean isVerbose = false;
  private boolean ignoreWhitespace = false;
  private String tool = "perforce";
  
  /** Initialize a new instance of a PerforceSensor. */
  public PerforceSensor() {
    //nothing yet.
  }
  
  /**
   * Sets the user name to access the Perforce repository. 
   * @param userName The user name.
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Sets if verbose mode has been enabled.
   * @param isVerbose true if verbose mode is enabled, false if not.
   */
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  /**
   * Sets the password for the user name.
   * 
   * @param password The password.
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * True if whitespace changes should be ignored by the underlying Perforce diff2 program
   * when calculating lines added, changed, and deleted. 
   * @param ignoreWhitespace True if whitespace changes should be ignored. 
   */
  public void setIgnoreWhitespace(boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }

  /**
   * Sets a string to be prepended to the file path in commit metric. Recall
   * that Perforce sensor gets the depotPath to the file, 
   * however, most hackystat analysis requires fully qualified file path. This
   * prefix can be used to turn relative file path into some pseudo fully
   * qualified file path.
   * 
   * @param fileNamePrefix The string to be prepended to the file path in commit
   * metric.
   */
  public void setFileNamePrefix(String fileNamePrefix) {
    this.fileNamePrefix = fileNamePrefix;
  }

  /**
   * Sets a default Hackystat account to which to send commit data when there is
   * no Perforce user to Hackystat account mapping.
   * 
   * @param defaultHackystatAccount The default Hackystat account.
   */
  public void setDefaultHackystatAccount(String defaultHackystatAccount) {
    this.defaultHackystatAccount = defaultHackystatAccount;
  }

  /**
   * Sets the default Hackystat account password.
   * @param defaultHackystatPassword the default account password.
   */
  public void setDefaultHackystatPassword(String defaultHackystatPassword) {
    this.defaultHackystatPassword = defaultHackystatPassword;
  }

  /**
   * Sets the default Hackystat sensorbase server.
   * @param defaultHackystatSensorbase the default sensorbase server.
   */
  public void setDefaultHackystatSensorbase(String defaultHackystatSensorbase) {
    this.defaultHackystatSensorbase = defaultHackystatSensorbase;
  }

  /**
   * Sets the optional fromDate. If fromDate is set, toDate must be set. This
   * field must be conform to yyyy/MM/dd format.
   * 
   * @param fromDateString The first date from which we send commit information
   * to Hackystat server.
   */
  public void setFromDate(String fromDateString) {
    this.fromDateString = fromDateString;
  }

  /**
   * Sets the optional toDate. If toDate is set, fromDate must be set. This
   * field must be conform to yyyy/MM/dd format.
   * 
   * @param toDateString The last date to which we send commit information to
   * Hackystat server.
   */
  public void setToDate(String toDateString) {
    this.toDateString = toDateString;
  }
  
  /**
   * Sets the port to the Perforce server.  Example: "public.perforce.com:1666".
   * @param port The port. 
   */
  public void setPort(String port) {
    this.port = port;
  }
  
  /**
   * Sets the path to the p4 executable. Example: "C:\\Program Files\\Perforce\\P4.EXE".
   * @param path The path.
   */
  public void setP4ExecutablePath(String path) {
    this.p4ExecutablePath = path;
  }
  
  /**
   * Sets the path to the Window system root dir. Default: "C:\\WINDOWS".
   * This is needed by the p4 executable on Windows for reasons only it knows.
   * @param sysroot The sysroot.
   */
  public void setP4SysRoot(String sysroot) {
    this.p4SysRoot = sysroot;
  }
  
  /**
   * Sets the path to the Window system drive. Default: "C:".
   * This is needed by the p4 executable on Windows for reasons only it knows.
   * @param sysdrive The sysdrive.
   */
  public void setP4SysDrive(String sysdrive) {
    this.p4SysDrive = sysdrive;
  }
  
  /**
   * Sets the depot path in the Perforce server. Example: "//guest/philip_johnson/...".
   * @param depotPath The depot path. 
   */
  public void setDepotPath (String depotPath) {
    this.depotPath = depotPath;
  }
  
  
  /**
   * Checks and make sure all properties are set up correctly.
   * 
   * @throws BuildException If any error is detected in the property setting.
   */
  private void validateProperties() throws BuildException {
    if (this.port == null || this.port.length() == 0) {
      throw new BuildException("Attribute 'port' must be set.");
    }
    if (this.depotPath == null || this.depotPath.length() == 0) {
      throw new BuildException("Attribute 'repositoryUrl' must be set.");
    }
    if (this.userName == null || this.userName.length() == 0) {
      throw new BuildException("Attribute 'userName' must be set.");
    }
    if (this.password == null || this.password.length() == 0) {
      throw new BuildException("Attribute 'password' must be set.");
    }
    if (this.depotPath == null || this.depotPath.length() == 0) {
      throw new BuildException("Attribute 'repositoryUrl' must be set.");
    }
    if (this.p4ExecutablePath == null || this.p4ExecutablePath.length() == 0) {
      throw new BuildException("Attribute 'p4ExecutablePath' must be set.");
    }
    File p4Executable = new File(this.p4ExecutablePath);
    if (!p4Executable.exists()) {
      throw new BuildException("Attribute 'p4ExecutablePath' " + this.p4ExecutablePath +
          " does not appear to point to an actual file.");
    }
    
    // If default* is specified, then all should be specified. 
    if (((this.defaultHackystatAccount != null) || 
         (this.defaultHackystatPassword != null) ||
         (this.defaultHackystatSensorbase != null)) &&
        ((this.defaultHackystatAccount == null) || 
         (this.defaultHackystatPassword == null) ||
         (this.defaultHackystatSensorbase == null))) {
      throw new BuildException ("If one of default Hackystat account, password, or sensorbase " +
          "is specified, then all must be specified.");
    }
    
    // Check to make sure that defaultHackystatAccount looks like a real email address.
    if (!ValidateEmailSyntax.isValid(this.defaultHackystatAccount)) {
      throw new BuildException("Attribute 'defaultHackystatAccount' " + this.defaultHackystatAccount
          + " does not appear to be a valid email address.");
    }
    
    // If fromDate and toDate not set, we extract commit information for the previous day.
    if (this.fromDateString == null && this.toDateString == null) {
      Day previousDay = Day.getInstance().inc(-1);
      this.fromDate = new Date(previousDay.getFirstTickOfTheDay() - 1);
      this.toDate = new Date(previousDay.getLastTickOfTheDay());
    }
    else {
      try {
        if (this.hasSetToAndFromDates()) {
          this.fromDate = new Date(Day.getInstance(this.dateFormat.parse(this.fromDateString))
              .getFirstTickOfTheDay() - 1);
          this.toDate = new Date(Day.getInstance(this.dateFormat.parse(this.toDateString))
              .getLastTickOfTheDay());
        }
        else {
          throw new BuildException(
              "Attributes 'fromDate' and 'toDate' must either be both set or both not set.");
        }
      }
      catch (ParseException ex) {
        throw new BuildException("Unable to parse 'fromDate' or 'toDate'.", ex);
      }

      if (this.fromDate.compareTo(this.toDate) > 0) {
        throw new BuildException("Attribute 'fromDate' must be a date before 'toDate'.");
      }
    }
  }

  /**
   * Returns true if both of the to and from date strings have been set by the
   * client. Both dates must be set or else this sensor will not know which
   * revisions to grab commit information.
   * @return true if both the to and from date strings have been set.
   */
  private boolean hasSetToAndFromDates() {
    return (this.fromDateString != null) && (this.toDateString != null);
  }


  /**
   * Extracts commit information from Perforce server, and sends them to the Hackystat server.
   * 
   * @throws BuildException If the task fails.
   */
  @Override
  public void execute() throws BuildException {
    this.validateProperties(); // sanity check.
    if (this.isVerbose) {
      System.out.printf("Processing changelists for %s %s between %s and %s. %n",
          this.port, this.depotPath, this.fromDate, this.toDate);
    }

    try {
      Map<String, SensorShell> shellCache = new HashMap<String, SensorShell>();
      SensorShellMap shellMap = new SensorShellMap(this.tool);
      if (this.isVerbose) {
        System.out.println("Checking for user maps at: " + shellMap.getUserMapFile());
        System.out.println("Perforce accounts found: " + shellMap.getToolAccounts(this.tool));
      }
      try {
        shellMap.validateHackystatInfo(this.tool);
      }
      catch (Exception e) {
        System.out.println("Warning: UserMap validation failed: " + e.getMessage());
      }
      
      P4Environment p4Env = new P4Environment();
      p4Env.setP4Port(this.port);
      p4Env.setP4User(this.userName);
      p4Env.setP4Password(this.password);
      p4Env.setP4Executable(this.p4ExecutablePath);
      // These are given default values above.  User need not set them in the Ant task unless
      // the defaults are not correct.
      p4Env.setP4SystemDrive(this.p4SysDrive);
      p4Env.setP4SystemRoot(this.p4SysRoot);
      p4Env.setVerbose(false); // could set this to true for lots of p4 debugging output. 
      PerforceCommitProcessor processor = new PerforceCommitProcessor(p4Env, this.depotPath);
      processor.setIgnoreWhitespace(this.ignoreWhitespace);
      processor.processChangeLists(dateFormat.format(this.fromDate), 
          dateFormat.format(this.toDate));
      int entriesAdded = 0;
      TstampSet tstampSet = new TstampSet();
      for (PerforceChangeListData data : processor.getChangeListDataList()) {
        if (this.isVerbose) {
          System.out.printf("Retrieved Perforce changelist: %d%n", data.getId());
        }
        String author = data.getOwner();
        Date commitTime = data.getModTime();
        for (PerforceChangeListData.PerforceFileData fileData : data.getFileData()) {
          SensorShell shell = this.getShell(shellCache, shellMap, author);
          this.processCommitEntry(shell, author, tstampSet
              .getUniqueTstamp(commitTime.getTime()), commitTime, data.getId(), fileData);
          entriesAdded++;
        }
      }
      // Always make sure you call cleanup() at the end. 
      processor.cleanup();
      if (this.isVerbose) {
        System.out.println("Found " + entriesAdded + " commit records.");
      }

      // Send the sensor data after all entries have been processed.
      for (SensorShell shell : shellCache.values()) {
        if (this.isVerbose) {
          System.out.println("Sending data to " + shell.getProperties().getSensorBaseUser() + 
              " at " + shell.getProperties().getSensorBaseHost());
        }
        shell.send();
        shell.quit();
      }
    }
    catch (Exception ex) {
      throw new BuildException(ex);
    }
  }

  /**
   * Returns the shell associated with the specified author. The shellCache is
   * used to store SensorShell instances associated with the specified user. The
   * SensorShellMap contains the SensorShell instances built from the
   * UserMap.xml file. This method should be used to retrieve the SensorShell
   * instances to avoid the unnecessary creation of SensorShell instances when
   * sending data for each commit entry. Rather than using a brand new
   * SensorShell instance, this method finds the correct shell in the map,
   * cache, or creates a brand new shell to use.
   * @param shellCache the mapping of author to SensorShell.
   * @param shellMap the mapping of author to SensorShell created by a usermap
   * entry.
   * @param author the author used to retrieve the shell instance.
   * @return the shell instance associated with the author name.
   * @throws SensorShellMapException thrown if there is a problem retrieving the
   * shell instance.
   * @throws SensorShellException thrown if there is a problem retrieving
   * the Hackystat host from the v8.sensor.properties file.
   */
  private SensorShell getShell(Map<String, SensorShell> shellCache, SensorShellMap shellMap,
      String author) throws SensorShellMapException, SensorShellException {
    if (shellCache.containsKey(author)) {
      return shellCache.get(author); // Returns a cached shell instance.
    }
    else {
      // If the shell user mapping has a shell, add it to the shell cache.
      if (shellMap.hasUserShell(author)) {
        SensorShell shell = shellMap.getUserShell(author);
        shellCache.put(author, shell);
        return shell;
      }
      else { // Create a new shell and add it to the cache.
        if ("".equals(this.defaultHackystatAccount)
            || "".equals(this.defaultHackystatPassword)
            || "".equals(this.defaultHackystatSensorbase)) {
          throw new BuildException("A user mapping for the user, " + author
              + " was not found and no default Hackystat account login, password, "
              + "or server was provided.");
        }
        SensorShellProperties props = new SensorShellProperties(this.defaultHackystatSensorbase,
            this.defaultHackystatAccount, this.defaultHackystatPassword);

        SensorShell shell = new SensorShell(props, false, "svn");
        shellCache.put(author, shell);
        return shell;
      }
    }
  }

  /**
   * Processes a fileData record entry and extracts relevant metrics.
   * 
   * @param shell The shell that the commit record information is added to.
   * @param author The author of the commit.
   * @param timestamp the unique timestamp that is associated with the specified entry.
   * @param commitTime The commit time.
   * @param revision The changelist ID number.
   * @param fileData The fileData.
   * 
   * @throws Exception If there is any error.
   */
  private void processCommitEntry(SensorShell shell, String author, long timestamp, 
      Date commitTime, int revision, PerforceChangeListData.PerforceFileData fileData)
    throws Exception {
    if (shell != null) {
      String file = this.fileNamePrefix == null ? "" : this.fileNamePrefix;
      file += fileData.getFileName();

      Map<String, String> pMap = new HashMap<String, String>();
      String timestampString = Tstamp.makeTimestamp(timestamp).toString();
      pMap.put("SensorDataType", "Commit");
      pMap.put("Resource", file);
      pMap.put("Tool", "Perforce");
      pMap.put("Timestamp", timestampString);
      pMap.put("Runtime", Tstamp.makeTimestamp(commitTime.getTime()).toString());
      pMap.put("totalLines", String.valueOf(fileData.getTotalLines()));
      pMap.put("linesAdded", String.valueOf(fileData.getLinesAdded()));
      pMap.put("linesDeleted", String.valueOf(fileData.getLinesDeleted()));
      pMap.put("linesModified", String.valueOf(fileData.getLinesModified()));
      shell.add(pMap);
      if (this.isVerbose) {
        System.out.printf("Sending Perforce Commit: Timestamp: %s Resource: %s User: %s%n", 
            timestampString, file, shell.getProperties().getSensorBaseUser());
      }
    }
  }

}
