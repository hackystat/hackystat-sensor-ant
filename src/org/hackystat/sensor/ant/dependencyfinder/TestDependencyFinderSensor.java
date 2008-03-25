package org.hackystat.sensor.ant.dependencyfinder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant DependencyFinder sensor.
 *
 * @author Philip Johnson
 */
public class TestDependencyFinderSensor extends AntSensorTestHelper {

  /**
   * Tests sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testDependencyFinderSensor() throws Exception {
    DependencyFinderSensor sensor = new DependencyFinderSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("dependencyfindertestfiles"))) {
      instances += sensor.processDependencyFinderXmlFile(file);
    }
    assertEquals("Currently zero due to no source files available", 0, instances);
  }  
}