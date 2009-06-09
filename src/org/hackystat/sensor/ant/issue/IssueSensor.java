package org.hackystat.sensor.ant.issue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hackystat.sensorshell.SensorShell;
import org.hackystat.sensorshell.SensorShellException;
import org.hackystat.sensorshell.SensorShellProperties;
import org.hackystat.sensorshell.usermap.SensorShellMap;
import org.hackystat.sensorshell.usermap.SensorShellMapException;
import org.hackystat.utilities.time.period.Day;
import org.hackystat.utilities.tstamp.Tstamp;
import au.com.bytecode.opencsv.CSVReader;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Ant task to retieve the issue changes and send those information to Hackystat
 * server.
 * 
 * @author Shaoxuan Zhang
 *
 */
public class IssueSensor extends Task {
  
  private static final String SENSOR_DATA_TYPE = "Issue";
  private String tool = "GoogleProjectHosting";
  private String projectName;
  private String defaultHackystatAccount = "";
  private String defaultHackystatPassword = "";
  private String defaultHackystatSensorbase = "";
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
  private String fromDateString, toDateString;
  private Date fromDate, toDate;
  private boolean isVerbose = false;
  private String lastIntervalInMinutesString = "";
  private int lastIntervalInMinutes;
  

  private int idCsvIndex = 0; 
  private int typeCsvIndex = 1; 
  private int statusCsvIndex = 2; 
  private int priorityCsvIndex = 3; 
  private int milestoneCsvIndex = 4; 
  private int ownerCsvIndex = 5; 
  //private int summaryCsvIndex = 6; 
  //private int openedCsvIndex = 7; 
  //private int closedCsvIndex = 8;
  /**
   * Extracts issue information from feeds, and sends them to the
   * Hackystat server.
   * 
   * @throws BuildException If the task fails.
   */
  @Override
  public void execute() throws BuildException {
    this.validateProperties(); // sanity check.
    if (this.isVerbose) {
      System.out.printf("Processing issue updates for %s between %s (exclusive) and " +
          "%s (inclusive)%n", this.getFeedUrl(), this.fromDate, this.toDate);
    }

    try {
      Map<String, SensorShell> shellCache = new HashMap<String, SensorShell>();
      SensorShellMap shellMap = new SensorShellMap(this.tool);
      if (this.isVerbose) {
        System.out.println("Checking for user maps at: " + shellMap.getUserMapFile());
        System.out.println("Issue accounts found: " + shellMap.getToolAccounts(this.tool));
      }
      
      // Runtime timestamp.
      String runTimeString = Tstamp.makeTimestamp().toString();
      
      // Extract issue events from Google Project Hosting.
      List<IssueEvent> issueEvents = this.getIssueEvents(this.fromDate, this.toDate);
      
      // Extract additional data from Issue CSV table.
      this.checkDataWithCsv(issueEvents);
      
      // Send data to SensorBase.
      int eventsAdded = 0;
      for (IssueEvent event : issueEvents) {
          if (this.isVerbose) {
            System.out.println("Retrieved Issue data: " + event.toString());
          }
          //issueEvents.add(event);
          SensorShell shell = this.getShell(shellCache, shellMap, event.getOwner());
          if (shell != null) {
            Map<String, String> pMap = new HashMap<String, String>();
            String timestampString = 
              Tstamp.makeTimestamp(event.getUpdatedDate().getTime()).toString();
            pMap.put("SensorDataType", SENSOR_DATA_TYPE);
            pMap.put("Resource", event.getUri());
            pMap.put("Tool", this.tool);
            pMap.put("Timestamp", timestampString);
            pMap.put("Runtime", runTimeString);
            pMap.put("id", String.valueOf(event.getId()));
            pMap.put("updateNumber", String.valueOf(event.getUpdateNumber()));
            pMap.put("status", event.getStatus());
            pMap.put("link", event.getLink());
            pMap.put("comment", event.getComment());
            shell.add(pMap);
            if (this.isVerbose) {
              System.out.printf("Sending SVN Commit: Timestamp: %s Resource: %s User: %s%n", 
                  timestampString, event.getUri(), shell.getProperties().getSensorBaseUser());
            }
            eventsAdded++;
        }
      }
      if (this.isVerbose) {
        System.out.println("Found " + eventsAdded + " issue records.");
      }
      
      // Send the sensor data after all entries have been processed.
      for (SensorShell shell : shellCache.values()) {
        if (this.isVerbose) {
          System.out.println("Sending data to " + shell.getProperties().getSensorBaseUser() + 
              " at " + shell.getProperties().getSensorBaseHost());
        }
        shell.send();
        shell.quit();
      }
    }
    catch (SensorShellMapException e) {
      throw new BuildException("Error whenretrieving the shell instance.", e);
    }
    catch (SensorShellException e) {
      throw new BuildException("SensorShell error.", e);
    }
    catch (IOException e) {
      throw new BuildException("IO error.", e);
    }
  }

  /**
   * Check data with the information from issue list table, which use CSV format.
   * @param issueEvents the issue events to be checked.
   * @throws IOException if errors.
   */
  private void checkDataWithCsv(List<IssueEvent> issueEvents) throws IOException {
    URL url = new URL(this.getOpenCsvUrl());
    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    Reader reader = new InputStreamReader(url.openStream());
    
    CSVReader csvReader = new CSVReader(reader);
    
    
    String[] line = csvReader.readNext();
    
    
    //sort the list by reverse time order. 
    Collections.sort(issueEvents, new Comparator<IssueEvent> () {
      public int compare(IssueEvent o1, IssueEvent o2) {
        // TODO Auto-generated method stub
        return -o1.getUpdatedDate().compareTo(o2.getUpdatedDate());
      }
    });
    System.out.println("Checking data with issue list table.");
    while ((line = csvReader.readNext()) != null && line.length > 1) {
      
      for (String l : line) {
        System.out.print(l + ", ");
      }
      System.out.println();
      
      int id = Integer.valueOf(line[idCsvIndex]);
      for (IssueEvent issueEvent : issueEvents) {
        //find the latest one with the same id and put the information in, then next entry.
        if (issueEvent.getId() == id) {
          issueEvent.setOwner(line[this.ownerCsvIndex]);
          issueEvent.setMilestone(line[this.milestoneCsvIndex]);
          issueEvent.setOwner(line[this.ownerCsvIndex]);
          issueEvent.setPriority(line[this.priorityCsvIndex]);
          issueEvent.setStatus(line[this.statusCsvIndex]);
          issueEvent.setType(line[this.typeCsvIndex]);
          break;
        }
      }
      
    }


    
    //BufferedReader bufferedReader = new BufferedReader(reader);
  }
  
  /**
   * Extract issue events from Google Project Hosting with the given time period.
   * @param fromDate the start date of the time period.
   * @param toDate the end date of the time period.
   * @return a List of IssueEvent.
   */
  protected List<IssueEvent> getIssueEvents(Date fromDate, Date toDate) {
    List<IssueEvent> issueEvents = new ArrayList<IssueEvent>();
    try {
    // Prepare the feed.
    if (this.isVerbose) {
      System.out.println("Retrieving data from " + this.getFeedUrl());
    }
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(new XmlReader(new URL(getFeedUrl())));
    if (this.isVerbose) {
      System.out.println("Done.");
    }
    // Parse the feed
    //List<IssueEvent> issueEvents = new ArrayList<IssueEvent>();
    for (Object o : feed.getEntries()) {
      SyndEntryImpl entry = (SyndEntryImpl)o;
      if (entry.getUpdatedDate().compareTo(this.fromDate) >= 0 && 
          entry.getUpdatedDate().compareTo(this.toDate) <= 0) {
        IssueEvent event = new IssueEvent(entry);
        issueEvents.add(event);
      }
    }
    }
    catch (MalformedURLException e) {
      throw new BuildException("The feed URL specifies an unknown protocol.", e);
    }
    catch (IOException e) {
      throw new BuildException("There is a problem reading the stream of the URL.", e);
    }
    catch (FeedException e) {
      throw new BuildException("The feed could not be parsed.", e);
    }
    catch (Exception e) {
      throw new BuildException("IO error when extract issue event from feed entry.", e);
    }
    return issueEvents;
  }
  
  /**
   * Checks and make sure all properties are set up correctly.
   * 
   * @throws BuildException If any error is detected in the property setting.
   */
  protected void validateProperties() throws BuildException {
    if (this.getFeedUrl() == null || this.getFeedUrl().length() == 0) {
      throw new BuildException("Attribute 'feedUrl' must be set.");
    }
    // If lastIntervalInMinutes is set, then we define fromDate and toDate appropriately and return.
    if (!this.lastIntervalInMinutesString.equals("")) {
      try {
        this.lastIntervalInMinutes = Integer.parseInt(this.lastIntervalInMinutesString);
        long now = (new Date()).getTime();
        this.toDate = new Date(now);
        long intervalMillis = 1000L * 60 * this.lastIntervalInMinutes;
        this.fromDate = new Date(now - intervalMillis);
        return;
      }
      catch (Exception e) {
        throw new BuildException("Attribute 'lastIntervalInMinutes' must be an integer.", e);
      }
    }

    // If lastIntervalInMinutes, fromDate, and toDate not set, we extract commit information for
    // the previous 25 hours. (This ensures that running the sensor as part of a daily build
    // should have enough "overlap" to not miss any entries.)
    // Then return.
    if (this.fromDateString == null && this.toDateString == null) {
      long now = (new Date()).getTime();
      this.toDate = new Date(now);
      long twentyFiveHoursMillis = 1000 * 60 * 60 * 25;
      this.fromDate = new Date(now - twentyFiveHoursMillis);
      return;
    }

    // Finally, we try to deal with the user provided from and to dates.
    try {
      if (this.hasSetToAndFromDates()) {
        this.fromDate = new Date(Day.getInstance(this.dateFormat.parse(this.fromDateString))
            .getFirstTickOfTheDay() - 1);
        this.toDate = new Date(Day.getInstance(this.dateFormat.parse(this.toDateString))
            .getLastTickOfTheDay());
      }
      else {
        throw new BuildException(
            "Attributes 'fromDate' and 'toDate' must either be both set or both not set.");
      }
    }
    catch (ParseException ex) {
      throw new BuildException("Unable to parse 'fromDate' or 'toDate'.", ex);
    }

    if (this.fromDate.compareTo(this.toDate) > 0) {
      throw new BuildException("Attribute 'fromDate' must be a date before 'toDate'.");
    }
  }

  /**
   * Returns true if both of the to and from date strings have been set by the client. Both dates
   * must be set or else this sensor will not know which revisions to grab commit information.
   * 
   * @return true if both the to and from date strings have been set.
   */
  private boolean hasSetToAndFromDates() {
    return (this.fromDateString != null) && (this.toDateString != null);
  }

  /**
   * Returns the shell associated with the specified author, or null if not found. 
   * The shellCache is
   * used to store SensorShell instances associated with the specified user. The
   * SensorShellMap contains the SensorShell instances built from the
   * UserMap.xml file. This method should be used to retrieve the SensorShell
   * instances to avoid the unnecessary creation of SensorShell instances when
   * sending data for each commit entry. Rather than using a brand new
   * SensorShell instance, this method finds the correct shell in the map,
   * cache, or creates a brand new shell to use.
   * @param shellCache the mapping of author to SensorShell.
   * @param shellMap the mapping of author to SensorShell created by a usermap
   * entry.
   * @param author the author used to retrieve the shell instance.
   * @return the shell instance associated with the author name.
   * @throws SensorShellMapException thrown if there is a problem retrieving the
   * shell instance.
   * @throws SensorShellException thrown if there is a problem retrieving
   * the Hackystat host from the v8.sensor.properties file.
   */
  private SensorShell getShell(Map<String, SensorShell> shellCache, SensorShellMap shellMap,
      String author) throws SensorShellMapException, SensorShellException {
    if (shellCache.containsKey(author)) {
      return shellCache.get(author); // Returns a cached shell instance.
    }
    else {
      // If the shell user mapping has a shell, add it to the shell cache.
      if (shellMap.hasUserShell(author)) {
        SensorShell shell = shellMap.getUserShell(author);
        shellCache.put(author, shell);
        return shell;
      }
      else { // Create a new shell and add it to the cache.
        if ("".equals(this.defaultHackystatAccount)
            || "".equals(this.defaultHackystatPassword)
            || "".equals(this.defaultHackystatSensorbase)) {
          System.out.println("Warning: A user mapping for the user, " + author
              + " was not found and no default Hackystat account login, password, "
              + "or server was provided. Data ignored.");
          return null;
        }
        SensorShellProperties props = new SensorShellProperties(this.defaultHackystatSensorbase,
            this.defaultHackystatAccount, this.defaultHackystatPassword);

        SensorShell shell = new SensorShell(props, false, this.tool);
        shellCache.put(author, shell);
        return shell;
      }
    }
  }

  /**
   * @return the feedUrl
   */
  public String getFeedUrl() {
    return "http://code.google.com/feeds/p/" + projectName + "/issueupdates/basic";
  }

  /**
   * @return the csvUrl
   */
  public String getAllCsvUrl() {
    return "http://code.google.com/p/" + projectName + "/issues/csv?can=1&q=&" +
    "colspec=ID%20Type%20Status%20Priority%20Milestone%20Owner%20Summary%20Opened%20Closed";
  }

  /**
   * @return the csvUrl
   */
  public String getOpenCsvUrl() {
    return "http://code.google.com/p/" + projectName + "/issues/csv";
  }
  
  /**
   * Sets if verbose mode has been enabled.
   * @param isVerbose true if verbose mode is enabled, false if not.
   */
  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  /**
   * Sets a default Hackystat account to which to send commit data when there is
   * no svn committer to Hackystat account mapping.
   * 
   * @param defaultHackystatAccount The default Hackystat account.
   */
  public void setDefaultHackystatAccount(String defaultHackystatAccount) {
    this.defaultHackystatAccount = defaultHackystatAccount;
  }

  /**
   * Sets the default Hackystat account password.
   * @param defaultHackystatPassword the default account password.
   */
  public void setDefaultHackystatPassword(String defaultHackystatPassword) {
    this.defaultHackystatPassword = defaultHackystatPassword;
  }

  /**
   * Sets the default Hackystat sensorbase server.
   * @param defaultHackystatSensorbase the default sensorbase server.
   */
  public void setDefaultHackystatSensorbase(String defaultHackystatSensorbase) {
    this.defaultHackystatSensorbase = defaultHackystatSensorbase;
  }

  /**
   * Sets the optional fromDate. If fromDate is set, toDate must be set. This
   * field must be conform to yyyy-MM-dd format.
   * 
   * @param fromDateString The first date from which we send commit information
   * to Hackystat server.
   */
  public void setFromDate(String fromDateString) {
    this.fromDateString = fromDateString;
  }

  /**
   * Sets the optional toDate. If toDate is set, fromDate must be set. This
   * field must be conform to yyyy-MM-dd format.
   * 
   * @param toDateString The last date to which we send commit information to
   * Hackystat server.
   */
  public void setToDate(String toDateString) {
    this.toDateString = toDateString;
  }
  
  /**
   * Sets the last interval in minutes. 
   * 
   * @param lastIntervalInMinutes The preceding interval in minutes to poll.  
   */
  public void setLastIntervalInMinutes(String lastIntervalInMinutes) {
    this.lastIntervalInMinutesString = lastIntervalInMinutes;
  }

  /**
   * @param projectName the projectName to set
   */
  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  /**
   * @return the projectName
   */
  public String getProjectName() {
    return projectName;
  }

}
