package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.ui.MainReefPanel;
import uk.ac.imperial.vazels.reef.client.ui.SetupPhasePanel;
import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

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
  
  private final ServerControl.ControlCentreRequester mServerRunRequester;

  @UiField Button btnToggleControlCenterRunning;
  @UiField Label lblProbeInstructions;
  @UiField CheckBox ckbDoneWithProbes;
  @UiField Button btnStartExperiment;
  
  private boolean mServerRunning;
  
  private final ManagerChangeHandler mStatusChangeHandler;

  public ServerControlWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    btnToggleControlCenterRunning.setText(sStringConstants.startControlCentre());
    lblProbeInstructions.setText(sStringConstants.setupInstructions());
    ckbDoneWithProbes.setText(sStringConstants.setupCheckBox());
    btnStartExperiment.setText(sStringConstants.startExperiment());
    
    mStatusChangeHandler = new StatusChangeHandler();
    
    mServerRunRequester = ServerControl.ControlCentreRequester
          .getInstance();
    
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
    try {
      SetupPhasePanel.getInstanceOrThrow().setupDone(!mServerRunning);
    } catch (NotInitialisedException e) {
      // Pretty sure this won't happen
      Window.alert("Tried to get SetupPhasePanel when it's not initialised");
      e.printStackTrace();
    }
    if (mServerRunning) {
      mServerRunRequester.stop();
    } else {
      mServerRunRequester.start();
    }
  }
  
  @UiHandler("btnStartExperiment")
  void startExperiment(ClickEvent event) {
    ServerControl.ExperimentStartRequester
    .getInstance().runExperiment();
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
    
    try {
      SetupPhasePanel.getInstanceOrThrow().setupDone(running);      
    } catch (NotInitialisedException e) {
      Window.alert("Phailed to initialise some stuff, now it's broken.");
      e.printStackTrace();
    }
  }
  
  /**
   * Handle updates to server status
   */
  private class StatusChangeHandler implements ManagerChangeHandler {
    @Override
    public void change(IManager man) {
      ServerStatusManager statusManager = ServerStatusManager.getManager();
      statusManager.setAutoRefresh(true);
      switch (statusManager.getStatus()) {
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
        statusManager.setAutoRefresh(false);
        MainReefPanel.getInstance().startRunningPhase();
        break;
      default:
        Window.alert(sStringConstants.unknownServerState());
        ServerControl.fail();
      }
    }
  }
}
