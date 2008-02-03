package org.hackystat.sensor.ant.junit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.hackystat.sensor.ant.junit.resource.jaxb.Error;
import org.hackystat.sensor.ant.junit.resource.jaxb.Failure;
import org.hackystat.sensor.ant.junit.resource.jaxb.Testcase;
import org.hackystat.sensor.ant.junit.resource.jaxb.Testsuite;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.sensor.ant.util.JavaClass2FilePathMapper;
import org.hackystat.sensor.ant.util.LongTimeConverter;
import org.hackystat.sensorshell.SensorShellException;


/**
 * Implements an Ant task that parses the XML files generated by JUnit and sends the test case
 * results to the Hackystat server.
 * 
 * You can specify the location of the source files either through the 'sourcePath' attribute or the
 * 'srcPath' nested element.  I agree, this isn't optimal, but I'm going for backward 
 * compatibility at the moment.  Eventually, we probably want to get rid of the sourcePath
 * attribute option.
 * 
 * @author Philip Johnson, Hongbing Kou, Joy Agustin, Julie Ann Sakuda, Aaron A. Kagawa
 */
public class JUnitSensor extends HackystatSensorTask {

  /** 
   * String indicating the root of the source path, e.g. C:/svn/hackystat/hackySensor_JUnit, 
   * if using 'sourcePath' attribute. 
   */
  private String sourcePathString;
  
  /** A Path instance, set if using the embedded 'srcPath' element. */
  private Path srcPath = null;

  private JavaClass2FilePathMapper javaClass2FilePathMapper;

  /** The name of this tool. */
  private static String tool = "JUnit";

  /** Initialize a new instance of a JUnitSensor. */
  public JUnitSensor() {
    super(tool);
  }
  
  /**
   * Initialize a new instance of a JUnitSensor, passing the host email, and password directly. This
   * supports testing. Note that when this constructor is called, offline data recovery by the
   * sensor is disabled.
   * 
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public JUnitSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  /**
   * Sets the sourcePath attribute, which is a string representing the path to the root of the
   * source files associated with these JUnit tests. 
   * 
   * @param sourcePath Source file path.
   */
  public void setSourcePath(String sourcePath) {
    File sourcePathFile = new File(sourcePath);
    try {
      this.sourcePathString = sourcePathFile.getCanonicalPath();
    }
    catch (IOException e) {
      throw new BuildException("Could not find sourcePath: " + sourcePath, e);
    }
  }


  /**
   * Parses the JUnit XML files and sends the resulting JUnit test case results to the hackystat
   * server. This method is invoked automatically by Ant.
   * 
   * @throws BuildException If there is an error.
   */
  @Override
  public void execute() throws BuildException {
    setupSensorShell();

    int numberOfTests = 0;

    Date startTime = new Date();
    try {
      // Get the JUnit XML file names from the FileSet directives.
      ArrayList<File> files = getFiles();

      // Iterate though each file, extract the JUnit data, send to sensorshell.
      for (Iterator<File> i = files.iterator(); i.hasNext();) {
        // get full path of next file to process
        String junitXmlFile = i.next().getPath();
        verboseInfo("Processing file: " + junitXmlFile);
        numberOfTests += processJunitXmlFile(junitXmlFile);
      }

      if (send() > 0) {
        Date endTime = new Date();
        long elapsedTime = (endTime.getTime() - startTime.getTime()) / 1000;
        info("Hackystat data on " + numberOfTests + " JUnit tests sent to "
              + this.sensorProps.getSensorBaseHost() + " (" + elapsedTime + " secs.)");
      }
      else if (numberOfTests == 0) {
        info("No data to send.");
      }
      else {
        info("Failed to send Hackystat JUnit test data.");
      }
    }
    catch (Exception e) {
      throw new BuildException(errMsgPrefix + "Errors occurred while processing junit files.", e);
    }
    finally { // After send-out, close the sensor shell.
      this.sensorShell.quit();
    }
  }


  /**
   * Parses a JUnit XML file and sends the JUnitEntry instances to the shell.
   * 
   * @param fileNameString The XML file name to be processed.
   * @exception BuildException if any error.
   * @return The number of test cases in this XML file.
   */
  public int processJunitXmlFile(String fileNameString) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
    File xmlFile = new File(fileNameString);

    try {
      JAXBContext context = JAXBContext
          .newInstance(org.hackystat.sensor.ant.junit.resource.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      // One JUnit test suite per file
      Testsuite suite = (Testsuite) unmarshaller.unmarshal(xmlFile);

      String testClassName = suite.getName();
      // The start time for all entries will be approximated by the XML file's last mod time.
      // The shell will ensure that it's unique by tweaking the millisecond field.
      long startTime = xmlFile.lastModified();

      List<Testcase> testcases = suite.getTestcase();
      for (Testcase testcase : testcases) {
        // Test case name
        String testCaseName = testcase.getName();

        // Get the stop time
        double elapsedTime = testcase.getTime();
        long elapsedTimeMillis = (long) (elapsedTime * 1000);

        // Make a list of error strings.
        // This should always be a list of zero or one elements.
        List<String> stringErrorList = new ArrayList<String>();
        Error error = testcase.getError();
        if (error != null) {
          stringErrorList.add(error.getMessage());
        }

        // Make a list of failure strings.
        // This should always be a list of zero or one elements.
        List<String> stringFailureList = new ArrayList<String>();
        Failure failure = testcase.getFailure();
        if (failure != null) {
          stringFailureList.add(failure.getMessage());
        }

        String result = "pass";
        if (!stringErrorList.isEmpty() || !stringFailureList.isEmpty()) {
          result = "fail";
        }

        String name = testClassName + "." + testCaseName;
        // Alter startTime to guarantee uniqueness.
        long uniqueTstamp = this.tstampSet.getUniqueTstamp(startTime);

        // Get altered start time as XMLGregorianCalendar
        XMLGregorianCalendar startTimeGregorian = LongTimeConverter
            .convertLongToGregorian(uniqueTstamp);

        Map<String, String> keyValMap = new HashMap<String, String>();
        keyValMap.put("Tool", "JUnit");
        keyValMap.put("SensorDataType", "UnitTest");

        // Required
        keyValMap.put("Runtime", runtimeGregorian.toString());
        keyValMap.put("Timestamp", startTimeGregorian.toString());
        keyValMap.put("Name", name);
        keyValMap.put("Resource", testCaseToPath(testClassName));
        keyValMap.put("Result", result);

        // Optional
        keyValMap.put("ElapsedTime", Long.toString(elapsedTimeMillis));
        keyValMap.put("TestName", testClassName);
        keyValMap.put("TestCaseName", testCaseName);

        if (!stringFailureList.isEmpty()) {
          keyValMap.put("FailureString", stringFailureList.get(0));
        }

        if (!stringErrorList.isEmpty()) {
          keyValMap.put("ErrorString", stringErrorList.get(0));
        }

        this.sensorShell.add(keyValMap); // add data to sensorshell
      }
      return testcases.size();
    }
    catch (JAXBException e) {
      throw new BuildException(errMsgPrefix + "Failure in JAXB " + fileNameString, e);
    }
    catch (SensorShellException f) {
      throw new BuildException(errMsgPrefix + "Failure in SensorShell " + fileNameString, f);
    }
  }

  /**
   * Maps the fully qualified test case class to its corresponding source file.
   * 
   * There are two ways to specify the source files: through the sourcePath attribute, which
   * is a string containing the path to the source file directory structure, or through the
   * srcPath nested element, which is a set of fileSets. 
   * 
   * @param testCaseName Fully qualified test case name.
   * @return The source file corresponding to this test case. 
   */
  
  private String testCaseToPath(String testCaseName) {
    if (this.sourcePathString == null) {
      return testCaseToPathFromSrcPath(testCaseName);
    } 
    else {
      return testCaseToPathFromSourcePath(testCaseName);
    }
  }

  /**
   * Returns the fully qualified source file name corresponding to the passed testCaseName, 
   * using data from the srcPath element.
   * @param testCaseName The test case name.
   * @return The fully qualified source file name
   */
  private String testCaseToPathFromSrcPath(String testCaseName) {
    JavaClass2FilePathMapper mapper = this.getJavaClass2FilePathMapper();
    return mapper.getFilePath(testCaseName);
  }
  
  /**
   * Maps the testCaseName to its source file using the sourcePath attribute.
   * @param testCaseName The test case name.
   * @return A string indicating the source file corresponding to this test case. 
   */
  private String testCaseToPathFromSourcePath(String testCaseName) {
    String path = this.sourcePathString == null ? "" : this.sourcePathString;
    if (path.length() > 0 && !path.endsWith("/")) {
      path += File.separator;
    }

    // Replace dot delimiters with slash.
    StringBuffer subPath = new StringBuffer();
    String[] fragments = testCaseName.split("\\.");
    for (int i = 0; i < fragments.length; i++) {
      subPath.append(fragments[i]);
      if (i < fragments.length - 1) {
        subPath.append(File.separator);
      }
    }
    // JUnit sensor is applicable on java file only
    subPath.append(".java");
    return path + subPath;
  }
  
  /** 
   * Sets the path for the source code PMD analyzed. 
   * 
   * @param src The path containing source code.
   */
  public void setSrcPath(Path src) {
    if (this.srcPath == null) {
      this.srcPath = src;
    }
    else {
      this.srcPath.append(src);
    }
  }
  
  /**
   * Initializes the source path if it has not already been initialized.
   * 
   * @return Returns the create path.
   */
  public Path createSrcPath() {
    if (this.srcPath == null) {
      this.srcPath = new Path(getProject());
    }
    return this.srcPath.createPath();
  }
  
  /**
   * Sets a source path using refid.
   * 
   * @param r The reference to the source path.
   */
  public void setSrcPathRef(Reference r) {
    this.createSrcPath().setRefid(r);
  }
  
  /**
   * Gets all of the files specified by the srcPath element.
   * 
   * @return Returns a set of all fully qualified source files.
   */
  private Set<String> getAllSourceFiles() {
    Set<String> sourceFiles = new HashSet<String>();
    String[] files = this.srcPath.list();
    for (int i = 0; i < files.length; i++) {
      String path = files[i];
      if (new File(path).isFile()) {
        sourceFiles.add(path);
      }
    }
    return sourceFiles;
  }
  
  /**
   * Get a java class to file path mapper.
   * @return The mapper.
   */
  private JavaClass2FilePathMapper getJavaClass2FilePathMapper() {
    if (this.javaClass2FilePathMapper == null) {
      this.javaClass2FilePathMapper = new JavaClass2FilePathMapper(getAllSourceFiles());
    }
    return this.javaClass2FilePathMapper;
  }
}