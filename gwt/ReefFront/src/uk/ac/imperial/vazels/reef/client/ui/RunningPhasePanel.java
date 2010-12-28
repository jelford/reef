package uk.ac.imperial.vazels.reef.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RunningPhasePanel extends Composite {

  private static RunningPhasePanelUiBinder uiBinder = GWT
      .create(RunningPhasePanelUiBinder.class);

  interface RunningPhasePanelUiBinder extends
      UiBinder<Widget, RunningPhasePanel> {
  }
  
  /**
   * Just in case we need the parent.
   */
  private final MainReefPanel top;

  public RunningPhasePanel(MainReefPanel top) {
    initWidget(uiBinder.createAndBindUi(this));
    this.top = top;
  }
  
  private static RunningPhasePanel sInstance;

  public static RunningPhasePanel getInstance(MainReefPanel top) {
    if (sInstance == null) {
      sInstance = new RunningPhasePanel(top);
    }
    return sInstance;
  }
}
