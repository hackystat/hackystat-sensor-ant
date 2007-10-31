package edu.hawaii.stack;

import java.util.Iterator;
import java.io.File;

/**
 * Implements a clear stack Abstract Data Type
 * to see the internal contents of the stack.
 * 
 * @author Philip M. Johnson 
 * @version $Id: ClearStack.java,v 1.5 2004/10/27 02:41:57 johnson Exp $
 */
public class ClearStack extends Stack {

  /**
   * Returns an iterator over the elements of the ClearStack.
   * Normally this method would be named "iterator", but we want the iterator
   * to be accessible as a Bean property.
   *
   * @return the iterator.
   */
  public Iterator<Object> getIterator() {
    return this.elements.iterator();
  } 

  /**
   * Gets the top attribute of the ClearStack object.
   *
   * @return The top value.
   * @exception EmptyStackException if the stack is empty.
   */
  public Object getTop() throws EmptyStackException {
    return this.top();
  }
}
