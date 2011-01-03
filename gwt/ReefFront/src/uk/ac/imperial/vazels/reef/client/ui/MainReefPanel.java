package uk.ac.imperial.vazels.reef.client.ui;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.servercontrol.ServerStatusManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainReefPanel extends Composite implements ManagerChangeHandler {
  
  /**
   * Let's have some localised string constants!
   */
  private static final UIStrings sStringConstants =
    (UIStrings) GWT.create(UIStrings.class);

  private static MainReefPanelUiBinder uiBinder = GWT
      .create(MainReefPanelUiBinder.class);

  interface MainReefPanelUiBinder extends UiBinder<Widget, MainReefPanel> {
  }
  
  /**
   * Which phase is the experiment in?
   * {@code null} means we don't know, otherwise we're in running or setup phase.
   */
  Boolean runningPhase = null;
  
  @UiField Label mTitle;
  @UiField SimplePanel mPlaceholder;

  public MainReefPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    
    ServerStatusManager man = ServerStatusManager.getManager();
    man.addChangeHandler(this);
    man.setAutoRefresh(true);
    
    setPhase();
  }

  private void setContent(String titleText, Widget w) {
    mTitle.setText(titleText);
    mPlaceholder.setWidget(w);
  }

  @Override
  public void change(IManager man) {
    ServerStatusManager manager = ServerStatusManager.getManager();
    
    Boolean oldPhase = runningPhase;
    
    switch (manager.getStatus()) {
    case EXPERIMENT:
    case FINISHED:
      runningPhase = true;
      break;
    case READY:
    case STARTING:
    case TIMEOUT:
    case RUNNING:
      runningPhase = false;
      break;
    case UNKNOWN:
    default:
      runningPhase = null;
      break;
    }
    
    if(oldPhase != runningPhase) {
      setPhase();
    }
  }
  
  /**
   * Set the current phase to the one specified by {@link MainReefPanel#runningPhase}.
   */
  protected void setPhase() {
    if(runningPhase == null) {
      setContent(sStringConstants.initialisation(), null);
    }
    else if(runningPhase) {
      setContent(sStringConstants.runningPhase(), new RunningPhasePanel());
    }
    else {
      setContent(sStringConstants.experimentSetup(), new SetupPhasePanel());
    }
  }
}
