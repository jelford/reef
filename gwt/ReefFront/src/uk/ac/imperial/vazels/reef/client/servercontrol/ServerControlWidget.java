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

  private static final ServerControlStrings sStringConstants = 
    (ServerControlStrings) GWT.create(ServerControlStrings.class);
  
  private final ServerControl.ControlCentreRequester mServerRun;

  @UiField Button btnStartServer;

  public ServerControlWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    mServerRun = ServerControl.ControlCentreRequester
      .getInstance(new StatusMessageHandler());
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnStartServer")
  void startClick(ClickEvent event) {
    btnStartServer.setEnabled(false);
    mServerRun.start();
  }

  /**
   * Set the UI elements (buttons, ...) to reflect the new running state
   * of the control centre.
   * @param running
   */
  private void setRunningState(boolean running) {
    if (running) {
      btnStartServer.setText(sStringConstants.goBackToSetup());
    } else {
      btnStartServer.setText(sStringConstants.startControlCentre());
    }
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
        setRunningState(true);
        break;
      case READY :
        setRunningState(false);
        break;
      case TIMEOUT :
        Window.alert(sStringConstants.controlCentreTimeout());
        break;
      case EXPERIMENT :
        Window.alert(sStringConstants.experimentRunning());
        break;
      default:
        Window.alert(sStringConstants.unknownServerState());
      }
    }
    
  }
}
