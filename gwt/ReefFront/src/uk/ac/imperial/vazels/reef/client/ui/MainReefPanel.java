package uk.ac.imperial.vazels.reef.client.ui;

import uk.ac.imperial.vazels.reef.client.servercontrol.ServerControl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainReefPanel extends Composite {
  
  private static MainReefPanel sInstance;
  
  /**
   * Let's have some localised string constants!
   */
  private static final UIStrings sStringConstants =
    (UIStrings) GWT.create(UIStrings.class);

  private static MainReefPanelUiBinder uiBinder = GWT
      .create(MainReefPanelUiBinder.class);

  interface MainReefPanelUiBinder extends UiBinder<Widget, MainReefPanel> {
  }
  
  @UiField Label mTitle;
  @UiField SimplePanel mPlaceholder;

  private MainReefPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setContent(sStringConstants.experimentSetup(), SetupPhasePanel.getInstance(this));
  }

  private void setContent(String titleText, Widget w) {
    mTitle.setText(titleText);
    mPlaceholder.setWidget(w);
  }
  
  public void startRunningPhase() {
    this.setContent(sStringConstants.runningPhase(), RunningPhasePanel.getInstance(this));
    ServerControl.cancelTimers();
  }
  
  /**
   * Today, we will be accessing a class that should only exist once using the
   * "Singleton" design pattern. There's no reason to get two 
   * {@code MainReefPanel}s.
   * @return The single instance of MainReefPanel.
   */
  public static MainReefPanel getInstance() {
    if (sInstance == null) {
      sInstance = new MainReefPanel();
    }
    return sInstance;
  }
}
