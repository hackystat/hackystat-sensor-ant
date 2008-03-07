package org.hackystat.sensor.ant.task;

import java.util.ArrayList;
import org.apache.tools.ant.types.FileSet;

/**
 * Represents the datafiles element that can be nested in a Hackystat sensor Ant task. 
 * @author Philip Johnson
 */
public class DataFiles {
  
  /** The list of all FileSets in this element. */
  protected ArrayList<FileSet> filesets = new ArrayList<FileSet>();
  
  /**
   * The sourcefiles element must contain one or more internal filesets.
   * This enables Ant to update our internal instance variable.
   * 
   * @param fs The file set.
   */
  public void addFileSet(FileSet fs) {
    filesets.add(fs);
  }
  
  /**
   * Returns the list of FileSet instances associated with the sourcefiles element. 
   * @return The list of FileSets. 
   */
  public ArrayList<FileSet> getFileSets() {
    return this.filesets;
  }

}
