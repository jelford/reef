package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.util.Message;
import uk.ac.imperial.vazels.reef.client.util.MessageHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ServerControlWidget extends Composite {

  private static ServerControlUiBinder uiBinder = GWT
  .create(ServerControlUiBinder.class);

  interface ServerControlUiBinder extends UiBinder<Widget, ServerControlWidget> {
  }

  
  //private final ServerControl.ServerStatusRequester mServerStatus;
  private final ServerControl.ServerRunRequester mServerRun;

  @UiField Button btnStartServer;
  @UiField Button btnStopServer;
  private final MessageHandler<ServerStatus> mStatusUpdateHandler;

  public ServerControlWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    mStatusUpdateHandler = new StatusMessageHandler();

    //mServerStatus = ServerControl.ServerStatusRequester.getInstance(mStatusUpdateHandler);
    mServerRun = ServerControl.ServerRunRequester.getInstance(mStatusUpdateHandler);
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnStartServer")
  void startClick(ClickEvent event) {
    btnStartServer.setEnabled(false);
    mServerRun.start();
  }

  @UiHandler("btnStopServer")
  void stopClick(ClickEvent event) {
    btnStopServer.setEnabled(false);
    mServerRun.stop();
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
   * Handle updates to server status
   */
  private class StatusMessageHandler extends MessageHandler<ServerStatus> {

    @Override
    public void handle(Message<ServerStatus> incoming) {
      handle(incoming.getMessage());
    }

    @Override
    public void handle(ServerStatus incoming) {
      switch (incoming.getState()) {
      case RUNNING :
        setRunningStateUI(true);
        break;
      case READY :
        setRunningStateUI(false);
        break;
      case TIMEOUT :
        Window.alert("Server has timed out! There might be a problem on the server end; try restarting the server, clearing and data saved in the working directory, and refreshing this page");
        break;
      default:
        Window.alert("Got an unknown server state - something is wrong. Try restarting the server and refreshing the page");
      }
    }
    
  }
}
