package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;

/**
 * Just a button to stop the server. For simplicity.
 */
public class StopServerButton extends Composite {
  /**
   * 
   */
  private Button stopBtn = new Button("Stop Experiment");
  
  public StopServerButton() {
    initWidget(stopBtn);
    
    stopBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ControlCentreManager.getManager().stop();
      }
    });
  }

}
