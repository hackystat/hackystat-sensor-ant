package org.hackystat.sensor.ant.checkstyle;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.hackystat.sensor.ant.checkstyle.resource.jaxb.Checkstyle;
import org.hackystat.sensor.ant.checkstyle.resource.jaxb.Error;
import org.hackystat.sensor.ant.util.LongTimeConverter;
import org.hackystat.sensorshell.SensorProperties;
import org.hackystat.sensorshell.SensorPropertiesException;
import org.hackystat.sensorshell.SensorShell;
import org.hackystat.sensorshell.usermap.SensorShellMap;
import org.hackystat.sensorshell.usermap.SensorShellMapException;
import org.hackystat.utilities.tstamp.TstampSet;

/**
 * Implements an Ant task that parses the XML files generated by Checkstyle. The Ant Task sends the
 * CodeIssue data to a Hackystat server.
 * 
 * @author Julie Ann Sakuda
 */
public class CheckstyleSensor extends Task {

  /** The list of all XML file sets generated by the Checkstyle task. */
  private ArrayList<FileSet> filesets;

  /** Whether or not to print out messages during sensor execution. */
  private boolean verbose = false;

  /** Specifies the runtime of the sensor. All issues sent will have the same runtime. */
  private long runtime = 0;

  /** The sensor shell instance used by this sensor. */
  private SensorShell sensorShell;

  /** Sensor properties to be used with the sensor. */
  private SensorProperties sensorProps;

  /** Guarantees unique timestamps for each code issue generated. */
  private TstampSet tstampSet = null;

  /** Tool in UserMap to use. */
  private String tool;

  /** Tool account in the UserMap to use. */
  private String toolAccount;

  /** Initialize a new instance of a CheckstyleSensor. */
  public CheckstyleSensor() {
    this.filesets = new ArrayList<FileSet>();
    this.tstampSet = new TstampSet();
    this.runtime = new Date().getTime();
  }

  /**
   * Initialize a new instance of a CheckstyleSensor, passing the host email, and password directly.
   * This supports testing. Note that when this constructor is called, offline data recovery by the
   * sensor is disabled.
   * 
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public CheckstyleSensor(String host, String email, String password) {
    this.filesets = new ArrayList<FileSet>();
    this.sensorProps = new SensorProperties(host, email, password);
    this.sensorShell = new SensorShell(this.sensorProps, false, "test", false);
    this.tstampSet = new TstampSet();
    this.runtime = new Date().getTime();
  }

  /**
   * Sets up the sensorshell instance to use either based on the given tool & tool account or from
   * the sensor.properties file. DO NOT call this method in the constructor. The optional properties
   * tool and tool account do not get set until after the constructor is done.
   */
  private void setupSensorShell() {
    if (isUsingUserMap()) {
      try {
        SensorShellMap map = new SensorShellMap(this.tool);
        this.sensorShell = map.getUserShell(this.toolAccount);
      }
      catch (SensorShellMapException e) {
        throw new BuildException(e.getMessage(), e);
      }
    }
    // sanity check to make sure the prop and shell haven't already been set by the
    // constructor that takes in the email, password, and host
    else if (this.sensorProps == null && this.sensorShell == null) {
      // use the sensor.properties file
      try {
        this.sensorProps = new SensorProperties();
        this.sensorShell = new SensorShell(this.sensorProps, false, "Checkstyle");
      }
      catch (SensorPropertiesException e) {
        System.out.println(e.getMessage());
        System.out.println("Exiting...");
        throw new BuildException(e.getMessage(), e);
      }

      if (!this.sensorProps.isFileAvailable()) {
        System.out.println("Could not find sensor.properties file. ");
        System.out.println("Expected in: " + this.sensorProps.getAbsolutePath());
      }
    }
  }

  /**
   * Set the verbose attribute to "on", "true", or "yes" to enable trace messages while the
   * Checkstyle sensor is running.
   * 
   * @param mode The new verbose value: should be "on", "true", or "yes" to enable.
   */
  public void setVerbose(String mode) {
    this.verbose = Project.toBoolean(mode);
  }

  /**
   * Allows the user to specify the tool in the UserMap that should be used when sending data. Note
   * that setting the tool will only have an effect if the tool account is also specified. Otherwise
   * it will be ignored and the values in v8.sensor.properties will be used.
   * 
   * @param tool The tool containing the tool account to be used when sending data.
   */
  public void setUserMapTool(String tool) {
    this.tool = tool;
  }

  /**
   * Allows the user to specify the tool account in the UserMap under the given tool to use when
   * sending data. Note that setting the tool account will only have an effect if the tool is also
   * specified. Otherwise the tool account will be ignored and v8.sensor.properties file values will
   * be used.
   * 
   * @param toolAccount The tool account in the UserMap to use when sending data.
   */
  public void setUserMapToolAccount(String toolAccount) {
    this.toolAccount = toolAccount;
  }

  /**
   * Parses the Checkstyle XML files and sends the resulting code issue results to the hackystat
   * server. This method is invoked automatically by Ant.
   * 
   * @throws BuildException If there is an error.
   */
  public void execute() throws BuildException {
    setupSensorShell();

    int numberOfCodeIssues = 0;
    Date startTime = new Date();
    // Get the file names from the FileSet directives.
    ArrayList<File> files = getFiles();
    // Iterate though each file, extract the Checkstyle data, send to sensorshell.
    for (File file : files) {
      String checkstyleXmlFile = file.getPath();
      if (this.verbose) {
        System.out.println("Processing file: " + checkstyleXmlFile);
      }
      try {
        numberOfCodeIssues += this.processIssueXmlFile(checkstyleXmlFile);
      }
      catch (Exception e) {
        // don't stop processing. just report back to the user of the problem.
        System.out.println("Errors occurred while processing the issue xml file " + e);
      }
    }
    if (this.send() > 0) {
      Date endTime = new Date();
      long elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
      if (isUsingUserMap()) {
        // no sensorProps exists because we used the sensorshell map
        System.out.println("Hackystat data on " + numberOfCodeIssues + " Checkstyle issues sent to "
            + "host stored in UserMap with tool '" + this.tool + "' and tool account '"
            + this.toolAccount + "' (" + elapsedTime + " secs.)");
      }
      else {
        System.out.println("Hackystat data on " + numberOfCodeIssues + " Checkstyle issues sent to "
            + this.sensorProps.getHackystatHost() + " (" + elapsedTime + " secs.)");
      }
    }
    else if (numberOfCodeIssues == 0) {
      System.out.println("No data to send.");
    }
    else {
      System.out.println("Failed to send Hackystat Code Issue data.");
    }
  }

  /**
   * Sends any accumulated data in the SensorShell to the server.
   * 
   * @return Returns the number of entries sent.
   */
  public int send() {
    return this.sensorShell.send();
  }

  /**
   * Parses a Checkstyle XML file and sends the code issue instances to the shell.
   * 
   * @param fileNameString The XML file name to be processed.
   * @return The number of issues that have been processed in this XML file.
   * @exception BuildException thrown if it fails to process a file.
   */
  public int processIssueXmlFile(String fileNameString) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
    File xmlFile = new File(fileNameString);
    try {
      JAXBContext context = JAXBContext
          .newInstance(org.hackystat.sensor.ant.checkstyle.resource.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      Checkstyle checkstyle = (Checkstyle) unmarshaller.unmarshal(xmlFile);
      // list of file elements in the checkstyle result file
      List<org.hackystat.sensor.ant.checkstyle.resource.jaxb.File> checkedFiles = checkstyle
          .getFile();
      
      // TODO when zero data is implemented this should count entries sent
      int errorCount = 0;
      for (org.hackystat.sensor.ant.checkstyle.resource.jaxb.File file : checkedFiles) {
        // Fully qualified name of the file checked
        String fileName = file.getName();

        // gets all error elements for the file
        List<Error> errors = file.getError();

        // TODO take zero data into account and remove this
        if (errors.isEmpty()) {
          continue;
        }
        
        for (Error error : errors) {
          int line = error.getLine();
          String errorMessage = error.getMessage();
          String severity = error.getSeverity(); // warning, ignore, or error
          String source = error.getSource();
          Integer column = error.getColumn(); // not all errors will have a column
          
          // Base unique timestamp off of the runtime (which is when it start running)
          long uniqueTstamp = this.tstampSet.getUniqueTstamp(this.runtime);

          // Get altered time as XMLGregorianCalendar
          XMLGregorianCalendar uniqueTstampGregorian = LongTimeConverter
              .convertLongToGregorian(uniqueTstamp);
          
          Map<String, String> keyValMap = new HashMap<String, String>();
          keyValMap.put("Tool", "Checkstyle");
          keyValMap.put("SensorDataType", "CodeIssue");
          
          // Required
          keyValMap.put("Timestamp", uniqueTstampGregorian.toString());
          keyValMap.put("Runtime", runtimeGregorian.toString());
          keyValMap.put("Resource", fileName);
          keyValMap.put("Type", source);
          
          // Optional
          keyValMap.put("Line", String.valueOf(line));
          keyValMap.put("Severity", severity);
          keyValMap.put("Message", errorMessage);
          if (column != null) {
            keyValMap.put("Column", String.valueOf(column));
          }
          this.sensorShell.add(keyValMap);
          errorCount++;
        }
      }
      // TODO this should return the number of issues and zero data entries
      return errorCount;
    }
    catch (Exception e) {
      throw new BuildException("Failed to process " + fileNameString + "   " + e);
    }
  }

  /**
   * Add a file set which contains the Checkstyle report xml file to be processed. Invoked
   * automatically by Ant.
   * 
   * @param fs The new file set of xml results.
   */
  public void addFileSet(FileSet fs) {
    filesets.add(fs);
  }

  /**
   * Get all of the files in the file set.
   * 
   * @return All files in the file set.
   */
  private ArrayList<File> getFiles() {
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
    return fileList;
  }

  /**
   * Gets whether or not this sensor instance is using a mapping in the UserMap.
   * 
   * @return Returns true of the tool and tool account are set, otherwise false.
   */
  private boolean isUsingUserMap() {
    return (this.tool != null && this.toolAccount != null);
  }
}