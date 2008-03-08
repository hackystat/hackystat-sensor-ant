package org.hackystat.sensor.ant.junit;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
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
 * 'srcPath' nested element. I agree, this isn't optimal, but I'm going for backward compatibility
 * at the moment. Eventually, we probably want to get rid of the sourcePath attribute option.
 * 
 * @author Philip Johnson, Hongbing Kou, Joy Agustin, Julie Ann Sakuda, Aaron A. Kagawa
 */
public class JUnitSensor extends HackystatSensorTask {

  /** The class to file path mapper. */
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
    // Iterate though each file, extract the JUnit data, send to sensorshell.
    for (File dataFile : getDataFiles()) {
      verboseInfo("Processing JUnit file: " + dataFile);
      try {
        numberOfTests += processJunitXmlFile(dataFile);
      }
      catch (Exception e) {
        signalError("Failure processing: " + dataFile, e);
      }
    }
    this.sendAndQuit();
    summaryInfo(startTime, "UnitTest", numberOfTests);
  }

  /**
   * Parses a JUnit XML file and sends the JUnitEntry instances to the shell.
   * 
   * @param xmlFile The XML file name to be processed.
   * @exception BuildException if any error.
   * @return The number of test cases in this XML file.
   */
  public int processJunitXmlFile(File xmlFile) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
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
      throw new BuildException(errMsgPrefix + "Failure in JAXB " + xmlFile, e);
    }
    catch (SensorShellException f) {
      throw new BuildException(errMsgPrefix + "Failure in SensorShell " + xmlFile, f);
    }
  }

  /**
   * Maps the fully qualified test case class to its corresponding source file.
   * 
   * There are two ways to specify the source files: through the sourcePath attribute, which is a
   * string containing the path to the source file directory structure, or through the srcPath
   * nested element, which is a set of fileSets.
   * 
   * @param testCaseName Fully qualified test case name.
   * @return The source file corresponding to this test case.
   */
  private String testCaseToPath(String testCaseName) {
    JavaClass2FilePathMapper mapper = this.getJavaClass2FilePathMapper();
    return mapper.getFilePath(testCaseName);
  }

  /**
   * Get a java class to file path mapper.
   * 
   * @return The mapper.
   */
  private JavaClass2FilePathMapper getJavaClass2FilePathMapper() {
    if (this.javaClass2FilePathMapper == null) {
      this.javaClass2FilePathMapper = new JavaClass2FilePathMapper(getSourceFiles());
    }
    return this.javaClass2FilePathMapper;
  }
}