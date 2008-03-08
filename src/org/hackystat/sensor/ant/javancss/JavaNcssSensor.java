package org.hackystat.sensor.ant.javancss;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.javancss.resource.jaxb.Javancss;
import org.hackystat.sensor.ant.task.HackystatSensorTask;
import org.hackystat.utilities.tstamp.Tstamp;

/**
 * Implements an Ant task that parses the XML files generated by JavaNCSS. The Ant Task sends the
 * FileMetric data to a Hackystat server.
 * 
 * @author Philip Johnson
 */
public class JavaNcssSensor extends HackystatSensorTask {

  /** The name of this tool. */
  private static String tool = "JavaNCSS";

  /** Initialize a new instance of a JavaNcssSensor. */
  public JavaNcssSensor() {
    super(tool);
  }

  /**
   * Initialize a new instance of a JavaNcssSensor for testing purposes.
   * 
   * @param host The SensorBase host URL.
   * @param email The SensorBase email to use.
   * @param password The SensorBase password to use.
   */
  public JavaNcssSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  /**
   * Parses the JavaNCSS XML file and sends the resulting FileMetric data to the SensorBase server.
   * 
   * @throws BuildException If there is an error.
   */
  @Override
  public void executeInternal() throws BuildException {
    this.setupSensorShell();
    int numberOfEntries = 0;
    Date startTime = new Date();
    for (File dataFile : getDataFiles()) {
      try {
        verboseInfo("Processing JavaNCSS file: " + dataFile);
        numberOfEntries += this.processJavaNcssXmlFile(dataFile);
      }
      catch (Exception e) {
        signalError("Failure processing: " + dataFile, e);
      }
    }
    // We've collected the data, now send it. 
    this.sendAndQuit();
    summaryInfo(startTime, "FileMetric", numberOfEntries);
  }

  /**
   * Processes the JavaNCSS XML data file, generating sensor data.
   * 
   * @param xmlFile The file containing the JavaNCSS data.
   * @return The number of FileMetrics instances generated.
   * @throws BuildException If problems occur.
   */
  private int processJavaNcssXmlFile(File xmlFile) throws BuildException {
    // The start time for all entries will be approximated by the XML file's last mod time.
    // Use the TstampSet to make it unique.
    long startTime = xmlFile.lastModified();
    int count = 0;
    try {
      JAXBContext context = JAXBContext
          .newInstance(org.hackystat.sensor.ant.javancss.resource.jaxb.ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();

      // JavaNCSS report.
      Javancss javancss = (Javancss) unmarshaller.unmarshal(xmlFile);
      // Construct the mapping from Java file paths to their CCN data.
      CcnData ccnData = new CcnData(getSourceFiles(), javancss.getFunctions());

      for (File resource : ccnData.getFiles()) {
        long tstamp = this.tstampSet.getUniqueTstamp(startTime);
        XMLGregorianCalendar tstampXml = Tstamp.makeTimestamp(tstamp);
        XMLGregorianCalendar runtimeXml = Tstamp.makeTimestamp(this.runtime);
        // Create the sensor data instance key/value map.
        Map<String, String> keyValMap = new HashMap<String, String>();
        // Required
        keyValMap.put("Tool", "JavaNCSS");
        keyValMap.put("SensorDataType", "FileMetric");
        keyValMap.put("Runtime", runtimeXml.toString());
        keyValMap.put("Timestamp", tstampXml.toString());
        keyValMap.put("Resource", resource.getAbsolutePath());
        // Expected
        keyValMap.put("TotalLines", String.valueOf(ccnData.getTotalLines(resource)));
        // Optional, but of course the whole point of this sensor.
        keyValMap.put("CyclomaticComplexityList", ccnData.getCcnData(resource));
        // add data to sensorshell
        this.sensorShell.add(keyValMap);
        count++;
      }
      return count;
    }
    catch (Exception e) {
      throw new BuildException(errMsgPrefix + "Failure: " + e.getMessage(), e);
    }
  }
}
