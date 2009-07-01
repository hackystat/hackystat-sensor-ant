package org.hackystat.sensor.ant.svn;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.hackystat.sensor.ant.vcs.GenericDiffCounter;
import org.hackystat.sensor.ant.vcs.GenericSizeCounter;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * A record tracing the change of one file or one directory in an SVN commit.
 * Note that the path may indication either a directory or a file. During a
 * commit a path name can change, such as in the case of renaming.
 * <p>
 * If an item is newly created in the commit, then fromPath is null. If an item
 * is deleted, then toPath is null.
 * 
 * @author (Cedric) Qin ZHANG
 * @version $Id$
 */
public class CommitRecordEntry {

  private SVNRepository svnRepository;
  private String fromPath, toPath;
  private long fromRevision, toRevision;

  private boolean statisticsComputed = false;
  private boolean isFile = false; // valid only if statisticsComputed=true
  private boolean isTextFile = false; // valid only if isFile=true
  private int linesAdded, linesDeleted, totalLines; // valid only if

  // isTextFile=true

  /**
   * Create this instance. One of fromPath and toPath can be null, but not both.
   * 
   * @param svnRepository SVN repository.
   * @param fromPath The from path.
   * @param fromRevision The from revision.
   * @param toPath The to path.
   * @param toRevision The to revision.
   * 
   * @throws Exception If both fromPath and toPath are null.
   */
  CommitRecordEntry(SVNRepository svnRepository, String fromPath, long fromRevision,
      String toPath, long toRevision) throws Exception {
    this.svnRepository = svnRepository;
    this.fromPath = fromPath;
    this.fromRevision = fromRevision;
    this.toPath = toPath;
    this.toRevision = toRevision;
    if (this.fromPath == null && this.toPath == null) {
      throw new Exception("FromPath and ToPath cannot both be null.");
    }
  }

  /**
   * Gets the from path.
   * 
   * @return The from path.
   */
  public String getFromPath() {
    return this.fromPath;
  }

  /**
   * Gets the to path.
   * 
   * @return The to path.
   */
  public String getToPath() {
    return this.toPath;
  }

  /**
   * Gets the from revision.
   * 
   * @return The from revision.
   */
  public long getFromRevision() {
    return this.fromRevision;
  }

  /**
   * Checks whether this entry represents a file or not. Note that a false
   * return value does not necessary mean this entry represents a directory.
   * 
   * @return True if this commit entry represents a file.
   * 
   * @throws Exception If there is any error.
   */
  public boolean isFile() throws Exception {
    this.computeStatistics();
    return this.isFile;
  }

  /**
   * Checks whether this entry represents a text file.
   * 
   * @return True if this commit entry represents a text file. False if either
   * this commit entry represents a binary file, or it's not a file at all.
   * 
   * @throws Exception If there is any error.
   */
  public boolean isTextFile() throws Exception {
    this.computeStatistics();
    return this.isTextFile;
  }

  /**
   * Gets the to revision.
   * 
   * @return The to revision.
   */
  public long getToRevision() {
    return this.toRevision;
  }

  /**
   * Gets the number of lines added.
   * 
   * @return The number of lines added.
   * 
   * @throws Exception If this entry does not represent a text file, or if there
   * is any other error.
   */
  public int getLinesAdded() throws Exception {
    this.computeStatistics();
    if (this.isTextFile) {
      return this.linesAdded;
    }
    else {
      throw new Exception("This is not a text file.");
    }
  }

  /**
   * Gets the number of lines deleted.
   * 
   * @return The number of lines deleted.
   * 
   * @throws Exception If this entry does not represent text a file, or if there
   * is any other error.
   */
  public int getLinesDeleted() throws Exception {
    this.computeStatistics();
    if (this.isTextFile) {
      return this.linesDeleted;
    }
    else {
      throw new Exception("This is not a text file.");
    }
  }

  /**
   * Gets the total number of lines for the toRevision.
   * 
   * @return The total number of lines.
   * 
   * @throws Exception If this entry does not represent a text file, or if there
   * is any other error.
   */
  public int getTotalLines() throws Exception {
    this.computeStatistics();
    if (this.isTextFile) {
      return this.totalLines;
    }
    else {
      throw new Exception("This is not a text file.");
    }
  }

  /**
   * Computes file metrics. Note that if the file is a binary file, either an
   * exception will be thrown or the computed value is invalid. The exact
   * behavior depends on the underlying metrics computation engine.
   * 
   * @throws Exception If this entry does not represent a file, or if there is
   * any other error.
   */
  private void computeStatistics() throws Exception {

    if (!this.statisticsComputed) {
      boolean useToPath = (this.toPath != null);

      // check isFile
      SVNNodeKind nodeKind = useToPath ? this.svnRepository.checkPath(this.toPath,
          this.toRevision) : this.svnRepository.checkPath(this.fromPath, this.fromRevision);
      this.isFile = (nodeKind == SVNNodeKind.FILE);
      // TODO: this is a hack
      // It seems that each call of checkPath method will open a new socket
      // connection,
      // If this method repeatedly called, OS will return a "address already in
      // use" exception.
      // This is underlying JavaSVN issue, the only thing I can do is to force
      // this
      // thread to sleep for some time.
      Thread.sleep(50);

      if (this.isFile) {
        // check isTextFile
        TreeMap<String, String> properties = new TreeMap<String, String>();
        if (useToPath) {
          this.getVersionedProperties(this.toPath, this.toRevision, properties);
        }
        else {
          this.getVersionedProperties(this.fromPath, this.fromRevision, properties);
        }
        String svnFileMineType = properties.get("svn:mime-type");
        this.isTextFile = (svnFileMineType == null || svnFileMineType.toLowerCase(Locale.ENGLISH)
            .startsWith("/text"));

        if (this.isTextFile) {
          byte[] content = useToPath ? this.getVersionedContent(this.toPath, this.toRevision,
              null) : this.getVersionedContent(this.fromPath, this.fromRevision, null);
          // compute diff
          byte[] fromContent = useToPath ? null : content;
          byte[] toContent = useToPath ? content : null;

          if (fromContent == null && this.fromPath != null) {
            fromContent = this.getVersionedContent(this.fromPath, this.fromRevision, null);
          }

          if (toContent == null && this.toPath != null) {
            toContent = this.getVersionedContent(this.toPath, this.toRevision, null);
          }

          String[] fromStrings = fromContent == null ? null : this
              .byteArrayToStringArray(fromContent);
          String[] toStrings = toContent == null ? null : this
              .byteArrayToStringArray(toContent);

          if (fromStrings == null) {
            this.totalLines = new GenericSizeCounter(toStrings).getNumOfTotalLines();
            this.linesAdded = this.totalLines;
            this.linesDeleted = 0;
          }
          else if (toStrings == null) {
            this.totalLines = 0;
            this.linesAdded = 0;
            this.linesDeleted = new GenericSizeCounter(fromStrings).getNumOfTotalLines();
          }
          else {
            this.totalLines = new GenericSizeCounter(toStrings).getNumOfTotalLines();
            GenericDiffCounter diff = new GenericDiffCounter(fromStrings, toStrings);
            this.linesAdded = diff.getLinesAdded();
            this.linesDeleted = diff.getLinesDeleted();
          }

        } // this.isTextFile
      } // this.isFile

      this.statisticsComputed = true;
    } // this.isStatisticsComputed
  }

  /**
   * Gets the content for a file at the specified revision.
   * @param filePath The file path.
   * @param revision The revision number.
   * @param properties A map to receive SVN properties associated with the file.
   * Note that this is an output parameter, it's advised that you use an empty
   * map. Null is a valid value.
   * 
   * @return The content as a byte array.
   * @throws Exception If there is any error.
   */
  private byte[] getVersionedContent(String filePath, long revision,
      Map<String, String> properties) throws Exception {
    ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
    SVNProperties svnProps = SVNProperties.wrap(properties);
    this.svnRepository.getFile(filePath, revision, svnProps, output);
    output.flush();
    return output.toByteArray();
  }

  /**
   * Gets the properties for a file at the specified revision.
   * @param filePath The file path.
   * @param revision The revision number.
   * @param properties A map to receive SVN properties associated with the file.
   * Note that this is an output parameter, it's advised that you use an empty
   * map. Null is a valid value.
   * @throws Exception If there is any error.
   */
  private void getVersionedProperties(String filePath, long revision,
      Map<String, String> properties) throws Exception {
    SVNProperties svnProps = SVNProperties.wrap(properties);
    this.svnRepository.getFile(filePath, revision, svnProps, null);
  }

  /**
   * Converts a byte array holding text into a string array with each string
   * represeting one line. TODO: check shits like unicode, text locale, and line
   * break with \r, \n or both!
   * 
   * @param byteArray A byte array holding text.
   * 
   * @return The converted string array. Note that if you pass in a byte array
   * containing binary contents, then the return value is undefined.
   * 
   * @throws Exception If there is any error.
   */
  private String[] byteArrayToStringArray(byte[] byteArray) throws Exception {
    List<String> strings = new ArrayList<String>(16);
    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
        byteArray)));
    String str = reader.readLine();
    while (str != null) {
      strings.add(str);
      str = reader.readLine();
    }
    // convert to vanilla string array
    int size = strings.size();
    String[] strs = new String[size];
    for (int i = 0; i < size; i++) {
      strs[i] = strings.get(i);
    }
    return strs;
  }

  /**
   * Gets a string representation of this instance.
   * 
   * @return The string representation.
   */
  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer(64);
    buff.append("CommitRecordEntry (");
    buff.append(this.fromPath).append('[').append(this.fromRevision).append("] ==> ");
    buff.append(this.toPath).append('[').append(this.toRevision).append("]) ");
    return buff.toString();
  }
}