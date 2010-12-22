package uk.ac.imperial.vazels.reef.client.servercontrol;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.util.MessageHandler;
import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;
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
   * package. 
   */
  private ServerControl() {}

  /**
   * The main URI for all control-based commands
   */
  private static final String SERVER_CONTROL_URI="/control";
  
 /**
   * Stock request to check the server status. A singleton class which will
   * handle any requests to the server regarding the running/ready state.
   */
  protected static class ServerStatusRequester extends MultipleRequester<ServerStatus> {
    /**
     * How long to wait between making requests to the server and asking
     * for its new status.
     */
    private static final int SERVER_UPDATE_DELAY = 3300;
    
    /**
     * How long can the server be "starting" for before it's a problem?
     * (Say 10 seconds)
     */
    private static final long SERVER_TIMEOUT = 10000;

    /**
     * How long to wait between periodic checks (when we've no reason to think
     * anything has happened - JUST IN CASE checks)
     */
    private static final int SERVER_PERIODIC_DELAY = 8500;

    /**
     * Store a list of subscribed MessageHandlers
     */
    private final Set<MessageHandler<ServerStatus>> mStatusHandlers;

    /**
     * Singleton class; have a private constructor.
     * @param statusHandler A MessageHandler to be notified when a request 
     * completes.
     */
    private ServerStatusRequester(
        final MessageHandler<ServerStatus> statusHandler) {

      super(RequestBuilder.GET, SERVER_CONTROL_URI, new Converter<ServerStatus>() {
        public ServerStatus convert(String original) {
          return new ServerStatus(original);
        }
      });

      if (statusHandler == null) {
        throw new NullPointerException("Doesn't make sense to request server" +
        "status with no callback on completed request");
      }

      mStatusHandlers = new HashSet<MessageHandler<ServerStatus>>();
      mStatusHandlers.add(statusHandler);
      
      /*
       * We want to get the server's status the first time someone registers
       * interest, and then probably again every so often so we're always on
       * the ball.
       * 
       * @TODO: reviewer - Should this repeat? I'm happy to change it to just
       * scheduling it the once (in that case, change this to only update() 
       * instead)
       * 
       * In any case, only do this once, in the constructor.
       */
      this.update();
      mScheduleStatusRequest.scheduleRepeating(SERVER_PERIODIC_DELAY);
    }

    /**
     * Store the single instance of this class.
     */
    private static ServerStatusRequester sInstance;

    /**
     * Get an instance of ServerStatusRequest. Use {@code getInstanceOrThrow()}
     * if you don't have a new MessageHandler to pass in (existing
     * MessageHandlers will be notified when requests complete). This setup
     * ensures we never complete a request with no MessageHandlers to notify
     * 
     * @param statusHandler Specify the handler to use for callbacks after
     * completed calls to the server. {@code NullPointerException} will be 
     * thrown if this is null.
     * 
     * If you're sure you want to get an instance of this Requester and
     * only send notifications to existing Handlers (rather than adding your
     * own), then use {@code getInstanceOrThrow()}
     * 
     * @return The single instance of ServerStatusRequest.
     */
    public static ServerStatusRequester getInstance(final MessageHandler<ServerStatus> statusHandler) {
      if (statusHandler == null) {
        throw new NullPointerException();
      }
      if (sInstance == null) {
        sInstance = new ServerStatusRequester(statusHandler);
      } else {
        sInstance.mStatusHandlers.add(statusHandler);
      }
      return sInstance;
    }
    
    /**
     * Works like {@code getInstance} except you needn't pass in a new
     * {@code MessageHandler}. Use this when you have already set up handlers
     * but need to get the class again, sending the results of completed requests
     * to those existing {@code MessageHandler}s
     * @see #getInstance(MessageHandler)
     * @return The single instance of ServerStatusRequest
     * @throws NotInitialisedException
     */
    public static ServerStatusRequester getInstanceOrThrow() throws NotInitialisedException{
      if (sInstance != null) {
        return sInstance;
      } else {
        throw new NotInitialisedException("Must initialise ServerStatusRequest with" +
        "a handler before trying to get an instance of it! Try getInstance(" +
        "MessageHandler<ServerStatus>)");
      }
    }

    /**
     * When the server is performing a long operation, we'd like to know how
     * long it's taking to do it. If it's too long, we'll take action.
     */
    private Date mHowLongHasServerBeenStarting = null;

    @Override
    protected void received(ServerStatus reply, boolean success, String message) {
      if(success) {
        /*
         * Intelligently pass notification back to the UI or reschedule another
         * update if the server is half-way through. Need to keep track of last
         * how long the server has had the STARTING status.
         */
        switch (reply.getState()) {
        case STARTING:
          // How long has the server been starting? Do we have a problem?
          if (mHowLongHasServerBeenStarting == null) {
            mHowLongHasServerBeenStarting = new Date();
          }

          Date theTimeNow = new Date();
          Long timeElapsed = theTimeNow.getTime() - 
          mHowLongHasServerBeenStarting.getTime();
          if (timeElapsed < SERVER_TIMEOUT) {
            mScheduleStatusRequest.schedule(SERVER_UPDATE_DELAY);     
            break;
          } else {
            reply = new ServerStatus(ServerStatus.ServerState.TIMEOUT);
          }
        default:
          // The operation has not timed out.
          mHowLongHasServerBeenStarting = null;

          for(MessageHandler<ServerStatus> handler : mStatusHandlers) {
            if (handler != null) {
              handler.handle(reply);
            }
          }
        }

      }
    }

    /**
     * Send request to server to grab server status
     * and update controls based on the result.
     */
    public void update() {
      go(null);
    }
  }

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
     * Have the Status Requester as a final, so we know it's initialised before
     * we use this class.
     */
    private final ServerStatusRequester mServerStatusRequester;

    /**
     * A singleton class
     */
    private ControlCentreRequester() throws NotInitialisedException {
      super(RequestBuilder.POST, SERVER_CONTROL_URI, null);
      mServerStatusRequester = ServerStatusRequester.getInstanceOrThrow();
    }

    /**
     * A singleton class. Also initialised the StatusRequester
     * @param statusHandler
     */
    public ControlCentreRequester(MessageHandler<ServerStatus> statusHandler) {
      super(RequestBuilder.POST, SERVER_CONTROL_URI, null);
      mServerStatusRequester = ServerStatusRequester.getInstance(statusHandler);
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
    public static ControlCentreRequester getInstanceOrThrow() throws NotInitialisedException {
      if (sInstance == null) {
        sInstance = new ControlCentreRequester();
      }
      return sInstance;
    }
    /**
     * Gets an instance of {@code ControlCentreRequester} and adds {@code 
     * statusHandler} to the list of status listeners, initialising 
     * ServerStatusRequest if necessary.
     * @param statusHandler Non-null MessageHandler for initialising the
     * ServerStatusRequester
     * @return the single instance of {@code ControlCentreRequester}
     */
    public static ControlCentreRequester getInstance(MessageHandler<ServerStatus> statusHandler) {
      if (sInstance == null) {
        sInstance = new ControlCentreRequester(statusHandler);
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
      mServerStatusRequester.update();
    }
  }
  
  public static class ExperimentStartRequester extends MultipleRequester<Void> {    
    /**
     * Start the experiment running (not to be confused with above)
     */
    private static final String SERVER_RUN_URI=SERVER_CONTROL_URI + "/startexperiment";
    
    /**
     * We don't want to risk that we haven't initialised our Status Requester
     * before using this class.
     */
    private final ServerStatusRequester mServerStatusRequester;
    
    /**
     * A singleton class
     */
    private ExperimentStartRequester() throws NotInitialisedException {
      super(RequestBuilder.POST, SERVER_RUN_URI, null);
      mServerStatusRequester = ServerStatusRequester.getInstanceOrThrow();
    }
    
    /**
     * A singleton class. Initialise the ServerStatusRequester too.
     * @param statusHandler
     */
    public ExperimentStartRequester(MessageHandler<ServerStatus> statusHandler) {
      super(RequestBuilder.POST, SERVER_RUN_URI, null);
      mServerStatusRequester = ServerStatusRequester.getInstance(statusHandler);
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
    public static ExperimentStartRequester getInstanceOrThrow() throws NotInitialisedException {
      if (sInstance == null) {
        sInstance = new ExperimentStartRequester();
      }
      return sInstance;
    }
    
    /**
     * Gets an instance of {@code ExperimentStartRequester}
     * @return the single instance of {@code ServerRunRequester}
     * @param a {@code MessageHandler} with which to initialise the 
     * {@code StatusRequester}.
     * @see #getInstance(MessageHandler)
     */
    public static ExperimentStartRequester getInstance(MessageHandler<ServerStatus> statusHandler) {
      if (sInstance == null) {
        sInstance = new ExperimentStartRequester(statusHandler);
      }
      return sInstance;
    }
    
    public void runExperiment() {
      go(null);
    }
    
    @Override
    public void received(Void reply, boolean success, String message) {
      mServerStatusRequester.update();
    }
    
  }

  /**
   * Have a timer so that we can space out sending status requests in a
   * sensible fashion.
   */
  private static Timer mScheduleStatusRequest = new Timer(){
    @Override
    public void run() {
      try {
        ServerStatusRequester.getInstanceOrThrow().update();
      } catch (NotInitialisedException e) {
        e.printStackTrace();
      }
    }
  };
  
  /**
   * The big red button to stop doing stuff if things go wrong.
   */
  public static void fail() {
    // Oh no! This is very bad indeed!
    Window.alert("It's all gone very wrong!");
    cancelTimers();
  }
  
  /**
   * This needs to be done either if we fail, or when we enter the running
   * phase.
   */
  public static void cancelTimers() {
    mScheduleStatusRequest.cancel();
  }
}
