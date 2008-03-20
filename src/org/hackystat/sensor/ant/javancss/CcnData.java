package org.hackystat.sensor.ant.javancss;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hackystat.sensor.ant.javancss.jaxb.Function;
import org.hackystat.sensor.ant.javancss.jaxb.Functions;

/**
 * A data structure that takes as input the list of Java files that were processed by JavaNCSS
 * and the Functions instance that JavaNCSS produced as a result of processing. Constructs
 * a data structure that maps the list of Java files to a string containing a list of 
 * comma-separated integers, each one representing a CCN value for one of the methods in that class.
 * If no CCN data was found for that file, null is returned.
 * 
 * @author Philip Johnson
 */
public class CcnData {
  
  /** Maps the fully qualified Java file to a list of ints indicating its methods' ccn's. */
  private Map<File, ArrayList<Integer>> file2Ccns = new HashMap<File, ArrayList<Integer>>();
  private Map<File, Integer> file2TotalLines = new HashMap<File, Integer>();
  
  /**
   * Constructs the File2CcnList, which is a mapping from a Java file path to a list of integers
   * representing the cyclometric complexity values found for all of its interior methods. 
   * @param files The Java files whose CCN numbers are to be found.
   * @param functions The Functions object contains CCN data. 
   */
  public CcnData(List<File> files, Functions functions) {
    for (Function function : functions.getFunction()) {
      String methodSignature = function.getName();
      File javaFile = findJavaFile(files, methodSignature);
      // Update our data structure only if we found a Java file corresponding to the method sig.
      if (javaFile != null) {
        // First, add the found ccn value to our list of ccn values. 
        if (!file2Ccns.containsKey(javaFile)) {
          file2Ccns.put(javaFile, new ArrayList<Integer>());
          file2TotalLines.put(javaFile, 0);
        }
        // Second, update the mapping from file to TotalLines.
        file2Ccns.get(javaFile).add(function.getCcn().intValue());
        int lines = 0;
        try {
          lines = Integer.valueOf(function.getNcss());
        }
        catch (Exception e) {
          System.out.println("Warning: could not make an integer from: " +
              function.getNcss() + ". Ignoring this value for NCSS");
        }
        int newTotalLines = file2TotalLines.get(javaFile) + lines;
        file2TotalLines.put(javaFile, newTotalLines);
      }
    }
  }

  /**
   * Takes a methodSignature, and returns the Java file associated with it, or null if no 
   * corresponding Java file could be found.
   * 
   * Method signatures look like:
   * <pre>
   * org.hackystat.sensor.xmldata.MessageDelegate.MessageDelegate(XmlDataController)
   * MessageDelegate.MessageDelegate(XmlDataController)
   * </pre>
   * @param files The list of all Files. 
   * @param methodSignature The method signature of interest.
   * @return The Java file, if one matches the method signature. 
   */
  private File findJavaFile(List<File> files, String methodSignature) {
    // Find the '.' that's just before the start of the method name and signature.
    int index = methodSignature.lastIndexOf('.');
    // Now slice off the method name and signature. 
    String fullyQualifiedClassName = methodSignature.substring(0, index);
    // Now create a path by replacing the remaining '.' with file.separator.
    String filePath = fullyQualifiedClassName.replace(".", System.getProperty("file.separator"));
    // Add the .java
    String javaPath = filePath + ".java";
    // Now we hopefully have something like: org/hackystat/sensor/xmldata/MessageDelegate.java
    // So see if any of our files ends with this string. If so, we've found a match and return it.
    for (File file : files) {
      if (file.getAbsolutePath().endsWith(javaPath)) {
        return file;
      }
    }
    // We didn't find it, so return null.
    return null;
  }
  
  /**
   * Returns a string containing a comma-separated list of CCN values for the given file, or null
   * if no CCN data is present. 
   * @param file The file whose CCN data is to be retrieved.
   * @return The CCN data string, or null.
   */
  public String getCcnData (File file) {
    List<Integer> ccnValues = file2Ccns.get(file);
    if (ccnValues == null) {
      return null;
    }
    // Otherwise we have data, so construct the string.
    StringBuilder ccnData = new StringBuilder();
    for (Integer value : ccnValues) {
      ccnData.append(value).append(",");
    }
    // strip off the final ",". 
    return ccnData.substring(0, ccnData.length() - 1);
  }
  
  /**
   * Returns the totallines value for file, or null if not found.
   * @param file The file whose total lines we're interested in. 
   * @return The number of lines, or null. 
   */
  public int getTotalLines(File file) {
    return file2TotalLines.get(file);
  }
  
  /**
   * Returns a set containing the java file paths in this CcnData instance. 
   * @return The Java file paths with CcnData. 
   */
  public Set<File> getFiles () {
    return file2Ccns.keySet();
  }
}
