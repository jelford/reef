package uk.ac.imperial.vazels.reef.client.servercontrol;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.util.MessageHandler;
import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;

public class ServerControl {
  /**
   * URIs to start and stop the server.
   */
  private static final String SERVER_START_URI_SUFFIX="/start";
  private static final String SERVER_STOP_URI_SUFFIX="/stop";
  private static final String SERVER_CONTROL_URI="/control";

  /**
   * How long to wait between making requests to the server and asking
   * for its new status.
   */
  private static final int SERVER_UPDATE_DELAY = 2700;

  /**
   * Stock request to check the server status.
   * <p>
   * When a response is received the status in the outer class is updated.
   * </p>
   */
  protected static class ServerStatusRequest extends MultipleRequester<ServerStatus> {
    private final Set<MessageHandler<ServerStatus>> mStatusHandlers;
    
    private ServerStatusRequest(final MessageHandler<ServerStatus> statusHandler) {
      super(RequestBuilder.GET, SERVER_CONTROL_URI, new Converter<ServerStatus>() {
        public ServerStatus convert(String original) {
          return new ServerStatus(original);
        }
      });
      mStatusHandlers = new HashSet<MessageHandler<ServerStatus>>();
      mStatusHandlers.add(statusHandler);
    }
    
    private static ServerStatusRequest instance;
    public static ServerStatusRequest getInstance(final MessageHandler<ServerStatus> statusHandler) {
      if (instance == null) {
        instance = new ServerStatusRequest(statusHandler);
      } else {
        instance.mStatusHandlers.add(statusHandler);
      }
      return instance;
    }
    public static ServerStatusRequest getInstanceOrThrow() throws NotInitialisedException{
      if (instance != null) {
        return instance;
      } else {
        throw new NotInitialisedException("Must initialise ServerStatusRequest with" +
        		"a handler before trying to get an instance of it!");
      }
    }
    
    @Override
    protected void received(ServerStatus reply, boolean success, String message) {
      if(success) {
        switch (reply.getState()) {
        case STARTING:
          mScheduleStatusRequest.schedule(SERVER_UPDATE_DELAY);
          break;
        default:
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
   * Stock request to update the server running state.
   */
  protected static class ServerRunRequest extends MultipleRequester<Void> {
    
    private ServerRunRequest() {
      super(RequestBuilder.POST, SERVER_CONTROL_URI, null);
    }
    
    private static ServerRunRequest instance;
    public static ServerRunRequest getInstance() throws NotInitialisedException {
      ServerStatusRequest.getInstanceOrThrow();
      if (instance == null) {
        instance = new ServerRunRequest();
      }
      return instance;
    }
    /**
     * Gets an instance of ServerRunRequest and adds @code statusHandler to
     * the list of status listeners, initialising ServerStatusRequest if
     * necessary.
     * @param statusHandler
     * @return
     */
    public static ServerRunRequest getInstance(MessageHandler<ServerStatus> statusHandler) {
      ServerStatusRequest.getInstance(statusHandler);
      if (instance == null) {
        instance = new ServerRunRequest();
      }
      return instance;
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
      mScheduleStatusRequest.schedule(SERVER_UPDATE_DELAY);
    }
  }
  
  
  private static Timer mScheduleStatusRequest = new Timer(){
    @Override
    public void run() {
      try {
        ServerStatusRequest.getInstanceOrThrow().update();
      } catch (NotInitialisedException e) {
        // TODO: Fix this
        e.printStackTrace();
      }
    }
  };
}
