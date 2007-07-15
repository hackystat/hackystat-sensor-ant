package org.hackystat.sensor.ant.junit;

/**
 * Indicates problems with the JUnit sensor.
 *
 * @author Jitender Miglani, Christopher Chan
 * @version $Id: JUnitSensorException.java,v 1.1.1.1 2005/10/20 23:56:58 johnson Exp $
 */
public class JUnitSensorException extends Exception {

  /** Constructor when there is no prior exception to chain. */
  public JUnitSensorException() {
  }

  /**
   * Constructor when detail message but no prior exception to chain.
   * @param pMsg Print message.
   */
  public JUnitSensorException(String pMsg) {
    super(pMsg);
  }

  /**
   * Constructor to produce an exception with a prior exception with a detail message.
   *
   * @param pMsg The detail message.
   * @param pEx The causal exception/throwable.
   */
  public JUnitSensorException(String pMsg, Throwable pEx) {
    super(pMsg, pEx);
  }
}
