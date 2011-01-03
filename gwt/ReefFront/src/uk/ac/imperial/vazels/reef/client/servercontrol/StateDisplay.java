package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * Keeps an up to date display of what the server is doing.
 */
public class StateDisplay extends Composite implements ManagerChangeHandler {
  private Label text = new Label();
  
  /**
   * Server-related strings.
   */
  private static final ServerControlStrings sStringConstants = 
    (ServerControlStrings) GWT.create(ServerControlStrings.class);
  
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
    String newText = sStringConstants.theVazelsSystemStatus();
    
    switch(ServerStatusManager.getManager().getStatus()) {
    case EXPERIMENT:
      newText += sStringConstants.isRunningExperiment();
      break;
    case FINISHED:
      newText += sStringConstants.isFinished();
      break;
    case READY:
      newText += sStringConstants.isReady();
      break;
    case RUNNING:
      newText += sStringConstants.isRunning();
      break;
    case STARTING:
      newText += sStringConstants.isStarting();
      break;
    case TIMEOUT:
      newText += sStringConstants.hasTimedOut();
      break;
    default:
      newText += sStringConstants.hasUnknownState();
      break;
    }
    
    text.setText(newText);
  }
}
