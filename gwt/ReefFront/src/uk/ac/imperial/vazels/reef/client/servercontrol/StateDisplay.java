package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Keeps an up to date display of what the server is doing.
 */
public class StateDisplay extends Composite implements ManagerChangeHandler {
  private Label text = new Label();
  
  public StateDisplay() {
    initWidget(text);
    ServerStatusManager man = ServerStatusManager.getManager();
    man.setAutoRefresh(true);
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
  }

  /**
   * Updates server state message.
   */
  @Override
  public void change(IManager man) {
    String newText = "The vazels system ";
    
    switch(ServerStatusManager.getManager().getStatus()) {
    case EXPERIMENT:
      newText += "is running an experiment.";
      break;
    case FINISHED:
      newText += "is finished.";
      break;
    case READY:
      newText += "is ready to start.";
      break;
    case RUNNING:
      newText += "is running.";
      break;
    case STARTING:
      newText += "is starting.";
      break;
    case TIMEOUT:
      newText += "has timed out.";
      break;
    default:
      newText += "is in an unknown state at this time.";
      break;
    }
    
    text.setText(newText);
  }
}
