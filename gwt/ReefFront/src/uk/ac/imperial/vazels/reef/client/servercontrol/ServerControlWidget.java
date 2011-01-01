package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ServerControlWidget extends Composite {

  private static ServerControlUiBinder uiBinder = GWT
  .create(ServerControlUiBinder.class);

  interface ServerControlUiBinder extends UiBinder<Widget, ServerControlWidget> {
  }

  private static final ServerControlStrings sStringConstants = 
    (ServerControlStrings) GWT.create(ServerControlStrings.class);

  @UiField Button btnToggleControlCenterRunning;
  @UiField Label lblProbeInstructions;
  @UiField CheckBox ckbDoneWithProbes;
  @UiField Button btnStartExperiment;
  
  private boolean mServerRunning;
  
  private final ManagerChangeHandler mStatusChangeHandler;

  public ServerControlWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    mStatusChangeHandler = new StatusChangeHandler();
    
    ServerStatusManager man = ServerStatusManager.getManager();
    man.addChangeHandler(mStatusChangeHandler);
    man.setAutoRefresh(true);
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnToggleControlCenterRunning")
  void toggleControlCenter(ClickEvent event) {
    btnToggleControlCenterRunning.setEnabled(false);
    if (mServerRunning) {
      ControlCentreManager.getManager().stop();
    } else {
      ControlCentreManager.getManager().start();
    }
  }
  
  @UiHandler("btnStartExperiment")
  void startExperiment(ClickEvent event) {
    ExperimentStartManager.getManager().startExperiment();
  }

  @UiHandler("ckbDoneWithProbes")
  void enableStartingExperiment(ClickEvent event) {
    btnStartExperiment.setEnabled(ckbDoneWithProbes.getValue());
  }
  
  /**
   * Set the UI elements (buttons, ...) to reflect the new running state
   * of the control centre.
   * @param running
   */
  private void setRunningState(boolean running) {
    mServerRunning = running;
    
    if (running) {
      btnToggleControlCenterRunning.setText(sStringConstants.goBackToSetup());
    } else {
      btnToggleControlCenterRunning.setText(sStringConstants.startControlCentre());
    }
    
    btnToggleControlCenterRunning.setEnabled(true);
    
    lblProbeInstructions.setVisible(running);
    ckbDoneWithProbes.setVisible(running);
    ckbDoneWithProbes.setValue(false);
    btnStartExperiment.setVisible(running);
  }
  
  /**
   * Handle updates to server status
   */
  private class StatusChangeHandler implements ManagerChangeHandler {
    @Override
    public void change(IManager man) {
      ServerStatusManager statusManager = ServerStatusManager.getManager();
      switch (statusManager.getStatus()) {
      case RUNNING :
        setRunningState(true);
        break;
      case READY :
        setRunningState(false);
        break;
      case TIMEOUT :
        Window.alert(sStringConstants.controlCentreTimeout());
        ControlCentreManager.getManager().stop();
        break;
      case STARTING:
      case EXPERIMENT:
      case FINISHED:
      case UNKNOWN:
        break;
      default:
        Window.alert(sStringConstants.unknownServerState());
      }
    }
  }
}
