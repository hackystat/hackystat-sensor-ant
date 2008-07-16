package org.hackystat.sensor.ant.perforce;

import org.hackystat.sensor.ant.task.HackystatSensorTask;

public class PerforceSensor extends HackystatSensorTask {
  
  /** The name of this tool. */
  private static String tool = "Perforce";

  /** Initialize a new instance of a PerforceSensor. */
  public PerforceSensor() {
    super(tool);
  }

  /**
   * Initialize a new instance of a PerforceSensor for testing purposes.
   * 
   * @param host The SensorBase host URL.
   * @param email The SensorBase email to use.
   * @param password The SensorBase password to use.
   */
  public PerforceSensor(String host, String email, String password) {
    super(host, email, password, tool);
  }

  @Override
  public void executeInternal() {
    // TODO Auto-generated method stub
  }

}
