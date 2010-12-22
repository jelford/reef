package uk.ac.imperial.vazels.reef.client.ui;

import uk.ac.imperial.vazels.reef.client.util.NotInitialisedException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Widget;

public class SetupPhasePanel extends Composite {
  /*
   * TODO: Replace depreciated {@code TabPanel} with 
   * {@code TabPanelLayout}
   */
  
  @UiField DecoratedTabPanel tabPanel;

  private static SetupPhasePanelUiBinder uiBinder = GWT
  .create(SetupPhasePanelUiBinder.class);

  interface SetupPhasePanelUiBinder extends UiBinder<Widget, SetupPhasePanel> {
  }

  @SuppressWarnings("deprecation")
  private SetupPhasePanel(MainReefPanel top) {
    initWidget(uiBinder.createAndBindUi(this));
    tabPanel.selectTab(0);
  }

  @SuppressWarnings("deprecation")
  public void setupDone(boolean done) {
    int tabCount = tabPanel.getTabBar().getTabCount();
    for (int i=0; i < tabCount; i++) {
      tabPanel.getTabBar().setTabEnabled(i, !done);
    }

    // Make sure we're on the last tab
    if (done) {
      tabPanel.selectTab(tabCount-1);
    }
  }

  private static SetupPhasePanel sInstance;

  public static SetupPhasePanel getInstance(MainReefPanel top) {
    if (sInstance == null) {
      sInstance = new SetupPhasePanel(top);
    }
    return sInstance;
  }

  public static SetupPhasePanel getInstanceOrThrow() throws NotInitialisedException {
    if (sInstance == null) {
      throw new NotInitialisedException("You must properly initialise the SetupPhasePanel.");
    }
    return sInstance;
  }
}