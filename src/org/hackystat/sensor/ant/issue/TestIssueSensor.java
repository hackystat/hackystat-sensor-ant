package org.hackystat.sensor.ant.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorshell.usermap.SensorShellMapException;
import org.junit.Test;

/**
 * Test cases for Issue Sensor.
 * @author Shaoxuan
 *
 */
public class TestIssueSensor extends AntSensorTestHelper {

  
  /**
   * Test the issue sensor with public Google Project Hosting service.
   * It will get data from project http://code.google.com/feeds/p/hackystat-sensor-ant.
   * @throws SensorBaseClientException if test fail.
   * @throws SensorShellMapException if error when loading sensorshell map
   */
  //@Ignore("Broken on 2/12/2009. Also renamed method to prevent Ant-based invocation")
  @Test
  public void testIssueSensorWithGoogleProjectHosting() 
    throws SensorBaseClientException, SensorShellMapException {
    IssueSensor sensor = new IssueSensor();
    sensor.setProjectName("hackystat-sensor-ant");
    // As there is no shell map for mapping account, all data will be sensor under the name
    // of TestAntSensors@hackystat.org.
    sensor.setDataOwnerHackystatAccount(user);
    sensor.setDataOwnerHackystatPassword(user);
    sensor.setDefaultHackystatSensorbase(host);
    sensor.setVerbose(true);

    System.out.println("Testing " + user + " in " + host);
    
    try {
      SensorBaseClient client = new SensorBaseClient(host, user, user);
      sensor.execute();
      int issueSize = client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
                            .getSensorDataRef().size();
      assertTrue("Should be at least 40 issues there.", issueSize > 40);
      assertEquals("All issue should be found new.", issueSize, sensor.updatedIssues.size());
      
      sensor.execute();
      assertEquals("Number of issue extract should be the same.", issueSize, 
          client.getSensorDataIndex(user, IssueSensor.ISSUE_SENSOR_DATA_TYPE)
          .getSensorDataRef().size());
      assertEquals("Now none issue should be found new.", 0, sensor.updatedIssues.size());
      System.out.println("Testing Issue Sensor.");
    }
    catch (BuildException e) {
      System.out.println(e.getMessage());
      if (e.getMessage().contains("IO error")) {
        System.out.println("Test not run because of IO exceptions.");
      }
      fail();
      //e.printStackTrace();
      return;
    }

    
    
  }
}
