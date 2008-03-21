package org.hackystat.sensor.ant.pmd;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant Pmd sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestPmdSensor extends AntSensorTestHelper {

  /**
   * Tests Sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testPmdSensor() throws Exception {
    PmdSensor sensor = new PmdSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("pmdtestfiles"))) {
      instances += sensor.processPmdXmlFile(file);
    }
    assertEquals("Should have 6 entries.", 6, instances);
  }  
}