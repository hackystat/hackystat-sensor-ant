package org.hackystat.sensor.ant.checkstyle;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant CheckstyleSensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestCheckstyleSensor extends AntSensorTestHelper {

  /**
   * Tests CheckstyleSensor by processing some test checkstyle files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testCheckstyleSensor() throws Exception {
    CheckstyleSensor sensor = new CheckstyleSensor(host, user, user);
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("checkstyletestfiles"))) {
      instances += sensor.processIssueXmlFile(file);
    }
    assertEquals("Should have 7 issues.", 7, instances);
  }  
}