package org.hackystat.sensor.ant.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.hackystat.sensor.ant.test.AntSensorTestHelper;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
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
  @Test
  public void testIssueSensorWithGoogleProjectHosing() {
    IssueSensor sensor = new IssueSensor();
    sensor.setFeedUrl("http://code.google.com/feeds/p/hackystat-sensor-ant/issueupdates/basic");
    sensor.setFromDate("2008-11-5");
    sensor.setToDate("2008-12-5");
    // As there is no shell map for mapping account, all data will be sensor under the name
    // of TestAntSensors@hackystat.org.
    sensor.setDefaultHackystatAccount(user);
    sensor.setDefaultHackystatPassword(user);
    sensor.setDefaultHackystatSensorbase(host);
    sensor.setVerbose(true);
    
    sensor.execute();

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
      /*
      assertEquals("Check first sensor data's id key", 
          "id", sensorData.getProperties().getProperty().get(3).getKey());
      assertEquals("Check first sensor data's id value", 
          "40", sensorData.getProperties().getProperty().get(0).getValue());
      assertEquals("Check first sensor data's update number key", 
          "updateNumber", sensorData.getProperties().getProperty().get(1).getKey());
      assertEquals("Check first sensor data's update number value", 
          "0", sensorData.getProperties().getProperty().get(1).getValue());
      assertEquals("Check first sensor data's status key", 
          "status", sensorData.getProperties().getProperty().get(2).getKey());
      assertEquals("Check first sensor data's status value", 
          "Created", sensorData.getProperties().getProperty().get(2).getValue());
      */
      
    }
    catch (SensorBaseClientException e) {
      // TODO Auto-generated catch block
      fail("SensorBaseClientException : " + e.getMessage());
    }
  }
}
