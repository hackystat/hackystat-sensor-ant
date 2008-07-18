package org.hackystat.sensor.ant.perforce;

import com.perforce.api.Debug;
import com.perforce.api.Env;

/**
 * Provides a simple mechanism for configuring the Perforce Java API connection to the p4 tool. 
 * Provides a wrapper around the Perforce Java API Env object, and supplies defaults for certain
 * properties. 
 * Typical usage:
 * <pre>
 * P4Environment p4Env = new P4Environment();
 * p4Env.setPort("myperforceserver.foo.com:1666");
 * p4Env.setUser("philip_johnson");
 * p4Env.setPassword("foo");
 * p4Env.setVerbose(true);
 * Env env = p4Env.getEnv();
 * </pre>
 * @author Philip Johnson
 */
public class P4Environment {
  /** The path to the p4 executable. */
  private String p4Executable = "C:\\Program Files\\Perforce\\P4.EXE";
  /** The hostname and port for the perforce server. */
  private String p4Port = "public.perforce.com:1666";
  /** The authorized Perforce user for this server. */
  private String p4User = null;
  /** The password for this user on this server. */
  private String p4Password = null;
  /** If running on windows, need this for p4. */
  private String p4SysRoot = "C:\\WINDOWS";
  /** If running on windows, need this for p4. */
  private String p4SysDrive = "C:";
  /** Determines whether the p4 output is verbose or not. */
  private boolean isVerbose = false;

  /** Constructs a new P4Environment with default property values. */
  public P4Environment () {
    // Does nothing.
  }
  
  /**
   * Sets the path to the p4 executable. Default: "C:\\Program Files\\Perforce\\P4.EXE"
   * @param path The path. 
   */
  public void setP4Executable(String path) {
    this.p4Executable = path;
  }
  
  /**
   * Sets the port for the perforce server. Default: "public.perforce.com:1666".
   * @param port The port.
   */
  public void setP4Port(String port) {
    this.p4Port = port;
  }
  
  /**
   * Sets the perforce user.  Example: philip_johnson.
   * @param user The user. 
   */
  public void setP4User(String user) {
    this.p4User = user;
  }
  
  /**
   * Sets the password for the perforce user. 
   * @param password The password.
   */
  public void setP4Password(String password) {
    this.p4Password = password;
  }
  
  /**
   * Sets the system root. Only needed on Windows systems. Default: "C:\\WINDOWS". 
   * @param sysroot The system root.
   */
  public void setP4SystemRoot (String sysroot) {
    this.p4SysRoot = sysroot;
  }
  
  /**
   * Sets the system drive.  Only needed on Windows systems. Default: "C:". 
   * @param sysdrive The system drive.
   */
  public void setP4SystemDrive (String sysdrive) {
    this.p4SysDrive = sysdrive;
  }
  
  /**
   * Sets verbose mode. If true, lots of output regarding p4 tool execution is printed. 
   * @param isVerbose True to set verbose mode. 
   */
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  /**
   * Returns a Perforce Java API Env instance constructed from the data supplied to this instance.
   * @return The Env instance. 
   */
  public Env getEnv() {
    Env env = new Env();
    env.setExecutable(p4Executable);
    env.setPort(p4Port);
    env.setUser(p4User);
    env.setPassword(p4Password);
    env.setSystemRoot(p4SysRoot);
    env.setSystemDrive(p4SysDrive);
    if (isVerbose) {
      Debug.setDebugLevel(Debug.VERBOSE); 
      Debug.setLogLevel(Debug.LOG_SPLIT);
    }
    return env;
  }
}
