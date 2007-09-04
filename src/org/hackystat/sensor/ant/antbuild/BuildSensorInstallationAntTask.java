package org.hackystat.sensor.ant.antbuild;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Task;

/**
 * A convenient way to install build sensor ant listener. The build sensor is implemented as an ant
 * listener. However, there is no way to specify listeners in ant build files. The only way to
 * specify listeners is using command line with "-listener" option, which may not be possible in all
 * cases. Even if it is logistically feasible in some cases, there is still no way to set relevant
 * context information.
 * <p>
 * This class provides an ant task to install the required listener. Preferably, you should write
 * the build script so that this task runs as early as possible.
 * <p>
 * Another purpose of this ant task is to gather build context information, such as Hackystat
 * project name, build configuration name, etc.
 * 
 * @author (Cedric) Qin Zhang
 */
public class BuildSensorInstallationAntTask extends Task {

  private boolean verbose = false;
  private boolean debug = false;

  private boolean monitorCheckstyle = false;
  private boolean monitorCompilation = false;
  private boolean monitorJUnit = false;

  private String keyValuePairString = null;
  private String tool = null;
  private String toolAccount = null;

  /**
   * Sets the key value pairs information.
   * 
   * @param keyValuePairString A comma-delimited string representing key value pair.
   */
  public void setKeyValuePairs(String keyValuePairString) {
    this.keyValuePairString = keyValuePairString;
  }

  /**
   * Sets verbose mode.
   * 
   * @param verbose True to enable verbose mode.
   */
  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  /**
   * Sets debug mode.
   * 
   * @param debug True to enable debug mode.
   */
  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  /**
   * Sets the optional UserMap tool to use.
   * 
   * @param tool The name of the tool mapping containing the tool account given.
   */
  public void setUserMapTool(String tool) {
    this.tool = tool;
  }

  /**
   * Sets the option UserMap tool account to use.
   * 
   * @param toolAccount The tool account listed under the given tool to use.
   */
  public void setUserMapToolAccount(String toolAccount) {
    this.toolAccount = toolAccount;
  }

  /**
   * Determines whether we should watch for checkstyle errors.
   * 
   * @param monitorCheckstyle True if we should watch for checkstyle errors.
   */
  public void setMonitorCheckstyle(boolean monitorCheckstyle) {
    this.monitorCheckstyle = monitorCheckstyle;
  }

  /**
   * Determines whether we should watch for compilation errors.
   * 
   * @param monitorCompilation True if we should watch for compilation errors.
   */
  public void setMonitorCompilation(boolean monitorCompilation) {
    this.monitorCompilation = monitorCompilation;
  }

  /**
   * Determines whether we should watch for JUnit errors.
   * 
   * @param monitorJUnit True if we should watch for JUnit errors.
   */
  public void setMonitorJUnit(boolean monitorJUnit) {
    this.monitorJUnit = monitorJUnit;
  }

  /**
   * Filters the property value to get rid of "${...}". If "${...}" is detected, then null is
   * returned.
   * 
   * @param originalValue The original value.
   * @return The filtered value, or null.
   */
  String filterProperty(String originalValue) {
    String pattern = "^[ \\t]*\\$[ \\t]*\\{.*\\}[ \\t]*$";
    if (originalValue.matches(pattern)) {
      return null;
    }
    else {
      return originalValue;
    }
  }

  /**
   * Hooks <code>BuildSensorAntListener</code> to ant system, if it's not already hooked.
   * 
   * @throws BuildException If there is an error.
   */
  @SuppressWarnings("unchecked")
  public void execute() throws BuildException {
    //debug option turns on verbose automatically.
    if (this.debug) {
      this.verbose = true;
    }

    //process key-value pair string if they are set.
    Map<String, String> keyValueMap = new TreeMap<String, String>();
    //    if (this.keyValuePairString != null) {
    //      String[] pairs = keyValuePairString.split(",");
    //      for (int i = 0; i < pairs.length; i++) {
    //        String pair = pairs[i].trim();
    //        String[] keyValue = pair.split("=");
    //        if (keyValue.length != 2) {
    //          throw new BuildException("Malformatted 'keyValuePairs' attribute.");
    //        }
    //        String key = keyValue[0].trim();
    //        String value = this.filterProperty(keyValue[1].trim());
    //        if (value == null) {
    //          value = "Unknown";
    //        }
    //        keyValueMap.put(key, value);
    //      }
    //Make sure there is only one instance of the listener.
    boolean instanceFound = false;
    for (Iterator i = this.getProject().getBuildListeners().iterator(); i.hasNext();) {
      BuildListener listener = (BuildListener) i.next();
      if (listener instanceof BuildSensorAntListener) {
        instanceFound = true;
        break;
      }
    }

    //Build sensor ant listener not installed, install one.
    if (instanceFound) {
      if (this.verbose) {
        System.out.println("Warning: Build sensor ant listener already installed.");
      }

    }
    else {
      BuildSensorAntListener buildSensor = new BuildSensorAntListener(this.verbose, this.debug,
          this.monitorCheckstyle, this.monitorCompilation, this.monitorJUnit, keyValueMap,
          this.tool, this.toolAccount);
      this.getProject().addBuildListener(buildSensor);
      if (this.verbose) {
        System.out.println("Build sensor ant listener installed.");
      }
    }
    //}
  }
}