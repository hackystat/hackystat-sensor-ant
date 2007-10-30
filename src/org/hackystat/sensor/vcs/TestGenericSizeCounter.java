package org.hackystat.sensor.vcs;

import junit.framework.TestCase;

/**
 * Test case for <code>GenericSizeCounter</code>.
 * 
 * @author Qin ZHANG
 * @version $Id$
 */
public class TestGenericSizeCounter extends TestCase {

  /**
   * Tests with strings.
   * @throws Exception If test fails.
   */
  public void test1() throws Exception {
    String[] content = new String[] { " line1 ", "   ", "line2", " \t line3  ", null, " \t  ",
      "\t" };
    GenericSizeCounter size = new GenericSizeCounter(content);
    assertEquals("The total amount of lines is incorrect.", 6, size.getNumOfTotalLines());
    assertEquals("The total non-empty lines is incorrect.", 3, size.getNumOfNonEmptyLines());
  }

  /**
   * Tests with objects.
   * @throws Exception If test fails.
   */
  public void test2() throws Exception {
    Object[] content = new Object[] { new Object(), null, new Object() };
    GenericSizeCounter size = new GenericSizeCounter(content);
    assertEquals("The total amount of lines is incorrect.", 2, size.getNumOfTotalLines());
    assertEquals("The total non-empty lines is incorrect.", 2, size.getNumOfNonEmptyLines());
  }
}