package org.hackystat.sensor.ant.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.FileSet;

/**
 * Represents the sourcefiles element that can be nested in a Hackystat sensor Ant task. 
 * @author Philip Johnson
 */
public class SourceFiles {
  
  /** The list of all FileSets in this element. */
  protected List<FileSet> filesets = new ArrayList<FileSet>();
  
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
  public List<FileSet> getFileSets() {
    return this.filesets;
  }

}
