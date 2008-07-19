package org.hackystat.sensor.ant.perforce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Provides a data structure with information about each file affected by a ChangeList and 
 * the number of lines added, deleted, and modified. 
 * @author Philip Johnson
 */
public class PerforceChangeListData {
  /** A list of instances indicating info about a single file. */
  private List<PerforceFileData> fileDataList = new ArrayList<PerforceFileData>();
  /** The perforce user who is the owner of this changelist. */
  private String owner;
  /** The integer ID for this changelist. */
  private int id;
  /** When this changelist was submitted. */
  private Date modTime;
  /** Formatter for parsing the date string returned by perforce. */
  private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
  
  /** Disable public no-arg constructor. */
  @SuppressWarnings("unused")
  private PerforceChangeListData() {
    // Nothing.
  }

  
  /** 
   * Create a new instance with the specified owner. 
   * @param owner The owner who committed this changelist.
   * @param id The id for this changelist. 
   * @param modTime The time this changelist was submitted.
   */
  public PerforceChangeListData(String owner, int id, String modTime) {
    this.owner = owner;
    this.id = id;
    try {
      this.modTime = format.parse(modTime);
    }
    catch (Exception e) {
      System.out.println("Could not parse date returned from Perforce: " + modTime);
      System.out.println("Defaulting to today's date");
      this.modTime = new Date();
    }
  }


  /**
   * Adds the Perforce file data information to this Changelist.
   * @param fileName The file name.  
   * @param linesAdded The lines added.
   * @param linesModified The lines modified. 
   * @param linesDeleted  The lines deleted.
   * @param totalLoc The total number of lines in the file.  
   */
  public void addFileData(String fileName, int linesAdded, int linesDeleted, int linesModified,
      int totalLoc) {
    this.fileDataList.add(new PerforceFileData(fileName, linesAdded, linesDeleted, linesModified,
        totalLoc));
  }
  
  /**
   * Returns as list of PerforceFileData instances associated with this changelist. 
   * @return The list of PerforceFileData instances. 
   */
  public List<PerforceFileData> getFileData() {
    return this.fileDataList;
  }
  
  /**
   * Returns the owner of this Changelist. 
   * @return The owner. 
   */
  public String getOwner() {
    return this.owner;
  }
  
  /**
   * Returns the ID associated with this changelist. 
   * @return The id.
   */
  public int getId() {
    return this.id;
  }
  
  /**
   * Returns the date on which this Changelist was submitted. 
   * @return The date on which this Changelist was submitted. 
   */
  public Date getModTime () {
    return (Date)this.modTime.clone();
  }
  
  /**
   * Returns this changelist in a nicely formatted output for debugging purposes.
   * @return The changelist as a string. 
   */
  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer();
    String header = String.format("[ChangeList %d %s %s ", this.id, this.modTime, this.owner);
    buff.append(header);
    for (PerforceFileData fileData : getFileData()) {
      String dataString = String.format("(%s %d %d %d %d) ", fileData.getFileName(),
          fileData.getLinesAdded(), fileData.getLinesDeleted(), fileData.getLinesModified(),
          fileData.getTotalLines());
      buff.append(dataString);
    }
    buff.append(']');
    return buff.toString();
  }

  /**
   * Inner class that provides information on a single file. 
   * @author Philip Johnson
   */
  public static class PerforceFileData {
    private String fileName;
    private int linesAdded;
    private int linesModified;
    private int linesDeleted;
    private int totalLoc;

    /**
     * Create a record with info about the given file. 
     * @param fileName The name.
     * @param linesAdded Lines added.
     * @param linesDeleted Lines deleted.
     * @param linesModified Lines modified.
     * @param totalLoc The total loc.
     */
    public PerforceFileData(String fileName, int linesAdded, int linesDeleted, int linesModified,
        int totalLoc) {
      this.fileName = fileName;
      this.linesAdded = linesAdded;
      this.linesDeleted = linesDeleted;
      this.linesModified = linesModified;
      this.totalLoc = totalLoc;
    }
    
    /**
     * Returns the file name.
     * @return The file name.
     */
    public String getFileName() {
      return this.fileName;
    }
    
    /**
     * Returns the lines added. 
     * @return The lines added.
     */
    public int getLinesAdded() {
      return this.linesAdded; 
    }
    
    /**
     * Returns the lines deleted. 
     * @return The lines deleted.
     */
    public int getLinesDeleted() {
      return this.linesDeleted;
    }
    
    /**
     * Returns the lines modified. 
     * @return The lines modified.
     */
    public int getLinesModified() {
      return this.linesModified;
    }
    
    /**
     * Returns the total number of lines.
     * @return The total lines. 
     */
    public int getTotalLines() {
      return this.totalLoc;
    }
  }

}
