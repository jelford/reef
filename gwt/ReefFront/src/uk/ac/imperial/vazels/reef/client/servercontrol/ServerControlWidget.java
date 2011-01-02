package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ServerControlWidget extends Composite implements ManagerChangeHandler {

  private static ServerControlUiBinder uiBinder = GWT
  .create(ServerControlUiBinder.class);

  interface ServerControlUiBinder extends UiBinder<Widget, ServerControlWidget> {
  }

  @UiField ToggleButton btnToggleControlCenterRunning;
  @UiField VerticalPanel runningControls;
  @UiField CheckBox ckbDoneWithProbes;
  @UiField Button btnStartExperiment;

  public ServerControlWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    ServerStatusManager man = ServerStatusManager.getManager();
    man.addChangeHandler(this);
    man.setAutoRefresh(true);
    
    try {
      man.withServerData(new PullCallback() {
        @Override
        public void got() {
          change(null);
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
    
  }

  /*
   * Add ClickHandlers for the buttons
   */
  @UiHandler("btnToggleControlCenterRunning")
  void toggleControlCenter(ClickEvent event) {
    btnToggleControlCenterRunning.setEnabled(false);
    
    if (btnToggleControlCenterRunning.getValue()) {
      ControlCentreManager.getManager().start();
    } else {
      ControlCentreManager.getManager().stop();
    }
  }
  
  @UiHandler("btnStartExperiment")
  void startExperiment(ClickEvent event) {
    ExperimentStartManager.getManager().startExperiment();
  }

  @UiHandler("ckbDoneWithProbes")
  void enableStartingExperiment(ValueChangeEvent<Boolean> event) {
    btnStartExperiment.setEnabled(event.getValue());
  }
  
  /**
   * Set the UI elements (buttons, ...) to reflect the new running state
   * of the control centre.
   * @param running
   */
  private void setRunningState(boolean running) {
    if (!running) {
      // Reset when not in this pane
      ckbDoneWithProbes.setValue(false, true);
    }
    
    btnToggleControlCenterRunning.setEnabled(true);
    btnToggleControlCenterRunning.setDown(running);
    runningControls.setVisible(running);
  }
  
  /**
   * Set the widget to be in intermediate state.
   * When the state is not ready or running. Instead it is starting.
   */
  protected void setIntermediateState() {
    btnToggleControlCenterRunning.setDown(true);
    btnToggleControlCenterRunning.setEnabled(false);
    runningControls.setVisible(false);
  }
  
  /**
   * Handle updates to server status
   */
  @Override
  public void change(IManager man) {
    ServerStatusManager statusManager = ServerStatusManager.getManager();
    
    switch (statusManager.getStatus()) {
    case RUNNING:
      setRunningState(true);
      break;
    case READY:
      setRunningState(false);
      break;
    case STARTING:
      setIntermediateState();
      break;
    }
  }
  
  /**
   * Create the toggle button with separate up and down text.
   * @param up Up text.
   * @param down Down text.
   * @return The toggle button.
   */
  @UiFactory
  ToggleButton createToggle(String up, String down) {
    return new ToggleButton(up, down);
  }
}
