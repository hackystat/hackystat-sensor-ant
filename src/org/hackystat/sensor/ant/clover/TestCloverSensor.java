package org.hackystat.sensor.ant.clover;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant Clover sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestCloverSensor extends AntSensorTestHelper {

  /**
   * Tests CheckstyleSensor by processing some test checkstyle files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testCloverSensor() throws Exception {
    CloverSensor sensor = new CloverSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("clovertestfiles"))) {
      instances += sensor.processCoverageXmlFile(file);
    }
    assertEquals("Should have 14 entries; 4 granularities per entry", 14, instances);
  }  
}