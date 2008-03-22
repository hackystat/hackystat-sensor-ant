package org.hackystat.sensor.ant.jdepend;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant JDepend sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestJDependSensor extends AntSensorTestHelper {

  /**
   * Tests Sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testJDependSensor() throws Exception {
    JDependSensor sensor = new JDependSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("jdependtestfiles"))) {
      instances += sensor.processJDependXmlFile(file);
    }
    assertEquals("Expecting 0, since source files missing", 0, instances);
  }  
}