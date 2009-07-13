package org.hackystat.sensor.ant.issue;

import java.util.Arrays;
import java.util.List;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.utilities.tstamp.Tstamp;

/**
 * An IssueEntry represent an issue in issue tracking system, always reflect the most current state.
 * @author Shaoxuan Zhang
 *
 */
public class IssueEntry {

  /** property key of ID. */
  public static final String ID_PROPERTY_KEY = "Id";
  /** property key of TYPE. */
  public static final String TYPE_PROPERTY_KEY = "Type";
  /** property key of STATUS. */
  public static final String STATUS_PROPERTY_KEY = "Status";
  /** property key of PRIORITY. */
  public static final String PRIORITY_PROPERTY_KEY = "Priority";
  /** property key of MILESTONE. */
  public static final String MILESTONE_PROPERTY_KEY = "Milestone";
  /** property key of OWNER. */
  public static final String OWNER_PROPERTY_KEY = "Owner";

  /** timestamp separator in property value. */
  public static final String TIMESTAMP_SEPARATOR = "--";
  
  private int id;
  private String type = "";
  private String status = "";
  private String priority = "";
  private String milestone = "";
  private String owner = "";
  /** open time of this issue, or the earliest time this issue being detected.*/
  private XMLGregorianCalendar openedTime = null;
  /** close time of this issue, null if this issue is still open. */
  private XMLGregorianCalendar closedTime = null;
  private XMLGregorianCalendar modifiedTime = null;
  
  private SensorData sensorData = null;
  
  /**
   * Update this IssueEntry as well as the associated sensordata to the given issue table column.
   * @param line the content of the issue table column.
   * @param runTimestamp the run time.
   * @return true if the sensordata is modified.
   */
  public boolean upToDate(String[] line, XMLGregorianCalendar runTimestamp) {
    boolean modified = false;
    if (this.type == null || this.type.equals(line[1])) {
      this.type = line[1];
      sensorData.addProperty(TYPE_PROPERTY_KEY, line[1] + TIMESTAMP_SEPARATOR + runTimestamp);
      modified = true;
    }
    if (this.status == null || this.status.equals(line[2])) {
      this.status = line[2];
      sensorData.addProperty(STATUS_PROPERTY_KEY, line[2] + TIMESTAMP_SEPARATOR + runTimestamp);
      modified = true;
    }
    if (this.priority == null || this.priority.equals(line[3])) {
      this.priority = line[3];
      sensorData.addProperty(PRIORITY_PROPERTY_KEY, line[3] + TIMESTAMP_SEPARATOR + runTimestamp);
      modified = true;
    }
    if (this.milestone == null || this.milestone.equals(line[4])) {
      this.milestone = line[4];
      sensorData.addProperty(MILESTONE_PROPERTY_KEY, line[4] + TIMESTAMP_SEPARATOR + runTimestamp);
      modified = true;
    }
    if (this.owner == null || this.owner.equals(line[5])) {
      this.owner = line[5];
      sensorData.addProperty(OWNER_PROPERTY_KEY, line[5] + TIMESTAMP_SEPARATOR + runTimestamp);
      modified = true;
    }
    if (modified) {
      sensorData.setLastMod(runTimestamp);
    }
    return modified;
  }

  /**
   * @param data The associated SensorData
   * @throws Exception if error
   */
  public IssueEntry(final SensorData data) throws Exception {
    this.sensorData = data;
    //get id
    for (Property property : data.getProperties().getProperty()) {
      if (ID_PROPERTY_KEY.equals(property.getKey())) {
        this.id = Integer.valueOf(property.getValue());
        break;
      }
    }
    //get latest type
    String type = this.getLatestValueWithKey(TYPE_PROPERTY_KEY);
    if (type != null) {
      this.type = type;
    }
    //get latest status
    String status = this.getLatestValueWithKey(STATUS_PROPERTY_KEY);
    if (status != null) {
      this.status = status;
    }
    //get latest priority
    String priority = this.getLatestValueWithKey(PRIORITY_PROPERTY_KEY);
    if (priority != null) {
      this.priority = priority;
    }
    //get latest milestone
    String milestone = this.getLatestValueWithKey(MILESTONE_PROPERTY_KEY);
    if (milestone != null) {
      this.milestone = milestone;
    }
    //get latest owner
    String owner = this.getLatestValueWithKey(OWNER_PROPERTY_KEY);
    if (owner != null) {
      this.owner = owner;
    }
  }

  /**
   * Get the last update time of this issue sensor data.
   * If LastModification time is available, it will be return.
   * Otherwise, it will process through properties to find the 
   * lastest timestamp in properties' timestamp.
   * @return the timestamp of last udpate. NULL if no relative information found.
   */
  public XMLGregorianCalendar getLastUpdateTime() {
    if (sensorData.getLastMod() != null) {
      return sensorData.getLastMod();
    }
    XMLGregorianCalendar timestamp = null;
    List<String> keys = Arrays.asList(new String[]{TYPE_PROPERTY_KEY, STATUS_PROPERTY_KEY, 
        PRIORITY_PROPERTY_KEY, MILESTONE_PROPERTY_KEY, OWNER_PROPERTY_KEY});
    for (Property property : sensorData.getProperties().getProperty()) {
      if (keys.contains(property.getKey())) {
        try {
          XMLGregorianCalendar valueTimestamp = extractTimestamp(property.getValue());
          if (timestamp == null || Tstamp.greaterThan(valueTimestamp, timestamp)) {
            timestamp = valueTimestamp;
          }
        }
        catch (Exception e) {
          System.out.println("Error when extracting timestamp from " + property.getValue() +
              " Exception message: " + e.getMessage());
        }
      }
    }
    return timestamp;
  }
  
  /**
   * Extract timestamp from formatted string.
   * @param value the string.
   * @return the timestamp.
   * @throws Exception if the string is not formatted.
   */
  private static XMLGregorianCalendar extractTimestamp(String value) throws Exception {
    int startIndex = value.indexOf(TIMESTAMP_SEPARATOR) + TIMESTAMP_SEPARATOR.length();
    return Tstamp.makeTimestamp(value.substring(startIndex));
  }

  /**
   * Extract value from formatted string.
   * @param string the string.
   * @return the value.
   * @throws Exception if the string is not formatted.
   */
  private static String extractValue(String string) throws Exception {
    return string.substring(0, string.indexOf(TIMESTAMP_SEPARATOR));
  }
  
  /**
   * Return the latest value with the given key.
   * @param key the property key
   * @return the latest value, null if not found.
   * @throws Exception if error when parsing property values.
   */
  public final String getLatestValueWithKey(String key) throws Exception {
    XMLGregorianCalendar latestTime = null;
    String value = null;
    for (Property property : sensorData.getProperties().getProperty()) {
      if (key.equals(property.getKey())) {
        XMLGregorianCalendar newTimestamp = extractTimestamp(property.getValue());
        if (latestTime == null || 
            latestTime.compare(newTimestamp) == DatatypeConstants.LESSER) {
          latestTime = newTimestamp;
          value = extractValue(property.getValue());
        }
      }
    }
    return value;
  }

  
  /**
   * @param id the id to set
   */
  protected void setId(int id) {
    this.id = id;
  }
  /**
   * @return the id
   */
  public int getId() {
    return id;
  }
  /**
   * @param type the type to set
   */
  protected void setType(String type) {
    this.type = type;
  }
  /**
   * @return the type
   */
  public String getType() {
    return type;
  }
  /**
   * @param status the status to set
   */
  protected void setStatus(String status) {
    this.status = status;
  }
  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }
  /**
   * @param priority the priority to set
   */
  protected void setPriority(String priority) {
    this.priority = priority;
  }
  /**
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }
  /**
   * @param milestone the milestone to set
   */
  protected void setMilestone(String milestone) {
    this.milestone = milestone;
  }
  /**
   * @return the milestone
   */
  public String getMilestone() {
    return milestone;
  }
  /**
   * @param owner the owner to set
   */
  protected void setOwner(String owner) {
    this.owner = owner;
  }
  /**
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }
  /**
   * @param openedTime the openedTime to set
   */
  protected void setOpenedTime(XMLGregorianCalendar openedTime) {
    this.openedTime = openedTime;
  }
  /**
   * @return the openedTime
   */
  public XMLGregorianCalendar getOpenedTime() {
    return openedTime;
  }
  /**
   * @param closedTime the closedTime to set
   */
  protected void setClosedTime(XMLGregorianCalendar closedTime) {
    this.closedTime = closedTime;
  }
  /**
   * @return the closedTime
   */
  public XMLGregorianCalendar getClosedTime() {
    return closedTime;
  }

  /**
   * @param sensorData the sensorData to set
   */
  protected void setSensorData(SensorData sensorData) {
    this.sensorData = sensorData;
  }

  /**
   * @return the sensorData
   */
  public SensorData getSensorData() {
    return sensorData;
  }


  /**
   * @param modifiedTime the modifiedTime to set
   */
  protected void setModifiedTime(XMLGregorianCalendar modifiedTime) {
    this.modifiedTime = modifiedTime;
  }


  /**
   * @return the modifiedTime
   */
  public XMLGregorianCalendar getModifiedTime() {
    return modifiedTime;
  }


}
