package org.hackystat.sensor.ant.svn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * A commit record tracks all items that get modified during an SVN revision.
 * Note that in SVN, versioning is performed on the entire repository instead of
 * a single file.
 * 
 * @author (Cedric) Qin ZHANG
 * @version $Id$
 */
public class CommitRecord {

  private SVNRepository svnRepository;
  private SVNLogEntry svnLogEntry;
  private List<CommitRecordEntry> commitRecordEntries = new ArrayList<CommitRecordEntry>();

  /**
   * Constructs this instance.
   * 
   * @param svnRepository The svn repository.
   * @param svnLogEntry The svn log for a revision.
   * 
   * @throws Exception If there is any error.
   */
  CommitRecord(SVNRepository svnRepository, SVNLogEntry svnLogEntry) throws Exception {
    this.svnRepository = svnRepository;
    this.svnLogEntry = svnLogEntry;
    this.processSvnLogEntry();
  }

  /**
   * Process SVN log to extract information about which file/directory gets
   * modified in this revision.
   * 
   * @throws Exception If there is any error.
   */
  private void processSvnLogEntry() throws Exception {
    long currentRevision = this.getRevision();

    // They are used to handle file or directory renaming.
    TreeMap<String, SVNLogEntryPath> copyPaths = new TreeMap<String, SVNLogEntryPath>();
    TreeSet<String> deletePaths = new TreeSet<String>();

    for (Object entryValue : this.svnLogEntry.getChangedPaths().values()) {
      SVNLogEntryPath changedPath = (SVNLogEntryPath) entryValue;
      String path = changedPath.getPath();
      String copyPath = changedPath.getCopyPath(); // null if no copy path.

      char changeType = changedPath.getType();
      if (changeType == 'A') {
        // New file added, if copyPath == null.
        // File rename, and possibly changed, then copyPath != null and there is
        // a 'D' entry.
        // (It seems there is always a 'D' entry')
        // What about resurect of previously deleted file?
        if (copyPath == null) { // newly created
          this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository, null, -1,
              path, currentRevision));
        }
        else { // file rename, check for 'D'
          copyPaths.put(copyPath, changedPath);
        }
      }
      else if (changeType == 'D') {
        // Delete an existing file, copyPath == null
        // Note: Node kind is alway "none", we don't know it's a file or
        // direcotry.
        deletePaths.add(path); // process later, some are deletion, some are
        // renaming.
      }
      else if (changeType == 'M') {
        // Modify existing file, copyPath == null
        // Note, if you rename a top directory, all files in the directory will
        // be moified
        // with copyPath == null, even if there is no change in those files.
        // Find out the file name in the previous revision, which might have
        // been changed.

        if (SVNNodeKind.FILE == this.svnRepository.checkPath(path, currentRevision)) {
          Collection<?> revisions = this.svnRepository.getFileRevisions(path, null, 1,
              currentRevision);
          // we should alway find at least one, since change type is 'M'.
          if (revisions == null || revisions.isEmpty()) {
            throw new RuntimeException("Inconsistent SVN record. Corrupted SVN repository?");
          }
          long thePrevRevisionNumber = -1;
          SVNFileRevision thePrevFileRevision = null;
          for (Object revision : revisions) {
            SVNFileRevision curRevision = (SVNFileRevision) revision;
            long curRevisionNumber = curRevision.getRevision();
            if (thePrevRevisionNumber < curRevisionNumber
                && curRevisionNumber < currentRevision) {
              thePrevRevisionNumber = curRevisionNumber;
              thePrevFileRevision = curRevision;
            }
          }
          if (thePrevFileRevision == null) {
            throw new RuntimeException("Assertion Failed.");
          }
          this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository,
              thePrevFileRevision.getPath(), thePrevFileRevision.getRevision(), path,
              currentRevision));
        }
      }
      else if (changeType == 'R') {
        // Replace, the object is first deleted, and another with the same name
        // added,
        // all within single revision (Note: I CANNOT produce this in SVN).
        // So, it's actually a deletion plus an addition.
        this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository, path,
            currentRevision - 1, null, currentRevision));
        this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository, null, -1, path,
            currentRevision));
      }
      else {
        throw new RuntimeException("Unknown SVN change type.");
      }
    }

    // handle delete and file rename
    for (String deletePath : deletePaths) {
      SVNLogEntryPath addLogEntryPath = copyPaths.get(deletePath);
      if (addLogEntryPath == null) { // true delete
        this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository, deletePath,
            currentRevision - 1, null, currentRevision));
      }
      else { // rename
        this.commitRecordEntries.add(new CommitRecordEntry(this.svnRepository, addLogEntryPath
            .getCopyPath(), addLogEntryPath.getCopyRevision(), addLogEntryPath.getPath(),
            currentRevision));
      }
    }
  }

  /**
   * Gets this revision number.
   * 
   * @return The revision number.
   */
  public final long getRevision() {
    return this.svnLogEntry.getRevision();
  }

  /**
   * Gets the author who made the commit.
   * 
   * @return The authoer.
   */
  public String getAuthor() {
    return this.svnLogEntry.getAuthor();
  }

  /**
   * Gets the commit time.
   * 
   * @return The commit time.
   */
  public Date getCommitTime() {
    return this.svnLogEntry.getDate();
  }

  /**
   * Gets the commit log message.
   * 
   * @return The commit log message.
   */
  public String getMessage() {
    return this.svnLogEntry.getMessage();
  }

  /**
   * Returns the string representation of this record.
   * @return the string representation.
   */
  public String toString() {
    return "Author=" + this.getAuthor() + ", Message=" + this.getMessage() + ", CommitTime="
        + this.getCommitTime();
  }

  /**
   * Gets all changed items in this revision.
   * 
   * @return A collection of <code>CommitRecordEntry</code> instances.
   */
  public Collection<CommitRecordEntry> getCommitRecordEntries() {
    return this.commitRecordEntries;
  }
}
