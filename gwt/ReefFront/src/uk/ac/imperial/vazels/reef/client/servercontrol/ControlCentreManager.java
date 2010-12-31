package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.managers.Manager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

/**
 * Handles any dealings with the control centre.
 */
public class ControlCentreManager extends Manager<Void, Void> {
  /**
   * Start the Control Centre
   */
  private static final String SERVER_START_URI_SUFFIX="/start";
  
  /**
   * Stop the Control Centre
   */
  private static final String SERVER_STOP_URI_SUFFIX="/stop";
  
  /**
   * Singleton instance of this manager.
   */
  private static ControlCentreManager manager = null;
  
  /**
   * Current extension to use with the requester.
   */
  private String currentExt = "";
  
  private ControlCentreManager() {
  }
  
  /**
   * Get the singleton instance of this manager.
   * @return a singleton instance.
   */
  public static ControlCentreManager getManager() {
    if(manager == null) {
      manager = new ControlCentreManager();
    }
    return manager;
  }
  
  /**
   * Use to start the control centre, this is used instead of
   * {@link Manager#pushLocalData(PushCallback)} so that the request extension is properly
   * set.
   */
  public void start() {
    currentExt = SERVER_START_URI_SUFFIX;
    try {
      pushLocalData(null);
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
    currentExt = "";
  }
  
  /**
   * Use to stop the control centre, this is used instead of
   * {@link Manager#pushLocalData(PushCallback)} so that the request extension is properly
   * set.
   */
  public void stop() {
    currentExt = SERVER_STOP_URI_SUFFIX;
    try {
      pushLocalData(null);
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
    currentExt = "";
  }
  
  @Deprecated
  @Override
  public void pushLocalData(PushCallback callback) throws MissingRequesterException {
    super.pushLocalData(callback);
  }
  
  @Override
  protected boolean receivePullData(Void pulled) {
    return false;
  }

  @Override
  protected boolean receivePushData(Void pushed) {
    ServerStatusManager.getManager().serverChange();
    return false;
  }

  protected class ControlCentreStartStop extends MultipleRequester<Void> {
    public ControlCentreStartStop() {
      super(RequestBuilder.POST, "/control", null);
    }
    
    @Override
    public void go(RequestHandler<Void> handler) {
      super.go(handler, currentExt);
    }
  }
}
