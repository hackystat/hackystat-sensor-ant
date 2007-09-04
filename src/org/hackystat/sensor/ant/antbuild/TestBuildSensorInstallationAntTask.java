package org.hackystat.sensor.ant.antbuild;

import junit.framework.TestCase;

/**
 * Test suite for <code>BuildSensorInstallationAntTask</code>.
 * 
 * @author (Cedric) Qin Zhang
 */
public class TestBuildSensorInstallationAntTask extends TestCase {

  /**
   * Test case.
   */
  public void testFilterProperty() {
    BuildSensorInstallationAntTask task = new BuildSensorInstallationAntTask();
    assertNull("Filter should not accept ${abc}.", task.filterProperty("${abc}"));
    assertNull("Filter should return null.", task.filterProperty("   ${abc}   "));
    assertNull("Should not accept property with ${}.", task.filterProperty("   ${   abc}  "));
    assertNull("Filter should return null.", task.filterProperty("   $   {    abc   }   "));
    assertEquals("Filter should return empty string.", "", task.filterProperty(""));
    assertEquals("Filter should return abc.", "abc", task.filterProperty("abc"));
    assertEquals("Filter should return ac-bc.", "ac-bc", task.filterProperty("ac-bc"));
    assertEquals("Filter should return 'Aa Bb'.", "Aa Bb", task.filterProperty("Aa Bb"));
    assertEquals("Filter should return ' Aa Bb '.", " Aa Bb ", task.filterProperty(" Aa Bb "));
  }
}
