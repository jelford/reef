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

  public RunningPhasePanel() {
    initWidget(uiBinder.createAndBindUi(this));
  }

}
