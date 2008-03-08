package org.hackystat.sensor.ant.pmd;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Tests the Ant PmdSensor.
 *
 * @author Aaron A. Kagawa, Julie Ann Sakuda
 */
public class TestPmdSensor extends TestCase {
  
  /** The test user. */
  private static String user = "TestAntSensors@hackystat.org";
  private static String host = "http://localhost";
  private static Server server;

  /**
   * Starts the server going for these tests, and makes sure our test user is registered. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    TestPmdSensor.server = Server.newInstance();
    TestPmdSensor.host = TestPmdSensor.server.getHostName();
    SensorBaseClient.registerUser(host, user);
  }
  
  /**
   * Gets rid of the sent sensor data and the user. 
   * @throws Exception If problems occur setting up the server. 
   */
  @AfterClass public static void teardownServer() throws Exception {
    // Now delete all data sent by this user.
    SensorBaseClient client = new SensorBaseClient(host, user, user);
    // First, delete all sensor data sent by this user. 
    SensorDataIndex index = client.getSensorDataIndex(user);
    for (SensorDataRef ref : index.getSensorDataRef()) {
      client.deleteSensorData(user, ref.getTimestamp());
    }
    // Now delete the user too.
    client.deleteUser(user);
  }
  
  /**
   * Tests PmdSensor by processing some test pmd files. This test case does not 
   *   check that the server received the data, s long as we can send the data 
   *   then we assume everything is ok.
   * @throws Exception If a program occurs.
   */
  public void testPmdSensorOnTestDataSetFiles() throws Exception {
    String testFileDirPath = System.getProperty("pmdtestfiles");
    PmdSensor sensor = new PmdSensor(host, user, user);
    
    File directory = new File(testFileDirPath);
    if (!directory.isDirectory()) {
      fail("cannot find pmd test files");
    }
    File[] files = directory.listFiles();
    // create a file filter that only accepts pmd.xml
    FileFilter filter = new FileFilter() {
      public boolean accept(File pathname) {
        if ("pmd.xml".equals(pathname.getName())) {
          return true;
        }
        return false;
      }
    };
    
    // Process all files
    int count = 0;
    for (int j = 0; j < files.length; j++) {
      if (filter.accept(files[j])) {
        count = sensor.processIssueXmlFile(files[j]);
      }
    }
    assertEquals("Checking number of CodeIssue entries", 3, count);
  }  
}