package org.hackystat.sensor.ant.junit;

import java.io.File;
import java.io.FileFilter;

import junit.framework.TestCase;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the Ant JUnitSensor.
 * 
 * @author Aaron A. Kagawa, Philip Johnson, Hongbing Kou, Joy Agustin, Julie Ann Sakuda
 */
public class TestJUnitSensor extends TestCase {

  /** The test user. */
  private static String user = "TestAntSensors@hackystat.org";
  private static String host = "http://localhost";
  private static Server server;

  /**
   * Starts the server going for these tests, and makes sure our test user is registered.
   * 
   * @throws Exception If problems occur setting up the server.
   */
  @BeforeClass
  public static void setupServer() throws Exception {
    ServerProperties properties = new ServerProperties();
    properties.setTestProperties();
    TestJUnitSensor.server = Server.newInstance(properties);
    TestJUnitSensor.host = TestJUnitSensor.server.getHostName();
    SensorBaseClient.registerUser(host, user);
  }

  /**
   * Gets rid of the sent sensor data and the user.
   * 
   * @throws Exception If problems occur setting up the server.
   */
  @AfterClass
  public static void teardownServer() throws Exception {
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
   * Tests JUnitSensor by processing some test JUnit files. This test case does not check that the
   * server received the data, as long as we can send the data then we assume everything is ok.
   * 
   * @throws Exception If a program error occurs.
   */
  @Test public void testJUnitSensorOnTestDataSetFiles() throws Exception {
    JUnitSensor sensor = new JUnitSensor(host, user, user);
    String testFileDirPath = System.getProperty("junittestfiles");
    File directory = new File(testFileDirPath);
    // look for an existing XML JUnit report.
    if (!directory.isDirectory()) {
      fail("cannot find junit test files");
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

    int testcases = 0;
    // Process all files
    for (int j = 0; j < files.length; j++) {
      if (filter.accept(files[j])) {
        testcases += sensor.processJunitXmlFile(files[j]);
      }
    }
    assertSame("Should have 4 entries.", 4, testcases);
  }
}