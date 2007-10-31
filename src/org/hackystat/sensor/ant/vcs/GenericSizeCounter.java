package org.hackystat.sensor.ant.vcs;

/**
 * Generic size counter. Note that this class is not desinged to count binary files.
 *
 * @author Qin ZHANG
 * @version $Id: GenericSizeCounter.java,v 1.1.1.1 2005/10/20 23:56:56 johnson Exp $
 */
public class GenericSizeCounter {

  private int totalLines = 0;
  private int nonEmptyLines = 0;
  
  /**
   * Construct this instance to count lines.
   *
   * @param content The file content.
   */
  public GenericSizeCounter(Object[] content) {
    for (int i = 0; i < content.length; i++) {
      Object line = content[i];

      if (line != null) {
        this.totalLines++;

        if (line instanceof String) {
          String str = (String) line;
          if (!this.isLineWhiteSpace(str)) {
            this.nonEmptyLines++;
          }
        }
        else {
          this.nonEmptyLines++;
        }
      }
    }
  }

  /**
   * Tests whether the line contains white space only or not.
   *
   * @param line The line to be tested.
   * @return True if the lines contains only white spaces or is null.
   */
  private boolean isLineWhiteSpace(String line) {
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (!Character.isWhitespace(ch)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the total number of lines, including empty lines.
   *
   * @return The total number of lines.
   */
  public int getNumOfTotalLines() {
    return this.totalLines;
  }

  /**
   * Gets the number of lines, excluding empty lines.
   *
   * @return The number of lines, excluding empty lines.
   */
  public int getNumOfNonEmptyLines() {
    return this.nonEmptyLines;
  }
}
