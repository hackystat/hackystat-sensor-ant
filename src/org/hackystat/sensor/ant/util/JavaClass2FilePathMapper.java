package org.hackystat.sensor.ant.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Maps Java class name to a file name.
 * 
 * @author (Cedric) Qin Zhang
 */
public class JavaClass2FilePathMapper {

  private static final char CANONICAL_SEPARATOR = '/';

  private ArrayList<String> originalFileNames;

  private ArrayList<String> canonicalfileNames;

  /**
   * A map with the edited package path as the key and the original package path
   * as the value.
   */
  private HashMap<String, String> packagePathMap = new HashMap<String, String>();

  /**
   * This constructor accepts a list of file names, and processes this list to
   * build two parallel arrays. The first array contains all of the .java file
   * names found in the passed list, and the second contains these same file
   * names with the path separator replaced by a canonical separator.
   * 
   * @param fullyQualifiedFileNames A collection of string representing fully
   * qualified file paths or directory path for java files.
   */
  public JavaClass2FilePathMapper(Collection<String> fullyQualifiedFileNames) {
    this.originalFileNames = new ArrayList<String>(fullyQualifiedFileNames.size());
    this.canonicalfileNames = new ArrayList<String>(fullyQualifiedFileNames.size());
    for (String fileName : fullyQualifiedFileNames) {
      File file = new File(fileName);
      // Allow either 'real' files or file names ending with .java that don't
      // currently exist.
      if (file.isFile() || fileName.endsWith(".java")) {
        addFile(fileName);
      }
      else if (file.isDirectory()) {
        traverseDirectory(file);
      }
      // splits the path string into the parent path without a backspace char at
      // the end
      // ex. C:\home\austen or /home/austen
      String[] originalPath = fileName.split(".\\w+\\.java");
      // adds the parent path to a hashmap
      String editedPath = originalPath[0].replace('\\', CANONICAL_SEPARATOR);
      this.packagePathMap.put(editedPath, originalPath[0]);
    }
  }

  /**
   * Recursively traverses the passed directory, invoking "addFile" on all files
   * found.
   * 
   * @param dir The directory to traverse.
   */
  private void traverseDirectory(File dir) {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isFile()) {
        addFile(file.getAbsolutePath());
      }
      else if (file.isDirectory()) {
        traverseDirectory(file);
      }
    }
  }

  /**
   * Updates our parallel arrays if the passed File is a Java file.
   * 
   * @param fileName A file name, which might be a Java file.
   */
  private void addFile(String fileName) {
    try {
      if (fileName.endsWith(".java")) {
        String canonicalFileName = fileName.substring(0, fileName.length() - 5).replace('\\',
            CANONICAL_SEPARATOR);
        this.originalFileNames.add(fileName);
        this.canonicalfileNames.add(canonicalFileName);
      }
    }
    catch (Exception e) {
      // do nothing
      return;
    }
  }

  /**
   * Returns a string containing the java file name corresponding to the passed
   * class name.
   * 
   * @param fullyQualifiedClassName The java class name.
   * @return The java file name, or null if there is no mapping information.
   */
  public String getFilePath(String fullyQualifiedClassName) {
    // Note that this uses linear search. Since we don't have too many data, it
    // should be ok.
    // If it turns out the performance is not good, then we need to use more
    // efficient search.
    String searchString = fullyQualifiedClassName.replace('.', CANONICAL_SEPARATOR);
    int indexOfFirstDollarSign = searchString.indexOf('$');
    if (indexOfFirstDollarSign >= 0) {
      searchString = searchString.substring(0, indexOfFirstDollarSign);
    }
    int size = this.canonicalfileNames.size();
    for (int i = 0; i < size; i++) {
      String canonicalFileName = this.canonicalfileNames.get(i);
      if (canonicalFileName.endsWith(searchString)) {
        return this.originalFileNames.get(i);
      }
    }
    return null;
  }

  /**
   * Returns the canonical file names array.
   * 
   * @return The array of canonical file names.
   */
  public String toString() {
    return this.originalFileNames.toString();
  }

  /**
   * Returns the path associated with a package name. If a path is not found, an
   * empty string is returned.
   * 
   * @param packageName the name of the package.
   * @return the path associated with the package name. An empty string is
   * returned if a path is not found.
   */
  public String getPackagePath(String packageName) {
    String tempPackageName = packageName.replace('.', CANONICAL_SEPARATOR);
    for (String path : this.packagePathMap.keySet()) {
      if (path.endsWith(tempPackageName)) {
        return this.packagePathMap.get(path);
      }
    }
    return "";
  }
}
