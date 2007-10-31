package edu.hawaii.stack;

import org.junit.Test;

/**
 * Checks to see that you can push three
 * objects onto the stack, then pop them off and get the exact same objects in the correct order.
 * This test also checks to see that popping an empty stack generates an exception.
 *
 * @author Philip Johnson aka PJ aka Peanut-butter Jelly.
 * @version $Id: TestStack.java,v 1.6 2004/10/27 22:58:03 johnson Exp $
 */
public class TestStack extends junit.framework.TestCase {

  /**
   * Test normal stack operations: push, pop, top and toArray methods.
   *
   * @exception EmptyStackException If errors during stack processing.
   */
  @Test
  public void testNormalOperation() throws EmptyStackException {
    Stack stack = new Stack();
    Integer one = Integer.valueOf(1);
    Integer two = Integer.valueOf(2);
    Integer three = Integer.valueOf(3);
    stack.push(one);
    stack.push(two);
    stack.push(three);
    assertEquals("Testing stack asArray", 3, stack.toArray().length);
    assertEquals("Testing toString operation", "[Stack [1, 2, 3]]", stack.toString());
    assertSame("Testing stack top of three", three, stack.top());
    assertSame("Testing stack pop of three", three, stack.pop());
    assertSame("Testing stack pop of two", two, stack.pop());
    assertSame("Testing stack pop of one", one, stack.pop());

    // Just invoke this method and make sure an exception isn't thrown.
  }


  /**
   * Test illegal pop of empty stack.
   *
   * @throws EmptyStackException If the stack was empty at the time of the pop.
   */
  @Test (expected = EmptyStackException.class)
  public void testIllegalPop() throws EmptyStackException {
    Stack stack = new Stack();
    try {
      stack.pop();
      fail("Pop of empty stack did not generate exception.");
    }
    catch (EmptyStackException e) {
      System.out.println("");
    }
  }

  /**
   * Test illegal Top of empty stack.
   * The EmptyStackException must be thrown when pop is called
   * on an empty stack.
   * @throws EmptyStackException If the stack was empty at the time of the pop.
   */
  @Test
  public void testIllegalTop() throws EmptyStackException {
    Stack s = new Stack();
    try {
      s.top();
      fail("Top of empty stack did not generate exception.");
    }
    catch (EmptyStackException e) {
      //System.out.println("");
    }
  }
}
