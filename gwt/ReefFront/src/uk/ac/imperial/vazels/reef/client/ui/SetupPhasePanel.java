package uk.ac.imperial.vazels.reef.client.ui;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.servercontrol.ServerStatusManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Widget;

public class SetupPhasePanel extends Composite implements ManagerChangeHandler {
  /*
   * TODO: Replace depreciated {@code TabPanel} with 
   * {@code TabPanelLayout}
   */
  
  @UiField DecoratedTabPanel tabPanel;

  private static SetupPhasePanelUiBinder uiBinder = GWT
  .create(SetupPhasePanelUiBinder.class);

  interface SetupPhasePanelUiBinder extends UiBinder<Widget, SetupPhasePanel> {
  }

  /**
   * Have we finished setup and are waiting to move on?
   */
  private boolean done;
  
  @SuppressWarnings("deprecation")
  public SetupPhasePanel() {
    initWidget(uiBinder.createAndBindUi(this));
    
    ServerStatusManager man = ServerStatusManager.getManager();
    man.addChangeHandler(this);
    
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
    
    done = false;
    tabPanel.selectTab(0);
  }

  /**
   * Enables/Disables the widget once the setup is finished and waiting to move to the next
   * phase.
   * 
   * @param done Is the setup finished?
   */
  @SuppressWarnings("deprecation")
  protected void setupDone(boolean done) {
    int tabCount = tabPanel.getTabBar().getTabCount();
    
    for (int i=0; i < tabCount; i++) {
      tabPanel.getTabBar().setTabEnabled(i, !done);
    }

    // Make sure we're on the last tab
    if (done) {
      tabPanel.selectTab(tabCount-1);
    }
  }

  @Override
  public void change(IManager man) {
    ServerStatusManager manager = ServerStatusManager.getManager();
    
    boolean doneNow;
    
    switch(manager.getStatus()) {
    case STARTING:
    case RUNNING:
      doneNow = true;
      break;
    default:
      doneNow = false;
      break;
    }
    
    // If the state has changed
    if(doneNow != done) {
      done = doneNow;
      setupDone(done);
    }
  }
}