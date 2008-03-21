package org.hackystat.sensor.ant.jdepend;

import java.io.File;
import java.util.ArrayList;

/**
 * A utility for determining the file path associated with a package. 
 * @author Philip Johnson
 *
 */
public class Package2Path {
  /** Holds the list of sourcePaths. */
  private ArrayList<String> sourcePaths = new ArrayList<String>();
  /** The path separator character on this platform. */  
  private char sep = System.getProperty("file.separator").charAt(0);
  
  /**
   * Constructs a new Package2Path instance from a list of source file paths. 
   * @param sourceFiles The source files from which the package paths are constructed.
   */
  public Package2Path(ArrayList<File> sourceFiles) {
    for (File file : sourceFiles) {
      String parentDir = file.getParent();
      if (parentDir != null) {
        sourcePaths.add(parentDir);
      }
    }
  }
  
  /**
   * Returns the source path associated with the package prefix, or null if not found. 
   * @param packageString A string such as "foo.bar.baz"
   * @return The corresponding path, such as "C:\foo\bar\baz", or null if it was not found.
   */
  public String getPath(String packageString) {
    // first, convert the package string to a partial path.
    String partialPath = packageString.replace('.', sep);
    for (String path : sourcePaths) {
      if (path.endsWith(partialPath)) {
        return path;
      }
    }
    return null;
  }
}
