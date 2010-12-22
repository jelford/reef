package uk.ac.imperial.vazels.reef.client.ui;

import uk.ac.imperial.vazels.reef.client.groups.AllocateGroups;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainReefPanel extends Composite {
  
  private static MainReefPanel sInstance;

  private static MainReefPanelUiBinder uiBinder = GWT
      .create(MainReefPanelUiBinder.class);

  interface MainReefPanelUiBinder extends UiBinder<Widget, MainReefPanel> {
  }
  
  @UiField Label title;
  @UiField SimplePanel placeholder;

  private MainReefPanel() {
    initWidget(uiBinder.createAndBindUi(this));
    this.setContent("Experiment Setup", new SetupPhasePanel(this));
  }

  private void setContent(String titleText, Widget w) {
    title.setText(titleText);
    placeholder.setWidget(w);
  }
  
  public void startRunningPhase() {
    this.setContent("Running phase", new AllocateGroups());
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
