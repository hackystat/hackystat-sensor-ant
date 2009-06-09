package org.hackystat.sensor.ant.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.apache.tools.ant.BuildException;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.junit.Test;

/**
 * Test cases for Issue Sensor.
 * @author Shaoxuan
 *
 */
public class TestIssueSensor extends AntSensorTestHelper {

  /**
   * Test the issue sensor with public Google Project Hosting service.
   * It will get data from project http://code.google.com/feeds/p/hackystat-sensor-ant
   * within the period from 2008-11-5 to 2008-12-5.
   */
  //@Ignore("Broken on 2/12/2009. Also renamed method to prevent Ant-based invocation")
  @Test
  public void testIssueSensorWithGoogleProjectHosting() {
    IssueSensor sensor = new IssueSensor();
    sensor.setProjectName("hackystat-sensor-ant");
    sensor.setFromDate("2008-11-5");
    sensor.setToDate("2008-12-5");
    // As there is no shell map for mapping account, all data will be sensor under the name
    // of TestAntSensors@hackystat.org.
    sensor.setDefaultHackystatAccount(user);
    sensor.setDefaultHackystatPassword(user);
    sensor.setDefaultHackystatSensorbase(host);
    sensor.setVerbose(true);
    
    try {
      sensor.execute();
    }
    catch (BuildException e) {
      if (e.getMessage().contains("IO error")) {
        System.out.println("Test not run because of IO exceptions.");
      }
      return;
    }

    SensorBaseClient client = new SensorBaseClient(host, user, user);
    try {
      SensorDataIndex sensorDataIndex = client.getSensorDataIndex(user);
      assertEquals("There should be 6 issue events sent.", 
          6, sensorDataIndex.getSensorDataRef().size());
      //check the first data
      SensorData sensorData = client.getSensorData(sensorDataIndex.getSensorDataRef().get(0));
      assertEquals("Check first sensor data's type", "Issue", sensorData.getSensorDataType());
      assertEquals("Check first sensor data's tool", "GoogleProjectHosting", sensorData.getTool());
      assertEquals("Check first sensor data's resource", 
          "http://code.google.com/feeds/p/hackystat-sensor-ant/issueupdates/basic/40", 
          sensorData.getResource());
      boolean idChecked = false;
      boolean updateNoChecked = false;
      boolean statusChecked = false;
      for (Property property : sensorData.getProperties().getProperty()) {
        if ("id".equals(property.getKey())) {
          idChecked = true;
          assertEquals("Check first sensor data's id value", "40", property.getValue());
        }
        else if ("updateNumber".equals(property.getKey())) {
          updateNoChecked = true;
          assertEquals("Check first sensor data's updateNumber value", "0", property.getValue());
        }
        else if ("status".equals(property.getKey())) {
          statusChecked = true;
          assertEquals("Check first sensor data's status value", "Created", property.getValue());
        }
      }
      assertTrue("Id should be checked", idChecked);
      assertTrue("Update number should be checked", updateNoChecked);
      assertTrue("Status should be checked", statusChecked);

      //check the forth data
      sensorData = client.getSensorData(sensorDataIndex.getSensorDataRef().get(3));
      assertEquals("Check first sensor data's type", "Issue", sensorData.getSensorDataType());
      assertEquals("Check first sensor data's tool", "GoogleProjectHosting", sensorData.getTool());
      assertEquals("Check first sensor data's resource", 
          "http://code.google.com/feeds/p/hackystat-sensor-ant/issueupdates/basic/41/1", 
          sensorData.getResource());
      idChecked = false;
      updateNoChecked = false;
      statusChecked = false;
      for (Property property : sensorData.getProperties().getProperty()) {
        if ("id".equals(property.getKey())) {
          idChecked = true;
          assertEquals("Check forth sensor data's id value", "41", property.getValue());
        }
        else if ("updateNumber".equals(property.getKey())) {
          updateNoChecked = true;
          assertEquals("Check forth sensor data's updateNumber value", "1", property.getValue());
        }
        else if ("status".equals(property.getKey())) {
          statusChecked = true;
          assertEquals("Check forth sensor data's status value", "Accepted", property.getValue());
        }
      }
      assertTrue("Id should be checked", idChecked);
      assertTrue("Update number should be checked", updateNoChecked);
      assertTrue("Status should be checked", statusChecked);
      
      //print comments
      /*
      for (SensorDataRef ref : sensorDataIndex.getSensorDataRef()) {
        System.out.println("checking...");
        SensorData data = client.getSensorData(ref);
        for (Property property : data.getProperties().getProperty()) {
          if ("comment".equals(property.getKey())) {
            System.out.println(property.getValue());
          }
        }
      }
      */
    }
    catch (SensorBaseClientException e) {
      // TODO Auto-generated catch block
      fail("SensorBaseClientException : " + e.getMessage());
    }
  }
}
