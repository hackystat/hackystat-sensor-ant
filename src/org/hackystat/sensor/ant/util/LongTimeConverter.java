package org.hackystat.sensor.ant.util;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.tools.ant.BuildException;

/**
 * Utilities to convert times represented as a long to other formats.
 * 
 * @author jsakuda
 *
 */
public class LongTimeConverter {
  
  /** Private constructor for utility class. */
  private LongTimeConverter() {
    // do nothing
  }
  
  /**
   * Converts a time represented in a long to a XmlGregorianCalendar.
   * 
   * @param timeInMillis The time to convert in milliseconds.
   * @return Returns the time passed in as a <code>XmlGregorianCalendar</code>.
   */
  public static XMLGregorianCalendar convertLongToGregorian(long timeInMillis) {
    DatatypeFactory factory = null;
    try {
      factory = DatatypeFactory.newInstance();
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(timeInMillis);
      return factory.newXMLGregorianCalendar(calendar);
    }
    catch (DatatypeConfigurationException e) {
      throw new BuildException("Error creating DatatypeFactory used for converting tstamp.", e);
    }
  }
}
