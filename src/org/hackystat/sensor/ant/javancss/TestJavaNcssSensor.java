package org.hackystat.sensor.ant.javancss;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant JavaNcss sensor
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestJavaNcssSensor extends AntSensorTestHelper {

  /**
   * Tests Sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testJavaNcssSensor() throws Exception {
    JavaNcssSensor sensor = new JavaNcssSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("javancsstestfiles"))) {
      instances += sensor.processJavaNcssXmlFile(file);
    }
    assertEquals("Currently returns 0 because source files not known", 0, instances);
  }  
}