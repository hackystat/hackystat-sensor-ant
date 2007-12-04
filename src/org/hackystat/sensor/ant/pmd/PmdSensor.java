package org.hackystat.sensor.ant.pmd;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.hackystat.sensor.ant.pmd.resource.jaxb.ObjectFactory;
import org.hackystat.sensor.ant.pmd.resource.jaxb.Pmd;
import org.hackystat.sensor.ant.pmd.resource.jaxb.Violation;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.sensor.ant.util.LongTimeConverter;
import org.hackystat.sensorshell.SensorShellException;

/**
 * Implements an Ant task that parses the XML files generated by Pmd.
 * Ant Task and sends the CodeIssue data to a Hackystat server.
 *
 * @author Aaron A. Kagawa, Julie Ann Sakuda, Philip Johnson
 */
public class PmdSensor extends HackystatSensorTask {
  /** Prefix for type attributes. */
  private static final String TYPE = "Type_";
  
  /** The path of the source code PMD analyzed. */
  private Path sourcePath = null;
  
  /** Tool in UserMap to use. */
  private static String tool = "PMD";
  
  /** The list of all XML file sets generated by the Checkstyle task. */
  private List<FileSet> sourceFileSets = new ArrayList<FileSet>();
  
  /** Initialize a new instance of a PmdSensor. */
  public PmdSensor() {
    super(tool);
  }

  /**
   * Initialize a new instance of a PmdSensor, passing the host email, and password directly. This
   * supports testing. Note that when this constructor is called, offline data recovery by the
   * sensor is disabled.
   * 
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public PmdSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  /** 
   * Sets the path for the source code PMD analyzed. 
   * 
   * @param src The path containing source code.
   */
  public void setSourcePath(Path src) {
    if (this.sourcePath == null) {
      this.sourcePath = src;
    }
    else {
      this.sourcePath.append(src);
    }
  }
  
  /**
   * Initializes the source path if it has not already been initialized.
   * 
   * @return Returns the create path.
   */
  public Path createSourcePath() {
    if (this.sourcePath == null) {
      this.sourcePath = new Path(getProject());
    }
    return this.sourcePath.createPath();
  }
  
  /**
   * Sets a source path using refid.
   * 
   * @param r The reference to the source path.
   */
  public void setSourcePathRef(Reference r) {
    this.createSourcePath().setRefid(r);
  }
  
  
  /**
   * Parses the PMD XML files and sends the resulting code issue results to
   *   the hackystat server. This method is invoked automatically by Ant.
   * @throws BuildException If there is an error.
   */
  @Override
  public void execute() throws BuildException {
    setupSensorShell();

    int numberOfCodeIssues = 0;

    Date startTime = new Date();
    try {
      // Get the file names from the FileSet directives.
      ArrayList<File> files = getFiles();

      // Iterate though each file, extract the PMD data, send to sensorshell.
      for (Iterator<File> i = files.iterator(); i.hasNext();) {
        // get full path of next file to process
        String pmdXmlFile = i.next().getPath();
        verboseInfo("Processing file: " + pmdXmlFile);
        numberOfCodeIssues += processIssueXmlFile(pmdXmlFile);
      }

      if (send() > 0) {
        Date endTime = new Date();
        long elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
        info("Hackystat data on " + numberOfCodeIssues + " PMD issues sent to "
              + this.sensorProps.getSensorBaseHost() + " (" + elapsedTime + " secs.)");
      }
      else if (numberOfCodeIssues == 0) {
        info("No data to send.");
      }
      else {
        info("Failed to send Hackystat PMD issue data.");
      }
    }
    catch (Exception e) {
      throw new BuildException("Errors occurred while processing the pmd report file ", e);
    }
    finally { // After send-out, close the sensor shell.
      this.sensorShell.quit();
    }
  }
  

  /**
   * Parses a PMD XML file and sends the code issue instances to the shell.
   * @param fileNameString The XML file name to be processed.
   * @return The number of issues that have been processed in this XML file.
   * @exception BuildException if any error.
   */
  public int processIssueXmlFile(String fileNameString) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
    File xmlFile = new File(fileNameString);
    
    try {
      JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      
      Set<String> allSourceFiles = this.getAllSourceFiles();
      Set<String> filesWithViolations = new HashSet<String>();
      
      Pmd pmdResults = (Pmd) unmarshaller.unmarshal(xmlFile);
      List<org.hackystat.sensor.ant.pmd.resource.jaxb.File> files = pmdResults.getFile();
      
      int codeIssueCount = 0;
      for (org.hackystat.sensor.ant.pmd.resource.jaxb.File file : files) {
        // Base unique timestamp off of the runtime (which is when it started running)
        long uniqueTstamp = this.tstampSet.getUniqueTstamp(this.runtime);
        // Get altered time as XMLGregorianCalendar
        XMLGregorianCalendar uniqueTstampGregorian = LongTimeConverter
            .convertLongToGregorian(uniqueTstamp);
        
        // derive the full path name from the file name
        String fileName = file.getName();
        
        String fullFilePath = this.findSrcFile(allSourceFiles, fileName);
        filesWithViolations.add(fullFilePath);
        
        Map<String, String> keyValMap = new HashMap<String, String>();
        // Required
        keyValMap.put("Tool", "PMD");
        keyValMap.put("SensorDataType", "CodeIssue");
        keyValMap.put("Timestamp", uniqueTstampGregorian.toString());
        keyValMap.put("Runtime", runtimeGregorian.toString());
        keyValMap.put("Resource", fullFilePath);
        
        HashMap<String, Integer> issueCounts = new HashMap<String, Integer>();
        
        List<Violation> violations = file.getViolation();
        for (Violation violation : violations) {
          String rule = violation.getRule();
          String ruleset = violation.getRuleset();

          String key = ruleset + "_" + rule;
          key = key.replaceAll(" ", ""); // remove spaces
          if (issueCounts.containsKey(key)) {
            Integer count = issueCounts.get(key);
            issueCounts.put(key, ++count);
          }
          else {
            // no previous mapping, add 1st issue to map
            issueCounts.put(key, 1);
          }
        }
        for (Entry<String, Integer> entry : issueCounts.entrySet()) {
          String typeKey = TYPE + entry.getKey();
          keyValMap.put(typeKey, entry.getValue().toString());
        }
        
        this.sensorShell.add(keyValMap); // add data to sensorshell
        codeIssueCount++;
      }
      
      //process the zero issues 
      allSourceFiles.removeAll(filesWithViolations);
      for (String srcFile : allSourceFiles) {
        // Alter startTime to guarantee uniqueness.
        long uniqueTstamp = this.tstampSet.getUniqueTstamp(this.runtime);

        // Get altered time as XMLGregorianCalendar
        XMLGregorianCalendar uniqueTstampGregorian = LongTimeConverter
            .convertLongToGregorian(uniqueTstamp);

        Map<String, String> keyValMap = new HashMap<String, String>();
        keyValMap.put("Tool", "PMD");
        keyValMap.put("SensorDataType", "CodeIssue");
        keyValMap.put("Timestamp", uniqueTstampGregorian.toString());
        keyValMap.put("Runtime", runtimeGregorian.toString());
        keyValMap.put("Resource", srcFile);
        
        this.sensorShell.add(keyValMap); // add data to sensorshell
        codeIssueCount++;
      }
      
      return codeIssueCount;
    }
    catch (JAXBException e) {
      throw new BuildException(errMsgPrefix + "Failure in JAXB " + fileNameString, e);
    }
    catch (SensorShellException f) {
      throw new BuildException(errMsgPrefix + "Failure in SensorShell " + fileNameString, f);
    }
  }
  
  /**
   * Finds the full file path of the source path within the src files collection. For example, 
   *   srcFiles could contain: [c:\foo\src\org\Foo.java, c:\foo\src\org\Bar.java] and the 
   *   sourcePath could be org\Foo.java.  This method will find and return the full path of the 
   *   Foo.java file. 
   * @param srcFiles Contains the full path to the files. 
   * @param sourcePath Contains a trimmed version of a file path. 
   * @return The full file path, or null if the path is not found. 
   */
  private String findSrcFile(Set<String> srcFiles, String sourcePath) {
    for (String srcFile : srcFiles) {
      if (srcFile == null) {
        continue;
      }
      String alteredSourcePath = sourcePath;
      if (srcFile.contains("\\")) {
        alteredSourcePath = sourcePath.replace('/', '\\');
      }
      if (srcFile != null && srcFile.contains(alteredSourcePath)) {
        return srcFile; 
      }
    }
    return null;
  }
  
  
  /**
   * Add a file set which contains the source code PMD analyzed. 
   *   Invoked automatically by Ant.
   * @param fs The new file set of source files.
   */
  public void addSourceFileSet(FileSet fs) {
    this.sourceFileSets.add(fs);
  }
  
  /**
   * Gets all files specified by the user specified source path.
   * 
   * @return Returns a set of all fully qualified source files.
   */
  private Set<String> getAllSourceFiles() {
    Set<String> sourceFiles = new HashSet<String>();
    String[] files = this.sourcePath.list();
    for (int i = 0; i < files.length; i++) {
      String path = files[i];
      if (new File(path).isFile()) {
        sourceFiles.add(path);
      }
    }
    return sourceFiles;
  }
}