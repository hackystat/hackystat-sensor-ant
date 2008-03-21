package org.hackystat.sensor.ant.junit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant JUnit sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestJUnitSensor extends AntSensorTestHelper {

  /**
   * Tests Sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testJUnitSensor() throws Exception {
    JUnitSensor sensor = new JUnitSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("junittestfiles"))) {
      instances += sensor.processJUnitXmlFile(file);
    }
    assertEquals("Should have 4 entries.", 4, instances);
  }  
}