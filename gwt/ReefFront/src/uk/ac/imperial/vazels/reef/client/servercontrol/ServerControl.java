package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.MultipleRequester.Converter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ServerControl extends Composite {

  private static ServerControlUiBinder uiBinder = GWT
      .create(ServerControlUiBinder.class);

  interface ServerControlUiBinder extends UiBinder<Widget, ServerControl> {
  }

  /**
   * URIs to start and stop the server.
   */
  private static final String SERVER_START_URI="/control/start";
  private static final String SERVER_STOP_URI="/control/stop";
  private static final String SERVER_STATUS_URI="/control";
  
  private final ServerRunningRequest mServerRunningRequest;
  
  @UiField Button btnStartServer;
  @UiField Button btnStopServer;
  
  public ServerControl() {
    initWidget(uiBinder.createAndBindUi(this));
    
    mServerRunningRequest = new ServerRunningRequest();
    
    mServerRunningRequest.updateServerStatus();
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnStartServer")
  void startClick(ClickEvent event) {
    btnStartServer.setEnabled(false);
    mServerRunningRequest.startServer();
  }
  
  @UiHandler("btnStopServer")
  void stopClick(ClickEvent event) {
    btnStopServer.setEnabled(false);
    mServerRunningRequest.stopServer();
  }
  
  /**
   * Set the UI elements (buttons, ...) to reflect the new running state
   * of the control centre.
   * @param running
   */
  private void setRunningStateUI(boolean running) {
    btnStartServer.setEnabled(!running);
    btnStopServer.setEnabled(running);
  }
  
  /**
   * Helper class to handle communicating with the server for us.
   * @author james
   *
   */
  private class ServerRunningRequest {
    /**
     * A request to start the control centre. Use the same one every time, and pass
     * in mPostFinishedHandler to .go().
     */
    private MultipleRequester<Void> mStartRequest;
    
    /**
     * A request to stop the control centre. Use the same one every time, and pass
     * in mPostFinishedHandler to .go().
     */
    private MultipleRequester<Void> mStopRequest;
    
    /**
     * A request to get the running/ready status of the control centre. Called after
     * any post request by mPostFinishedHandler, and any time the main class's code
     * needs to know the status of the control centre.
     */
    private MultipleRequester<ServerStatus> mStatusRequest;
    
    /**
     * Both start and stop requests handle returns in the same way;
     * they must update the UI according to the new control centre status.
     */
    private PostFinishedHandler mPostFinishedHandler;

    /**
     * Initialise MultipleRequesters for all the different types of request we can do.
     * This is potentially very expensive, so it's best if this is a singleton class.
     * Cannot enforce singleton through a Factory design pattern, but have made this
     * private so it won't be instantiated elsewhere.
     */
    private ServerRunningRequest() {
      mStartRequest = new MultipleRequester<Void>(RequestBuilder.POST, SERVER_START_URI, null);
      mStopRequest = new MultipleRequester<Void>(RequestBuilder.POST, SERVER_STOP_URI, null);
      mStatusRequest = new MultipleRequester<ServerStatus>(RequestBuilder.GET, SERVER_STATUS_URI,
          new Converter<ServerStatus>(){

        @Override
        public ServerStatus convert(String original) {
          return new ServerStatus(original);
        }
        
      });
      mPostFinishedHandler = new PostFinishedHandler();
    }

    /**
     * Send an async call to start the server, and when that finishes send another
     * to get the server running state. Necessary because the server can't return
     * an accurate running state immediately, but won't block until it knows properly.
     */
    public void startServer(){
      mStartRequest.go(mPostFinishedHandler);
    }
    
    /**
     * Send an async call to stop the server, and when that finishes send another
     * to get the server running state. Necessary because the server can't return
     * an accurate running state immediately, but won't block until it knows properly.
     */
    public void stopServer(){
      mStopRequest.go(mPostFinishedHandler);
    }
    
    /**
     * Get the running/stopped status of the server
     */
    public void updateServerStatus(){
      mStatusRequest.go(new RequestHandler<ServerStatus>(){
        @Override
        public void handle(ServerStatus reply, boolean success, String message) {
          switch (reply.mServerState) {
          case RUNNING:
            setRunningStateUI(true);
            break;
          case READY:
            setRunningStateUI(false);
            break;
          default:
            // TODO: Clean this up
            Window.alert("Got unknown server state");
          }
        }
      });
    }
    
    /**
     * Any POST requests to the server will need to update the UI status
     * afterwards, and can do so using this handler. They cannot get the
     * control centre's status from the return value of the post request
     * as the server returns before it knows whether launching was successful.
     * @author james
     *
     */
    private class PostFinishedHandler extends RequestHandler<Void>{
      /**
       * We introduce a delay to give the server time to work out
       * whether it's successfully launched the control centre.
       */
      private static final int SERVER_UPDATE_DELAY = 3000;
      @Override
      public void handle(Void reply, boolean success, String message) {
        /*
         * Update the UI status after a delay.
         */
        new Timer(){
          @Override
          public void run() {
            updateServerStatus();
          }
        }.schedule(SERVER_UPDATE_DELAY);
      }
    }
  }
}
