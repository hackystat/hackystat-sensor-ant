package org.hackystat.sensor.ant.perforce;

import com.perforce.api.Client;
import com.perforce.api.Env;
import com.perforce.api.Utils;

/**
 * Example program on how to create a client.
 * 
 * @author Philip Johnson
 */
public class CreateClient {

  /**
   * Creates a client.
   * 
   * @param argv The arguments.
   */
  public static void main(String[] argv) {
    /*
     * Please see the Common.java file to see how the environment is being set up. This is
     * important, but it is common to all the examples.
     */
    Env env = Common.setup();

    Client clnt = new Client(env, "test-client");
    clnt.setRoot("D:\\TestWorkspace2");
    clnt.addView("//guest/david_markley/p4package/...", "//test-client/...");

    try {
      clnt.commit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    Utils.cleanUp();
  }
}
