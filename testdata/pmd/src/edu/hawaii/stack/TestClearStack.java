package edu.hawaii.stack;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Checks to see that a ClearStack
 * containing three elements can be iterated through and that this iteration retrieves the
 * elements in the correct order.
 *
 * @author Philip Johnson
 */
public class TestClearStack extends junit.framework.TestCase {

  // Objects used for testing.
  private Integer ONE = Integer.valueOf(1);
  private Integer TWO = Integer.valueOf(2);
  private Integer THREE = Integer.valueOf(3);

  /**
   * Test the ClearStack iterator.
   */
  @Test
  public void testNormalOperation() {
    ClearStack stack = new ClearStack();
    stack.push(ONE);
    stack.push(TWO);
    stack.push(THREE);
    ArrayList<Integer> list = new ArrayList<Integer>();
    // Go through the elements in the stack and save them in an ArrayList.
    for (Iterator<Object> i = stack.getIterator(); i.hasNext(); ) {
      Integer element = (Integer) i.next();
      list.add(element);
    }
    // Now check to see they were there and in the right order.
    assertSame(ONE, list.get(0));
    assertSame(TWO, list.get(1));
    assertSame(THREE, list.get(2));
  }

  /**
   * Test getTop operation.
   * @exception EmptyStackException If errors during stack processing.
   */

  @Test
  public void testGetTop() throws EmptyStackException {
    ClearStack stack = new ClearStack();

    try {
      stack.getTop();
      fail("Top of empty stack did not generate exception.");
    }
    catch (EmptyStackException e) {
      assert true : e;
    }

    stack.push(ONE);
    assertSame("Testing stack top of one", ONE, stack.getTop());
    stack.push(TWO);
    assertSame("Testing stack top of two", TWO, stack.getTop());
    stack.push(THREE);
    assertSame("Testing stack top of three", THREE, stack.getTop());

  }

}
