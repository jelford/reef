package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;

public class ServerStatusManager extends Manager<ServerStatus, Void>{
  private static final String SERVER_CONTROL_URI="/control";
  
  private static ServerStatusManager manager = null;
  
  private ServerStatus status;
  
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
