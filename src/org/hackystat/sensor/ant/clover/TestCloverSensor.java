package org.hackystat.sensor.ant.clover;

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
 * Tests the Ant CloverSensor.
 * 
 * @author Aaron A. Kagawa
 */
public class TestCloverSensor extends TestCase {
  
  /** The test user. */
  private static String user = "TestAntSensors@hackystat.org";
  private static String host = "http://localhost";
  private static Server server;

  /**
   * Starts the server going for these tests, and makes sure our test user is registered. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    TestCloverSensor.server = Server.newInstance();
    TestCloverSensor.host = TestCloverSensor.server.getHostName();
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
   * Tests CloverSensor by processing some test coverage files. This test case does
   * not check that the server received the data, as long as we can send the
   * data then we assume everything is ok.
   * 
   * @throws Exception If a program error occurs.
   */
  public void testCloverSensorOnTestDataSetFiles() throws Exception {
    CloverSensor sensor = new CloverSensor(host, user, user);
    sensor.setVerbose("false");
    //FileSet javaSourceFiles = new FileSet();
    //sensor.addFileSet(javaSourceFiles);
    
    String testFileDirPath = System.getProperty("clovertestfiles");
    File directory = new File(testFileDirPath);
    // look for an existing XML JUnit report.
    if (!directory.isDirectory()) {
      fail("cannot find clover test files");
    }
    
    File[] files = directory.listFiles();
    // create a file filter that only accepts xml files
    FileFilter filter = new FileFilter() {
      public boolean accept(File pathname) {
        if (pathname.getName().endsWith(".xml")) {
          return true;
        }
        return false;
      }
    };
    
    int coverageEntries = 0;
    // Process all files
    for (int j = 0; j < files.length; j++) {
      if (filter.accept(files[j])) {
        String fileName = files[j].getName();
        // Process the file.
        coverageEntries += sensor.processCoverageXmlFile(directory.getCanonicalPath() + 
            File.separator + fileName);
      }
    }
    assertSame("Should have 14 entries; 4 granularities per entry", 14, coverageEntries);
  }
}