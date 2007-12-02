package org.hackystat.sensor.ant.findbugs;

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
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.findbugs.resource.jaxb.BugCollection;
import org.hackystat.sensor.ant.findbugs.resource.jaxb.BugInstance;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.sensor.ant.util.LongTimeConverter;

/**
 * Implements an Ant task that parses the XML files generated by FindBugs and sends the test case
 * results to the Hackystat server.
 * 
 * @author Philip Johnson, Hongbing Kou, Joy Agustin, Julie Ann Sakuda, Aaron A. Kagawa
 */
public class FindBugsSensor extends HackystatSensorTask {
  
  /** The name of this tool. */
  private static String tool = "FindBugs";

  /** Initialize a new instance of a FindBugsSensor. */
  public FindBugsSensor() {
    super(tool);
  }
  
  /**
   * Initialize a new instance of a FindBugsSensor, passing the host email, and password 
   * directly. This supports testing. Note that when this constructor is called, offline 
   * data recovery by the sensor is disabled.
   * 
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public FindBugsSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  /**
   * Parses the FindBugs XML files and sends the resulting FindBugs test case results to 
   * the hackystat server. This method is invoked automatically by Ant.
   * 
   * @throws BuildException If there is an error.
   */
  @Override
  public void execute() throws BuildException {
    setupSensorShell();

    int numberOfTests = 0;

    Date startTime = new Date();
    try {
      // Get the files from the FileSet directives.
      ArrayList<File> files = getFiles();

      // Iterate though each file, extract the FindBugs data, send to sensorshell.
      for (Iterator<File> i = files.iterator(); i.hasNext();) {
        // get full path of next file to process
        String findBugsXmlFile = i.next().getPath();
        verboseInfo("Processing file: " + findBugsXmlFile);
        numberOfTests += processFindBugsXmlFile(findBugsXmlFile);
      }

      if (send() > 0) {
        Date endTime = new Date();
        long elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
        info("Hackystat data on " + numberOfTests + " FindBugs tests sent to "
              + this.sensorProps.getSensorBaseHost() + " (" + elapsedTime + " secs.)");
      }
      else if (numberOfTests == 0) {
        info("No data to send.");
      }
      else {
        info("Failed to send Hackystat FindBugs test data.");
      }
    }
    catch (Exception e) {
      throw new BuildException("Errors occurred while processing the FindBugs report file ", e);
    }
    finally { // After send-out, close the sensor shell.
      this.sensorShell.quit();
    }
  }

  /**
   * Parses a FindBugs XML file and sends the FindBugsEntry instances to the shell.
   * 
   * @param fileNameString The XML file name to be processed.
   * @exception BuildException if any error.
   * @return The number of test cases in this XML file.
   */
  public int processFindBugsXmlFile(String fileNameString) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = 
      LongTimeConverter.convertLongToGregorian(new Date().getTime());
    File xmlFile = new File(fileNameString);
    // The start time for all entries will be approximated by the XML file's last mod time.
    // The shell will ensure that it's unique by tweaking the millisecond field.
    long startTime = xmlFile.lastModified();

    try {
      JAXBContext context = JAXBContext
          .newInstance(org.hackystat.sensor.ant.findbugs.resource.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      BugCollection bugCollection = (BugCollection) unmarshaller.unmarshal(xmlFile);
      Set<String> allSrcFiles = new HashSet<String>(bugCollection.getProject().getSrcDir());
      
      List<BugInstance> bugInstanceCollection = bugCollection.getBugInstance();
      
      // Sort all the bugs by the file they are from
      HashMap<String, List<BugInstance>> fileToBugs = new HashMap<String, List<BugInstance>>();
      for (BugInstance bugInstance : bugInstanceCollection) {
        String abstractSourcePath = bugInstance.getSourceLine().getSourcepath();
        String fullSourcePath = this.findSrcFile(allSrcFiles, abstractSourcePath);
        
        if (fileToBugs.containsKey(fullSourcePath)) {
          List<BugInstance> bugs = fileToBugs.get(fullSourcePath);
          bugs.add(bugInstance);
        }
        else {
          List<BugInstance> bugs = new ArrayList<BugInstance>();
          bugs.add(bugInstance);
          fileToBugs.put(fullSourcePath, bugs);
        }
      }
      
      // now process all files with bugs
      int codeIssueCount = 0;
      for (Entry<String, List<BugInstance>> entry : fileToBugs.entrySet()) {
        // Alter startTime to guarantee uniqueness.
        long uniqueTstamp = this.tstampSet.getUniqueTstamp(startTime);
        
        // Get altered start time as XMLGregorianCalendar
        XMLGregorianCalendar timestamp = 
          LongTimeConverter.convertLongToGregorian(uniqueTstamp);
        
        Map<String, String> keyValMap = new HashMap<String, String>();
        keyValMap.put("Tool", "FindBugs");
        keyValMap.put("SensorDataType", "CodeIssue");
        keyValMap.put("Runtime", runtimeGregorian.toString());
        keyValMap.put("Timestamp", timestamp.toString());
        keyValMap.put("Resource", entry.getKey());

        HashMap<String, Integer> issueCounts = new HashMap<String, Integer>();
        for (BugInstance bugInstance : entry.getValue()) {
          String category = bugInstance.getCategory();
          String type = bugInstance.getType();
          String key = category + "_" + type;

          if (issueCounts.containsKey(key)) {
            Integer count = issueCounts.get(key);
            issueCounts.put(key, ++count);
          }
          else {
            // no previous mapping, add 1st issue to map
            issueCounts.put(key, 1);
          }
        }
        for (Entry<String, Integer> issueCountEntry : issueCounts.entrySet()) {
          String typeKey = "Type_" + issueCountEntry.getKey();
          keyValMap.put(typeKey, issueCountEntry.getValue().toString());
        }
        
        this.sensorShell.add(keyValMap);
        codeIssueCount++;
      }
      
      //process the zero issues 
      allSrcFiles.removeAll(fileToBugs.keySet());
      for (String srcFile : allSrcFiles) {
        // Alter startTime to guarantee uniqueness.
        long uniqueTstamp = this.tstampSet.getUniqueTstamp(startTime);

        // Get altered start time as XMLGregorianCalendar
        XMLGregorianCalendar timestamp = 
          LongTimeConverter.convertLongToGregorian(uniqueTstamp);

        Map<String, String> keyValMap = new HashMap<String, String>();
        // Required
        keyValMap.put("Tool", "FindBugs");
        keyValMap.put("SensorDataType", "CodeIssue");
        keyValMap.put("Runtime", runtimeGregorian.toString());
        keyValMap.put("Timestamp", timestamp.toString());
        keyValMap.put("Resource", srcFile);

        this.sensorShell.add(keyValMap); // add data to sensorshell
        codeIssueCount++;
      }
      
      return codeIssueCount;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new BuildException(errMsgPrefix + "Failed to process " + fileNameString, e);
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
}