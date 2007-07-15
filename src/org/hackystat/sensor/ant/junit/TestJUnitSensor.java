package org.hackystat.sensor.ant.junit;

import java.io.File;

import junit.framework.TestCase;

import org.hackystat.core.kernel.admin.ServerProperties;
import org.hackystat.core.kernel.user.User;
import org.hackystat.core.kernel.user.UserManager;
import org.hackystat.core.kernel.util.ExtensionFileFilter;

/**
 * Tests the Ant JUnitSensor.
 *
 * @author Aaron A. Kagawa, Philip Johnson, Hongbing Kou, Joy Agustin
 * @version $Id: TestJUnitSensor.java,v 1.1.1.1 2005/10/20 23:56:58 johnson Exp $
 */
public class TestJUnitSensor extends TestCase {

  /**
   * Tests JUnitSensor by processing some test JUnit files. This test case does not check that
   * the server recieved the data, s long as we can send the data then we assume everything is ok.
   * @throws Exception If a program occurs.
   */
  public void testJUnitSensorOnTestDataSetFiles() throws Exception {
    User testUser = UserManager.getInstance().getTestUser();
    ServerProperties properties = ServerProperties.getInstance();
    JUnitSensor sensor = new JUnitSensor(properties.getHackystatHost(), testUser.getUserKey());
    sensor.setVerbose("on");
    sensor.setSourcePath("C:/svn/hackystat/hackySdt_Cli/src");
    
    File directory = new File(properties.getUserDir(testUser), "junittestfiles");
    // look for an existing XML JUnit report.
    if (!directory.isDirectory()) {
      fail("cannot find junit test files");
    }

    File[] files = directory.listFiles();
    ExtensionFileFilter filter = new ExtensionFileFilter(".xml");
    // Process all files in the testdataset/test_output directory.
    for (int j = 0; j < files.length; j++) {
      if (filter.accept((File) files[j])) {
        String fileName = ((File) files[j]).getName();
        // Process the file and send it.
        sensor.processJunitXmlFile(directory.getCanonicalPath() + File.separator + fileName);
        assertTrue("Checking send of existing XML : ", sensor.send());
      }
    }
  }  
}