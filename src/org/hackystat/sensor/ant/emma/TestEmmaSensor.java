package org.hackystat.sensor.ant.emma;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant Emma sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestEmmaSensor extends AntSensorTestHelper {

  /**
   * Tests sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testEmmaSensor() throws Exception {
    EmmaSensor sensor = new EmmaSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("emmatestfiles"))) {
      instances += sensor.processCoverageXmlFile(file);
    }
    assertEquals("Should have 5 entries; 4 granularities per entry", 5, instances);
  }  
}