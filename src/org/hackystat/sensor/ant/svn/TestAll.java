package org.hackystat.sensor.ant.svn;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Test suite for everything in this package.
 * 
 * @author (Cedric) Qin ZHANG
 * @version $Id$
 */
public class TestAll extends TestCase {

  //private String svnRepositoryUrl = "svn://localhost:9999/test";
  private SVNCommitProcessor p = null;
  private boolean runTest = false;
  
  /**
   * Test case set up.
   * 
   * @throws Exception If we cannot set up this test case.
   */
  @Override
  protected void setUp() throws Exception {
    //we only run test in a environment we can access a testing svn server.
    //To bad, JavaSVN does not support file protocol in verions 1.0 release.
    //Otherwise, we can always run the test through file protocol.
    String url = System.getProperty("hackySensor_Svn.TestingRepositoryUrl");
    if (url == null) {
      System.out.println("WARNING: Test cases in org.hackystat.sensor.svn.core.TestAll not run "
                  + "because environment variable hackySensor_Svn.TestingRepositoryUrl not set "
                  + "(i.e. Testing SVN server not available).");
      this.runTest = false;
    }
    else {
      this.p = new SVNCommitProcessor(url, null, null);
      this.runTest = true;
    }
  }

   /**
     * Tests get svn revision number.
     * 
     * @throws Exception If test fails.
     */
  public void testGetRevisionNumberWithOutliers() throws Exception {
    if (!runTest) {
      return;
    }
    assertEquals("The revision number is not 0.", 0, p.getRevisionNumber(new Date(0)));
    assertEquals("The revision number is not 0.", 0, p.getRevisionNumber(new Date(
        Long.MIN_VALUE)));
    assertEquals("The revision number is not 0", 9, p.getRevisionNumber(new Date(
        Long.MAX_VALUE)));
  }
  //  
  // /**
  // * Tests everything in this package.
  // *
  // * @throws Exception If test fails.
  // */
  // public void testAll() throws Exception {
  // if (!runTest) {
  // return;
  // }
  //    
  // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  // assertEquals(0, this.p.getRevisionNumber(dateFormat.parse("2000-01-01")));
  // assertEquals(9, this.p.getRevisionNumber(dateFormat.parse("2005-12-12")));
  //    
  // //revision 0, non-existent. JavaSVN should throw exception here, but it did
  // not.
  // CommitRecord commitRecord = this.p.getCommitRecord(0);
  // if (commitRecord != null) {
  // assertEquals(0, commitRecord.getCommitRecordEntries().size());
  // }
  //    
  // //revision 1 - 9
  // for (int i = 1; i <= 9; i++) {
  // assertNotNull(this.p.getCommitRecord(i));
  // }
  //    
  // //revision 10, non-existent
  // try {
  // this.p.getCommitRecord(10);
  // fail("Revision 10 does not exist.");
  // }
  // catch (Exception ex) {
  // //ignore
  // }
  //    
  // //revision 1
  // commitRecord = this.p.getCommitRecord(1);
  // assertEquals(1L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131014866409L, commitRecord.getCommitTime().getTime());
  // Collection<CommitRecordEntry> entries =
  // commitRecord.getCommitRecordEntries();
  // assertEquals(1, entries.size());
  // //adding new file
  // CommitRecordEntry entry = entries.iterator().next();
  // assertEquals(null, entry.getFromPath());
  // assertEquals(-1, entry.getFromRevision());
  // assertEquals("/test.txt", entry.getToPath());
  // assertEquals(1, entry.getToRevision());
  // assertEquals(true, entry.isFile());
  // assertEquals(true, entry.isTextFile());
  // assertEquals(1, entry.getTotalLines());
  // assertEquals(1, entry.getLinesAdded());
  // assertEquals(0, entry.getLinesDeleted());
  //    
  // //revision 2
  // commitRecord = this.p.getCommitRecord(2);
  // assertEquals(2L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131014902982L, commitRecord.getCommitTime().getTime());
  // entries = commitRecord.getCommitRecordEntries();
  // assertEquals(1, entries.size());
  // //file rename
  // entry = entries.iterator().next();
  // assertEquals("/test.txt", entry.getFromPath());
  // assertEquals(1, entry.getFromRevision());
  // assertEquals("/test1.txt", entry.getToPath());
  // assertEquals(2, entry.getToRevision());
  // assertEquals(true, entry.isFile());
  // assertEquals(true, entry.isTextFile());
  // assertEquals(1, entry.getTotalLines());
  // assertEquals(0, entry.getLinesAdded());
  // assertEquals(0, entry.getLinesDeleted());
  //
  // //revision 4
  // commitRecord = this.p.getCommitRecord(4);
  // assertEquals(4L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131015460273L, commitRecord.getCommitTime().getTime());
  // entries = commitRecord.getCommitRecordEntries();
  // assertEquals(2, entries.size());
  // //add a directory, and rename a file.
  // boolean directoryChecked = false;
  // boolean fileChecked = false;
  // for (Iterator i = entries.iterator(); i.hasNext(); ) {
  // entry = (CommitRecordEntry) i.next();
  // if (entry.getToPath().equals("/d")) {
  // assertEquals(null, entry.getFromPath());
  // assertEquals(-1, entry.getFromRevision());
  // assertEquals("/d", entry.getToPath());
  // assertEquals(4, entry.getToRevision());
  // assertEquals(false, entry.isFile());
  // assertEquals(false, entry.isTextFile());
  // directoryChecked = true;
  // }
  // else if (entry.getToPath().equals("/d/test1.txt")) {
  // assertEquals("/test1.txt", entry.getFromPath());
  // assertEquals(2, entry.getFromRevision());
  // assertEquals("/d/test1.txt", entry.getToPath());
  // assertEquals(4, entry.getToRevision());
  // assertEquals(true, entry.isFile());
  // assertEquals(true, entry.isTextFile());
  // fileChecked = true;
  // }
  // else {
  // fail("Unexpected entry.");
  // }
  // }
  // if (!directoryChecked || !fileChecked) {
  // fail("Error in rev 4");
  // }
  //    
  // //revision 5
  // commitRecord = this.p.getCommitRecord(5);
  // assertEquals(5L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131015935056L, commitRecord.getCommitTime().getTime());
  // entries = commitRecord.getCommitRecordEntries();
  // assertEquals(1, entries.size());
  // //file modification
  // entry = entries.iterator().next();
  // assertEquals("/d/test1.txt", entry.getFromPath());
  // assertEquals(4, entry.getFromRevision());
  // assertEquals("/d/test1.txt", entry.getToPath());
  // assertEquals(5, entry.getToRevision());
  // assertEquals(true, entry.isFile());
  // assertEquals(true, entry.isTextFile());
  // assertEquals(2, entry.getTotalLines());
  // assertEquals(1, entry.getLinesAdded());
  // assertEquals(0, entry.getLinesDeleted());
  //
  // //revision 6
  // commitRecord = this.p.getCommitRecord(6);
  // assertEquals(6L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131016379795L, commitRecord.getCommitTime().getTime());
  // entries = commitRecord.getCommitRecordEntries();
  // assertEquals(1, entries.size());
  // //file deletion
  // entry = entries.iterator().next();
  // assertEquals("/t.txt", entry.getFromPath());
  // assertEquals(5, entry.getFromRevision()); //added in rev 3, not touched in
  // rev 4 and 5
  // assertEquals(null, entry.getToPath());
  // assertEquals(6, entry.getToRevision());
  // assertEquals(true, entry.isFile());
  // assertEquals(true, entry.isTextFile());
  // assertEquals(0, entry.getTotalLines());
  // assertEquals(0, entry.getLinesAdded());
  // assertEquals(3, entry.getLinesDeleted());
  //   
  // //revision 9
  // commitRecord = this.p.getCommitRecord(9);
  // assertEquals(9L, commitRecord.getRevision());
  // assertEquals(null, commitRecord.getAuthor());
  // assertEquals("", commitRecord.getMessage());
  // assertEquals(1131204918532L, commitRecord.getCommitTime().getTime());
  // entries = commitRecord.getCommitRecordEntries();
  // assertEquals(1, entries.size());
  // //binary file addition
  //    entry = entries.iterator().next();
  //    assertEquals(null, entry.getFromPath());
  //    assertEquals(-1, entry.getFromRevision());
  //    assertEquals("/d/text2binary.dat", entry.getToPath());
  //    assertEquals(9, entry.getToRevision());
  //    assertEquals(true, entry.isFile());
  //    assertEquals(false, entry.isTextFile());  
  //  }
}
