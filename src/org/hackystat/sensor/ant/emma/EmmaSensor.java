package org.hackystat.sensor.ant.emma;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.emma.resource.jaxb.All;
import org.hackystat.sensor.ant.emma.resource.jaxb.Class;
import org.hackystat.sensor.ant.emma.resource.jaxb.Coverage;
import org.hackystat.sensor.ant.emma.resource.jaxb.Data;
import org.hackystat.sensor.ant.emma.resource.jaxb.Package;
import org.hackystat.sensor.ant.emma.resource.jaxb.Report;
import org.hackystat.sensor.ant.emma.resource.jaxb.Srcfile;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.sensor.ant.util.JavaClass2FilePathMapper;
import org.hackystat.sensor.ant.util.LongTimeConverter;
import org.hackystat.sensorshell.SensorShellException;
import org.hackystat.utilities.stacktrace.StackTrace;


/**
 * Implements an Ant task that parses the XML files generated by Emma, a Java coverage tool.
 * The Ant Task sends the Coverage data to a Hackystat server.
 *
 * @author Aaron A. Kagawa, Cedric Qin Zhang, Philip Johnson
 */
public class EmmaSensor extends HackystatSensorTask {

  /** A list of all XML file sets generated by the Emma task. */
  private String emmaReportXmlFile;
  
  /** The mapper used to map class names to file paths. */
  private JavaClass2FilePathMapper javaClass2FilePathMapper;
  
  /** The name of this tool. */
  private static String tool = "Emma";
  
  /** Initialize a new instance of a EmmaSensor. */
  public EmmaSensor() {
    super(tool);
  }

  /**
   * Initialize a new instance of a EmmaSensor, passing the host and directory 
   *   key in explicitly. This supports testing. Note that when this constructor 
   *   is called, offline data recovery by the sensor is disabled.
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public EmmaSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }
  


  /**
   * Sets the file path of the emma report xml file.
   * @param filePath The emma report file path.
   */
  public void setEmmaReportXmlFile(String filePath) {
    this.emmaReportXmlFile = filePath;
  }

  /**
   * Parses the Coverage XML files and sends the resulting coverage results to
   *   the hackystat server. This method is invoked automatically by Ant.
   * @throws BuildException If there is an error.
   */
  @Override
  public void execute() throws BuildException {
    this.setupSensorShell();
    int numberOfEntries = 0;
    Date startTime = new Date();
    verboseInfo("Processing emma report file: " + this.emmaReportXmlFile);
    try {
      numberOfEntries += this.processCoverageXmlFile(this.emmaReportXmlFile);
    }
    catch (Exception e) {
      String msg = "Failure processing: " + this.emmaReportXmlFile;
      info(msg + " " + StackTrace.toString(e));
      throw new BuildException(msg, e);
    }
    this.sendAndQuit();
    summaryInfo(startTime, "Coverage", numberOfEntries);
  }

  /**
   * Parses an Emma XML file and sends the data to the shell. The only coverage information that 
   * is used by the sensor is the Emma class level report. All other coverage information is 
   * ignored; for example the sensor does not use the method element coverage information. 
   * Instead, the sensor parses the class element. Here is an example: 
   * <pre>
   * <class name="JUnitSensor">
   *   <coverage type="class, %" value="100% (1/1)"/>
   *   <coverage type="method, %" value="58%  (7/12)"/>
   *   <coverage type="block, %" value="57%  (374/656)"/>
   *   <coverage type="line, %" value="58%  (80.5/139)"/>
   * </class>
   * </pre> 
   * The granularities of class, method, block, and line are retrieved from the class element. 
   * One could dig down into the method elements, but we are not doing this at the moment.  
   * 
   * @param fileNameString The XML file name to be processed.
   * @return The number of coverage entries in this XML file.
   */
  public int processCoverageXmlFile(String fileNameString) {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
    File xmlFile = new File(fileNameString);
    // The start time for all entries will be approximated by the XML file's last mod time.
    // The shell will ensure that it's unique by tweaking the millisecond field.
    long startTime = xmlFile.lastModified();
    try {
      JAXBContext context = 
        JAXBContext.newInstance(org.hackystat.sensor.ant.emma.resource.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      
      // emma report
      Report report = (Report) unmarshaller.unmarshal(xmlFile);
      Data data = report.getData();
      All allData = data.getAll();
      
      int coverageEntriesCount = 0;
      for (Package packageReport : allData.getPackage()) {
        String packageName = packageReport.getName();
        for (Srcfile srcfile : packageReport.getSrcfile()) {
          for (Class classReport : srcfile.getClazz()) {
            String className = classReport.getName();
            String javaClassName = packageName + '.' + className;
            String javaSourceFilePath = 
              this.getJavaClass2FilePathMapper().getFilePath(javaClassName);
            if (javaSourceFilePath == null) {
              verboseInfo("Warning: Unable to find java source file path for class '" 
                  + javaClassName + "'. Use empty string for file path.");
              javaSourceFilePath = "";
            }
            
            // Alter startTime to guarantee uniqueness.
            long uniqueTstamp = this.tstampSet.getUniqueTstamp(startTime);

            // Get altered start time as XMLGregorianCalendar
            XMLGregorianCalendar startTimeGregorian = 
              LongTimeConverter.convertLongToGregorian(uniqueTstamp);

            Map<String, String> keyValMap = new HashMap<String, String>();
            keyValMap.put("Tool", "Emma");
            keyValMap.put("SensorDataType", "Coverage");

            // Required
            keyValMap.put("Runtime", runtimeGregorian.toString());
            keyValMap.put("Timestamp", startTimeGregorian.toString());
            keyValMap.put("Resource", javaSourceFilePath);

            // Optional
            keyValMap.put("ClassName", javaClassName);
                          
            
            for (Coverage coverage : classReport.getCoverage()) {
              String type = coverage.getType();
              String granularity = type.substring(0, type.indexOf(", %"));
              String value = coverage.getValue();
              String coveredString = value.substring(value.indexOf('(') + 1, value.indexOf('/'));
              String totalString = value.substring(value.indexOf('/') + 1, value.indexOf(')'));
              double covered = new Double(coveredString); 
              double total = new Double(totalString);

              keyValMap.put(granularity  + "_Covered", String.valueOf(covered));
              keyValMap.put(granularity + "_Uncovered", String.valueOf(total - covered));
            }
            
            this.sensorShell.add(keyValMap); // add data to sensorshell
            coverageEntriesCount++;
          }
        }
      }
      
      return coverageEntriesCount;
    }
    catch (JAXBException e) {
      throw new BuildException(errMsgPrefix + "JAXB Problem " + fileNameString, e);
    }
    catch (SensorShellException e) {
      throw new BuildException(errMsgPrefix + "Sensor processing problem " + fileNameString, e);
    }
  }

  /**
   * Get a java class to file path mapper.
   * @return The mapper.
   */
  private JavaClass2FilePathMapper getJavaClass2FilePathMapper() {
    if (this.javaClass2FilePathMapper == null) {
      this.javaClass2FilePathMapper = new JavaClass2FilePathMapper(this.getFiles());
    }
    return this.javaClass2FilePathMapper;
  }
}