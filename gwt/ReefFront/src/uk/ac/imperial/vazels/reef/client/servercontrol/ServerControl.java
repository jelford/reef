package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.groups.AllocateGroups;

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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ServerControl extends Composite {

  private static ServerControlUiBinder uiBinder = GWT
  .create(ServerControlUiBinder.class);

  interface ServerControlUiBinder extends UiBinder<Widget, ServerControl> {
  }

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
  
  private final ServerStatusRequest serverStatus;
  private final ServerRunRequest serverRun;

  @UiField Button btnStartServer;
  @UiField Button btnStopServer;


  public ServerControl() {
    initWidget(uiBinder.createAndBindUi(this));

    serverRun = new ServerRunRequest();
    serverStatus = new ServerStatusRequest();
    serverStatus.update();
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnStartServer")
  void startClick(ClickEvent event) {
    btnStartServer.setEnabled(false);
    serverRun.start();
  }

  @UiHandler("btnStopServer")
  void stopClick(ClickEvent event) {
    btnStopServer.setEnabled(false);
    serverRun.stop();
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
  
  protected Timer mScheduleStatusRequest = new Timer(){
    @Override
    public void run() {
      serverStatus.update();
    }
  };

  /**
   * Stock request to check the server status.
   * <p>
   * When a response is received the status in the outer class is updated.
   * </p>
   */
  private class ServerStatusRequest extends MultipleRequester<ServerStatus> {
    public ServerStatusRequest() {
      super(RequestBuilder.GET, SERVER_CONTROL_URI, new Converter<ServerStatus>() {
        public ServerStatus convert(String original) {
          return new ServerStatus(original);
        }
      });
    }
    
    @Override
    protected void received(ServerStatus reply, boolean success, String message) {
      if(success) {
        switch (reply.getState()) {
        case RUNNING:
          setRunningStateUI(true);
          break;
        case READY:
          setRunningStateUI(false);
          break;
        case STARTING:
          mScheduleStatusRequest.schedule(SERVER_UPDATE_DELAY);
          break;
        case TIMEOUT:
          // TODO: Figure out what we're going to do in this case (probably shut down the server)
          break;
        default:
          Window.alert("Got unknown server state");
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
  private class ServerRunRequest extends MultipleRequester<Void> {
    
    public ServerRunRequest() {
      super(RequestBuilder.POST, SERVER_CONTROL_URI, null);
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
}
