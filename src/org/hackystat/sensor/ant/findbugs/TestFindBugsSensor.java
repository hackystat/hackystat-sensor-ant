package org.hackystat.sensor.ant.findbugs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.junit.Test;

/**
 * Tests the Ant FindBugs sensor.
 *
 * @author Philip Johnson, Aaron A. Kagawa
 */
public class TestFindBugsSensor extends AntSensorTestHelper {

  /**
   * Tests sensor by processing some test files. 
   * @throws Exception If a problem occurs.
   */
  @Test
  public void testFindBugsSensor() throws Exception {
    FindBugsSensor sensor = new FindBugsSensor(host, user, user);
    sensor.setVerbose("off");
    int instances = 0;
    // Process the test files.
    for (File file : super.getXmlFiles(System.getProperty("findbugstestfiles"))) {
      instances += sensor.processFindBugsXmlFile(file);
    }
    assertEquals("Should have 9 entries.", 9, instances);
  }  
}