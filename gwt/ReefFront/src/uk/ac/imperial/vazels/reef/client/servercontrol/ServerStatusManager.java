package uk.ac.imperial.vazels.reef.client.servercontrol;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.servercontrol.ServerStatus.ServerState;

/**
 * Deals with keeping an up to date status for the server.
 */
public class ServerStatusManager extends Manager<ServerStatus, Void>{
  /**
   * The main URI for all control-based commands
   */
  private static final String SERVER_CONTROL_URI="/control";
  
  /**
   * How long to wait between periodic updates (when we've no reason to think
   * anything has happened - JUST IN CASE checks)
   */
  private static final int SERVER_PERIODIC_DELAY = 8500;
  
  /**
   * How long to wait between updates when we want a really up to date status.
   */
  private static final int SERVER_FREQUENT_DELAY = 2000;
  
  /**
   * How long can the server be "starting" for before it's a problem? (Say 10
   * seconds)
   */
  private static final long SERVER_TIMEOUT = 10000;
  
  /**
   * Singleton manager.
   */
  private static ServerStatusManager manager = null;
  
  /**
   * Should the manager auto-refresh?
   */
  private boolean autoRefresh = false;
  
  /**
   * Auto refreshes.
   */
  private DelayedUpdate autoRefresher = new DelayedUpdate();
  
  /**
   * When did we enter this status?
   */
  private Date inCurrentStatus = null;
  
  /**
   * Are we waiting for the status to change?
   */
  private boolean waitingForChange = false;
  
  /**
   * Cached server status.
   */
  private ServerState status;
  
  /**
   * Server-related strings.
   */
  private static final ServerControlStrings sStringConstants = 
    (ServerControlStrings) GWT.create(ServerControlStrings.class);
  
  /**
   * Has the server timed out in the current status.
   * This should be updated at every pull.
   */
  private boolean timedOut = false;
  
  private ServerStatusManager() {
    super();
    setPuller(new StatusUpdate());
  }
  
  /**
   * Gets the singleton instance of the manager
   * @return singleton server status manager
   */
  public static ServerStatusManager getManager() {
    if(manager == null) {
      manager = new ServerStatusManager();
    }
    return manager;
  }
  
  @Override
  protected boolean receivePullData(ServerStatus pulled) {
    //Changing state?
    if(!pulled.getState().equals(status)) {
      waitingForChange = false;
      
      if(pulled.getState().equals(ServerState.STARTING)) {
        waitingForChange = true;
      }
      
      inCurrentStatus = new Date();
      // Updates the frequency
      if(isAutoRefreshing()) {
        setAutoRefreshInterval();
      }
    }
    
    status = pulled.getState();
    timedOut = checkTimeout();
    
    if(timedOut) {
      Window.alert(sStringConstants.controlCentreTimeout());
      ControlCentreManager.getManager().stop();
    }
    
    return true;
  }
  
  /**
   * Check for server timeout.
   * @return {@code true} if and only if the server has timed out.
   */
  protected boolean checkTimeout() {
    if(!ServerState.STARTING.equals(status)) {
      return false;
    }
    
    Date now = new Date();
    long elapsed = now.getTime() - inCurrentStatus.getTime();
    
    return (elapsed >= SERVER_TIMEOUT);
  }

  @Override
  protected boolean receivePushData(Void pushed) {
    return false;
  }
  
  /**
   * As in {@link Manager#serverChange} but starts a pull immediately if we have
   * auto-refresh turned on.
   */
  @Override
  public void serverChange() {
    super.serverChange();
    if(autoRefresh) {
      try {
        getServerData();
      } catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * Tell the manager you're waiting for a change to update more frequently.
   */
  public void waitForChange() {
    waitingForChange = true;
    if(autoRefresh) {
      setAutoRefreshInterval();
    }
  }
  
  /**
   * Get the server status.
   * @return the server status.
   */
  public ServerState getStatus() {
    if(timedOut) {
      return ServerState.TIMEOUT;
    }
    return status;
  }

  /**
   * Tell the manager whether or not to auto-refresh.
   * @param refresh Should the manager auto-refresh?
   */
  public void setAutoRefresh(boolean refresh) {
    if(autoRefresh != refresh) {
      if(refresh) {
        setAutoRefreshInterval();
      }
      else {
        autoRefresher.cancel();
      }
    }
    
    autoRefresh = refresh;
  }
  
  /**
   * Sets up auto-refreshing with correct intervals.
   */
  protected void setAutoRefreshInterval() {
    if(waitingForChange) {
      autoRefresher.setFrequency(SERVER_FREQUENT_DELAY);
    }
    else {
      autoRefresher.setFrequency(SERVER_PERIODIC_DELAY);
    }
  }
  
  /**
   * Is this manager currently set to auto-refresh?
   * @return {@code true} if the manager is set to auto-refresh
   */
  public boolean isAutoRefreshing() {
    return autoRefresh;
  }
  
  /**
   * Used to pull status data from the server.
   */
  protected class StatusUpdate extends MultipleRequester<ServerStatus> {
    public StatusUpdate() {
      super(RequestBuilder.GET, SERVER_CONTROL_URI, new Converter<ServerStatus>() {
        @Override
        public ServerStatus convert(String original) {
          return new ServerStatus(original);
        }
      });
    }
  }
  
  /**
   * Schedules manager pull requests.
   */
  protected class DelayedUpdate extends Timer {    
    @Override
    public void run() {
      serverChange();
      try {
        getServerData();
      } catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
    
    /**
     * Set an update to occur in {@code delay} milliseconds.
     * @param delay Milliseconds before manager update should occur.
     */
    public void update(int delay) {
      cancel();
      schedule(delay);
    }
    
    public void setFrequency(int period) {
      cancel();
      run();
      scheduleRepeating(period);
    }
  }
}
