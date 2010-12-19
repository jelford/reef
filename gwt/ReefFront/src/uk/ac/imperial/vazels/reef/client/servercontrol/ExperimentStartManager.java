package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

public class ExperimentStartManager extends Manager<Void, Void> {
  /**
   * URI to start the experiment running.
   */
  private static final String SERVER_RUN_URI="/control/startexperiment";
  
  /**
   * Server-related strings.
   */
  private static final ServerControlStrings sStringConstants = 
    (ServerControlStrings) GWT.create(ServerControlStrings.class);
  
  /**
   * Singleton instance of the manager.
   */
  private static ExperimentStartManager manager = null;
  
  private ExperimentStartManager() {
    super();
    setPusher(new StartExperiment());
  }
  
  /**
   * Get the singleton instance of this class.
   * @return singleton instance.
   */
  public static ExperimentStartManager getManager() {
    if(manager == null) {
      manager = new ExperimentStartManager();
    }
    return manager;
  }
  
  @Override
  protected boolean receivePullData(Void pulled) {
    return false;
  }

  @Override
  protected boolean receivePushData(Void pushed) {
    ServerStatusManager.getManager().waitForChange();
    return false;
  }
  
  /**
   * Keep trying to start the experiment until we get a successful response.
   * <p>
   * To send a single request use {@link Manager#pushLocalData}.
   */
  public void startExperiment() {
    try {
      pushLocalData(new PushCallback() {
        @Override
        public void got() {}
        
        @Override
        public void failed() {
          Window.alert(sStringConstants.startExperimentFailed());
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }

  /**
   * Request to start an experiment.
   */
  protected class StartExperiment extends MultipleRequester<Void> {
    public StartExperiment() {
      super(RequestBuilder.POST, SERVER_RUN_URI, null);
    }
  }
}
