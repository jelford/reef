package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.util.MessageHandler;
import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

/**
 * A wrapper class containing Requesters to talk to the server. Really, it's a
 * package. Except it's not; it's a class. Why have I done this? Well, I thought
 * we had quite enough packages, and also I wanted them to share bunch of resources.
 * 
 * Don't inherit from this; it will end badly.
 * @author james
 *
 */
public final class ServerControl {
  /**
   * It doesn't make sense to instantiate this. This makes it act like a
   * package. WHAT????? THEN USE A FUCKING PACKAGE!
   */
  private ServerControl() {}

  /**
   * The main URI for all control-based commands
   */
  private static final String SERVER_CONTROL_URI="/control";
  
  /**
   * Stock request to update the server running state. A singleton class which
   * will handle sending requests to the server to start or stop the control
   * centre. Before using this, be sure to initialise the ServerStatusRequester.
   */
  protected static class ControlCentreRequester extends MultipleRequester<Void> {
    /*
     * URIs to start and stop the server.
     */
    /**
     * Start the Control Centre
     */
    private static final String SERVER_START_URI_SUFFIX="/start";
    
    /**
     * Stop the Control Centre
     */
    private static final String SERVER_STOP_URI_SUFFIX="/stop";

    /**
     * A singleton class
     */
    private ControlCentreRequester() {
      super(RequestBuilder.POST, SERVER_CONTROL_URI, null);
    }

    /**
     * Store the single instance of ServerRunRequester
     */
    private static ControlCentreRequester sInstance;
    
    /**
     * Gets an instance of {@code ControlCentreRequester}
     * @return the single instance of ControlCentreRequester
     * @throws NotInitialisedException if you fail to first initialise the
     * {@code ServerStatusRequester}.
     * @see #getInstance(MessageHandler)
     */
    public static ControlCentreRequester getInstance() {
      if (sInstance == null) {
        sInstance = new ControlCentreRequester();
      }
      return sInstance;
    }
    
    /**
     * Send an async call to start the server.
     */
    public void start() {
      go(null, SERVER_START_URI_SUFFIX);
    }

    /**
     * Send an async call to stop the server.
     */
    public void stop() {
      go(null, SERVER_STOP_URI_SUFFIX);
    }

    /**
     * Handle responses from the server after sending a start or stop request.
     * <p>
     * Needs to wait for a period of time and then send a request to the server to
     * check the running state. This is necessary as the server cannot return an
     * accurate running state immediately, but we do not want it to block while it
     * waits for a clear answer.
     * </p>
     */
    @Override
    protected void received(Void reply, boolean success, String message) {
      ServerStatusManager.getManager().serverChange();
    }
  }
  
  public static class ExperimentStartRequester extends MultipleRequester<Void> {    
    /**
     * Start the experiment running (not to be confused with above)
     */
    private static final String SERVER_RUN_URI=SERVER_CONTROL_URI + "/startexperiment";
    
    /**
     * A singleton class
     */
    private ExperimentStartRequester() {
      super(RequestBuilder.POST, SERVER_RUN_URI, null);
    }

    /**
     * Store the single instance of {@code ExperimentStartRequester}
     */
    private static ExperimentStartRequester sInstance;
    
    /**
     * Gets an instance of {@code ExperimentStartRequester}
     * @return the single instance of {@code ServerRunRequester}
     * @throws NotInitialisedException if you fail to first initialise the
     * {@code ServerStatusRequester}.
     * @see #getInstance(MessageHandler)
     */
    public static ExperimentStartRequester getInstance() {
      if (sInstance == null) {
        sInstance = new ExperimentStartRequester();
      }
      return sInstance;
    }
    
    public void runExperiment() {
      go(null);
    }
    
    @Override
    public void received(Void reply, boolean success, String message) {
      ServerStatusManager.getManager().serverChange();
    }
    
  }
  
  /**
   * The big red button to stop doing stuff if things go wrong.
   */
  public static void fail() {
    // Oh no! This is very bad indeed!
    Window.alert("It's all gone very wrong!");
    ServerStatusManager.getManager().setAutoRefresh(false);
  }
}
