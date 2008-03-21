package org.hackystat.sensor.ant.clover;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.clover.jaxb.Coverage;
import org.hackystat.sensor.ant.clover.jaxb.File;
import org.hackystat.sensor.ant.clover.jaxb.Metrics;
import org.hackystat.sensor.ant.clover.jaxb.Package;
import org.hackystat.sensor.ant.clover.jaxb.Project;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.sensor.ant.util.JavaClass2FilePathMapper;
import org.hackystat.sensor.ant.util.LongTimeConverter;
import org.hackystat.sensorshell.SensorShellException;

/**
 * Implements an Ant task that parses the XML files generated by the Clover coverage tool and 
 * sends the data to a Hackystat server.
 *
 * @author Aaron A. Kagawa, Philip Johnson
 */
public class CloverSensor extends HackystatSensorTask {

  /** The mapper used to map class names to file paths. */
  private JavaClass2FilePathMapper javaClass2FilePathMapper;
  
  /** The name of this tool. */
  private static String tool = "Clover";
  
  /** Initialize a new instance of a CloverSensor. */
  public CloverSensor() {
    super(tool);
  }

  /**
   * Initialize a new instance of a CloverSensor, passing the host and directory 
   *   key in explicitly. This supports testing. Note that when this constructor 
   *   is called, offline data recovery by the sensor is disabled.
   * @param host The hackystat host URL.
   * @param email The Hackystat email to use.
   * @param password The Hackystat password to use.
   */
  public CloverSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  /**
   * Parses the Coverage XML files and sends the resulting coverage results to
   * the hackystat server. This method is invoked automatically by Ant.
   * @throws BuildException If there is an error.
   */
  @Override
  public void executeInternal() throws BuildException {
    this.setupSensorShell();
    int numberOfEntries = 0;
    Date startTime = new Date();
    
    for (java.io.File dataFile : getDataFiles()) {
      verboseInfo("Processing Clover file: " + dataFile);
      try {
        numberOfEntries += this.processCoverageXmlFile(dataFile);
      }
      catch (Exception e) {
        signalError("Failure processing Clover file: " + dataFile, e);
      }
    }
    this.sendAndQuit();
    summaryInfo(startTime, "Coverage", numberOfEntries);
  }

  /**
   * Parses an Clover XML file and sends the data to the shell. The only coverage information that 
   * is used by the sensor is the Clover class level report. All other coverage information is 
   * ignored; for example the sensor does not use the method element coverage information. 
   * Instead, the sensor parses the class element. Here is an example: 
   * <pre>
   * <file name="PmdSensor.java">
   *   <class name="PmdSensor">
   *     <metrics methods="18" conditionals="36" coveredstatements="77" coveredmethods="7" 
   *       coveredconditionals="11" statements="141" coveredelements="95" elements="195"/>
   *   </class>
   *   <metrics classes="1" methods="18" conditionals="36" ncloc="270" coveredstatements="77" 
   *     coveredmethods="7" coveredconditionals="11" statements="141" loc="456" 
   *     coveredelements="95" elements="195"/>
   *   ...
   * </file>
   * </pre> 
   * The granularities of file metrics element.  One could dig down into the line elements but 
   * we aren't doing that now.   
   * 
   * @param xmlFile The XML file name to be processed.
   * @exception BuildException if any error.
   * @return The number of coverage entries in this XML file.
   */
  public int processCoverageXmlFile(java.io.File xmlFile) throws BuildException {
    XMLGregorianCalendar runtimeGregorian = LongTimeConverter.convertLongToGregorian(this.runtime);
    // The start time for all entries will be approximated by the XML file's last mod time.
    // The shell will ensure that it's unique by tweaking the millisecond field.
    long startTime = xmlFile.lastModified();


    try {
      JAXBContext context = 
        JAXBContext.newInstance(org.hackystat.sensor.ant.clover.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      
      // clover report
      Coverage coverage = (Coverage) unmarshaller.unmarshal(xmlFile);
      Project project = coverage.getProject();
      
      int coverageEntriesCount = 0;
      for (Package packageReport : project.getPackage()) {
        String packageName = packageReport.getName();
        
        for (File file : packageReport.getFile()) {
          String fileName = file.getName();
          Metrics metrics = file.getMetrics();
          String className = file.getClazz().getName();
          String javaClassName = packageName + '.' + className;
          String javaSourceFilePath = fileName;
          // for some reason sometimes file names can be fully qualified. 
          // not sure how to configure clover to do that. if its not fully 
          // qualified then we try to use the mapping. 
          if (javaSourceFilePath.length() <= (className + ".java").length()) {
            javaSourceFilePath = this.getJavaClass2FilePathMapper().getFilePath(javaClassName);
            if (javaSourceFilePath == null) {
              verboseInfo("Warning: Unable to find java source file path for class '" 
                  + javaClassName + "'. Using empty string as the resource.");
              javaSourceFilePath = "";
            }
          }
          
          // Alter startTime to guarantee uniqueness.
          long uniqueTstamp = this.tstampSet.getUniqueTstamp(startTime);

          // Get altered start time as XMLGregorianCalendar
          XMLGregorianCalendar startTimeGregorian = 
            LongTimeConverter.convertLongToGregorian(uniqueTstamp);

          Map<String, String> keyValMap = new HashMap<String, String>();
          keyValMap.put("Tool", "Clover");
          keyValMap.put("SensorDataType", "Coverage");

          // Required
          keyValMap.put("Runtime", runtimeGregorian.toString());
          keyValMap.put("Timestamp", startTimeGregorian.toString());
          keyValMap.put("Resource", javaSourceFilePath);

          // Optional
          keyValMap.put("ClassName", javaClassName);
                        
          int total = metrics.getConditionals();
          int covered = metrics.getCoveredconditionals();
          keyValMap.put("conditional_Covered", String.valueOf(covered));
          keyValMap.put("conditional_Uncovered", String.valueOf(total - covered));
          
          total = metrics.getElements();
          covered = metrics.getCoveredelements();
          keyValMap.put("element_Covered", String.valueOf(covered));
          keyValMap.put("element_Uncovered", String.valueOf(total - covered));

          total = metrics.getStatements();
          covered = metrics.getCoveredstatements();
          keyValMap.put("statement_Covered", String.valueOf(covered));
          keyValMap.put("statement_Uncovered", String.valueOf(total - covered));

          total = metrics.getMethods();
          covered = metrics.getCoveredmethods();
          keyValMap.put("method_Covered", String.valueOf(covered));
          keyValMap.put("method_Uncovered", String.valueOf(total - covered));

          // add data to sensorshell
          this.sensorShell.add(keyValMap); 
          coverageEntriesCount++;
        }
      }
      return coverageEntriesCount;
    }
    catch (JAXBException e) {
      throw new BuildException(errMsgPrefix + "Failure in JAXB " + xmlFile, e);
    }
    catch (SensorShellException f) {
      throw new BuildException(errMsgPrefix + "Failure in SensorShell " + xmlFile, f);
    }
  }

  /**
   * Get a java class to file path mapper.
   * @return The mapper.
   */
  private JavaClass2FilePathMapper getJavaClass2FilePathMapper() {
    if (this.javaClass2FilePathMapper == null) {
      this.javaClass2FilePathMapper = new JavaClass2FilePathMapper(this.getSourceFiles());
    }
    return this.javaClass2FilePathMapper;
  }
  
}