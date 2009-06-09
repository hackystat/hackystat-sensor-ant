package org.hackystat.sensor.ant.issue;

import java.util.Date;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * A record of an issue change event.
 * @author Shaoxuan
 *
 */
public class IssueEvent {

  private String uri;
  private String link;
  private int id;
  private String type;
  private String status;
  private String priority;
  private String milestone;
  private String owner;
  private int updateNumber = 0;
  private Date updatedDate;
  private String comment;

  
  private static final String googleIssueFeedContentPrefix = "<pre>";
  private static final String googleIssueFeedContentSuffix = "</pre>";
  private static final String statusPrefix = "Status:";
  private static final String labelPrefix = "Label:";
  
  /**
   * Create an issue event instance with the information extract from the given entry.
   * @param entry a SyndEntryImpl.
   * @throws Exception if error occur.
   */
  public IssueEvent(SyndEntryImpl entry) throws Exception {
    this.owner = entry.getAuthor();
    this.uri = entry.getUri();
    this.link = entry.getLink();
    this.updatedDate = entry.getUpdatedDate();
    if (entry.getContents().size() != 1) {
      throw new Exception("Unsupport: There are more than one contents in the entry.");
    }
    //[1] extract issue number and update number from title
    String[] titleWords = entry.getTitle().trim().split(" ");
    if (titleWords.length > 5 && 
        titleWords[0].equals("Update") && titleWords[3].equals("issue")) { //Update
      this.updateNumber = Integer.valueOf(titleWords[1]); 
      this.id = Integer.valueOf(titleWords[4]);
    }
    else if (titleWords.length > 3 && 
        titleWords[0].equals("Issue") && titleWords[2].equals("created:")) { //Create
      status = "Created";
      //TODO should extract more detail from the issue page.
      this.id = Integer.valueOf(titleWords[1]);
    }
    
    //[2] extract comment and status from content
    SyndContent syndContent = (SyndContent)entry.getContents().get(0);
    String content = syndContent.getValue().replace(googleIssueFeedContentPrefix, "").
                                            replace(googleIssueFeedContentSuffix, "").trim();
    content = content.replace("<br/>", " ");
    if (updateNumber > 0) {
      //extract status, which is the word following Status:, and comment.
      if (content.contains(statusPrefix) || content.contains(labelPrefix) ) {
        int statusStartIndex = content.lastIndexOf(statusPrefix);
        statusStartIndex = (statusStartIndex == -1) ? content.length() - 1 : statusStartIndex;
        int labelStartIndex = content.lastIndexOf(labelPrefix);
        labelStartIndex = (labelStartIndex == -1) ? content.length() - 1 : labelStartIndex;
        //String label = 
        //  content.substring(labelStartIndex + labelPrefix.length(), content.length() - 1);
        //parseLabel(label);
        comment = content.substring(0, 
            (statusStartIndex < labelStartIndex ? statusStartIndex : labelStartIndex));
        int statusEndIndex = content.indexOf(" ", statusStartIndex + statusPrefix.length() + 1);
        statusEndIndex = statusEndIndex == -1 ? content.length() - 1 : statusEndIndex;
        status = content.substring(statusStartIndex + statusPrefix.length(), statusEndIndex).trim();
      }
      else {
        comment = content;
      }
    }
    else {
      comment = content;
    }
    
  }

  /**
   * Gets a string representation of this instance.
   * 
   * @return The string representation.
   */
  @Override
  public String toString() {
    return "Issue " + this.getId() + ", Update " + this.getUpdateNumber() + 
    ", Author=" + this.getOwner() + ", status=" + status + ", updateDate=" + 
    this.getUpdatedDate() + ", uri=" + this.getUri();
  }
  
  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @return the owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * @return the updatedDate
   */
  public Date getUpdatedDate() {
    return new Date(updatedDate.getTime());
  }

  /**
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * @return the uri
   */
  public String getUri() {
    return uri;
  }

  /**
   * @return the link
   */
  public String getLink() {
    return link;
  }

  /**
   * @return the updateNumber
   */
  public int getUpdateNumber() {
    return updateNumber;
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
   * @param id the id to set
   */
  protected void setId(int id) {
    this.id = id;
  }
  
}
