package edu.hawaii.stack;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the stack abstract data type.
 * 
 * @author Philip M. Johnson 
 */
public class Stack {

  /**
   * Holds the elements of the stack.
   * Visibility is protected to allow subclass access to internal representation.
   */
  protected List<Object> elements = new ArrayList<Object>();
  
  /**
   * Pushes obj onto the stack.
   *
   * @param obj Any object.
   */
  public void push(Object obj) {
    this.elements.add(obj);
  }

  /**
   * Returns the last object pushed onto the stack and removes
   * that object from the stack.
   *
   * @return The last object pushed onto the stack.
   * @throws EmptyStackException If the stack was empty at the time of the pop.
   */
  public Object pop() throws EmptyStackException {
    try {
      Object obj = this.elements.get(elements.size() - 1);
      elements.remove(elements.size() - 1);
      return obj;
    }
    catch (Exception e) {
      throw new EmptyStackException(e);
    }
  }

  /**
   * Returns the last object pushed onto the stack without removing it.
   *
   * @return The last object pushed onto the stack.
   * @throws EmptyStackException If the stack was empty at the time top was called.
   */
  public Object top() throws EmptyStackException {
    try {
      return this.elements.get(elements.size() - 1);
    }
    catch (Exception e) {
      throw new EmptyStackException(e);
    }
  }

  /**
   * Returns a shallow copy of the current stack as an array.
   *
   * @return An array containing the elements of the stack.
   */
  public Object[] toArray() {    
    return this.elements.toArray();
  }

  /**
   * Provides a readable representation of the stack.
   * toSting method follows this format, "[Stack 'object']"
   *
   * @return The stack as a string.
   */
  public String toString() {
    return "[Stack " + this.elements + "]";
  }
}
