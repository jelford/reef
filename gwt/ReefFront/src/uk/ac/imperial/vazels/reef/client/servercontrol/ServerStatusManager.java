package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.servercontrol.ServerControl.ServerStatusRequester;
import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

public class ServerStatusManager extends Manager<ServerStatus, Void>{
  /**
   * The main URI for all control-based commands
   */
  private static final String SERVER_CONTROL_URI="/control";
  
  /**
   * Singleton manager.
   */
  private static ServerStatusManager manager = null;
  
  private Timer autoRefreshScheduler = null;
  
  /**
   * Cached server status.
   */
  private ServerStatus status;
  
  private ServerStatusManager() {
    super();
    setPuller(new StatusUpdate());
    
    autoRefreshScheduler = new Timer(){    
      @Override
      public void run() {
        try {
          getServerData();
        } catch (MissingRequesterException e) {
          e.printStackTrace();
        }
      }
    };
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
    status = pulled;
    return true;
  }

  @Override
  protected boolean receivePushData(Void pushed) {
    return false;
  }
  
  /**
   * Get the server status.
   * @return the server status.
   */
  public ServerStatus getStatus() {
    return status;
  }

  /**
   * Ask the manager to auto refresh the server every {@code delay} milliseconds.
   * @param delay Milliseconds between status requests.
   */
  public void setAutoRefresh(int delay) {
    autoRefreshScheduler.scheduleRepeating(delay);
  }
  
  /**
   * Stop the manager autorefreshing.
   */
  public void cancelAutoRefresh() {
    autoRefreshScheduler.cancel();
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
}
