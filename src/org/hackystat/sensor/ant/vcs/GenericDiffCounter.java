package org.hackystat.sensor.ant.vcs;

import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.Revision;


/**
 * Diff counter, which wraps around JRCS diff engine. <p>
 * Note that if this class is used to diff two text files, then empty lines and
 * comments are considered in diff result. This may or may not be desired.
 * Also note that if you try to diff two binary files, you either get an exception or
 * an incorrect result.
 * 
 * @author Qin ZHANG
 * @version $Id: GenericDiffCounter.java,v 1.1.1.1 2005/10/20 23:56:56 johnson Exp $
 */
public class GenericDiffCounter {

  private int linesAdded = 0;
  private int linesDeleted = 0;
  
  /**
   * Constructs this instance to diff two revisions.
   *
   * @param original The original version.
   * @param revised The revision.
   * @throws Exception If there is error in diff engine.
   */
  public GenericDiffCounter(Object[] original, Object[] revised) throws Exception {
    Diff dfEngine = new Diff(original);
    Revision revision = dfEngine.diff(revised);

    for (int i = 0; i < revision.size(); i++) {
      Delta delta = revision.getDelta(i);
      Chunk fromChunk = delta.getOriginal();
      Chunk toChunk = delta.getRevised();
      this.linesDeleted += fromChunk.size();
      this.linesAdded += toChunk.size();
    }

    //Following is redundant, it only serves to check whether diff is correct.
    //Since the version of JRCS used is not final, double-checking is always good.
    Object[] reco = revision.patch(original);
    if (!Diff.compare(revised, reco)) {
      throw new Exception("JRCS Diff internal error: files differ after patching.");
    }
    //END OF redundant code
  }

  /**
   * Gets the lines added.
   *
   * @return The number of lines added.
   */
  public int getLinesAdded() {
    return this.linesAdded;
  }

  /**
   * Gets the lines deleted.
   *
   * @return The number of lines deleted.
   */
  public int getLinesDeleted() {
    return this.linesDeleted;
  }
}
