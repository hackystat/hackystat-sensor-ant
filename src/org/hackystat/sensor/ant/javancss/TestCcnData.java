package org.hackystat.sensor.ant.javancss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.hackystat.sensor.ant.javancss.jaxb.Function;
import org.hackystat.sensor.ant.javancss.jaxb.Functions;
import org.junit.Test;

/**
 * Tests the CcnData abstraction.
 * @author Philip Johnson
 */
public class TestCcnData {

  /** The path to our hypothetical files. */
  private File dir = new File("proj" + System.getProperty("file.separator") + 
      "foo" + System.getProperty("file.separator") + 
      "bar" + System.getProperty("file.separator"));
  
  /** The files we will have in our testing data structure. */
  private File file1 = new File(dir, "Baz.java"); 
  private File file2 = new File(dir, "Zob.java"); 
  private File file3 = new File(dir, "Quark.java"); 

  /**
   * Tests the ccnData abstraction.
   */
  @Test 
  public void  testCcnData() {
    CcnData ccnData = new CcnData(makeFiles(), makeFunctions());
    assertEquals("Test Baz ccn", "1", ccnData.getCcnData(file1));
    assertEquals("Test Baz ncss", 1, ccnData.getTotalLines(file1));
    assertEquals("Test Zob ccn", "2,3", ccnData.getCcnData(file2));
    assertEquals("Test Zob ncss", 5, ccnData.getTotalLines(file2));
    assertNull("Test Quark", ccnData.getCcnData(file3));
  }

  /**
   * Creates an example Functions instance for testing.
   * Has data on two classes: Baz and Zob.  Baz has one method and Qux has two.
   * That's a total of three function instances inside this Functions object.  
   * @return The Functions instance. 
   */
  private Functions makeFunctions() {
    Functions functions = new Functions();
    Function function1 = new Function();
    function1.setName("foo.bar.Baz.qux()");
    function1.setCcn(BigInteger.valueOf(1));
    function1.setNcss("1");
    Function function2 = new Function();
    function2.setName("Zob.boffo()");
    function2.setCcn(BigInteger.valueOf(2));
    function2.setNcss("2");
    Function function3 = new Function();
    function3.setName("Zob.twork()");
    function3.setCcn(BigInteger.valueOf(3));
    function3.setNcss("3");
    functions.getFunction().add(function1);
    functions.getFunction().add(function2);
    functions.getFunction().add(function3);
    return functions;
  }
  
  /**
   * Creates and returns a list of three files.  Two should have CCN data, but one 
   * should not. 
   * @return The list of three files for testing. 
   */
  private List<File> makeFiles() {
    List<File> files = new ArrayList<File>();
    files.add(file1);
    files.add(file2);
    files.add(file3);
    return files;
  }

}
