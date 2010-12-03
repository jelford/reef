package uk.ac.imperial.vazels.reef.client.managers;

import com.google.gwt.user.client.Timer;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

/**
 * A base class to manage a single item.
 * For managing a group of items use {@link CollectionManager}
 *
 * @param <PullData> The type returned from a pull operation
 * @param <PushData> The type returned from a push operation
 */
public abstract class Manager<PullData, PushData> implements IManager {
  private SyncTracker syncTracker;
  
  private MultipleRequester<PullData> puller;
  private boolean pendingPull;
  private RepeatPullHandler pullHandler;
  
  private MultipleRequester<PushData> pusher;
  
  /**
   * Init the manager.
   * You still need to call {@link Manager#setPuller(MultipleRequester)}
   * and {@link Manager#setPusher(MultipleRequester)}
   * before using the class.
   */
  public Manager() {
    syncTracker = new SyncTracker();
    puller = null;
    pendingPull = false;
    pullHandler = new RepeatPullHandler();
    pusher = null;
  }
  
  /**
   * Sets the requester to use for pulling data.
   * @param pull Request builder for pull requests.
   */
  public void setPuller(MultipleRequester<PullData> pull) {
    this.puller = pull;
  }
  
  /**
   * Sets the requester to use for pushing data.
   * @param push Request builder for push requests.
   */
  public void setPusher(MultipleRequester<PushData> push) {
    this.pusher = push;
  }
  
  @Override
  public boolean hasServerData() {
    return syncTracker.hasServerData();
  }

  @Override
  public boolean hasLocalChanges() {
    return syncTracker.hasLocalChanges();
  }

  @Override
  public void withServerData(PullCallback callback)
      throws MissingRequesterException {
    // Set request going immediately if it's needed
    getServerData();
    // Now add to callback queue
    syncTracker.addServerDataCallback(callback);
  }

  @Override
  public void getServerData() throws MissingRequesterException {
    if(puller == null) {
      throw new MissingRequesterException();
    }
    
    // Don't need to do anything if we already have the data
    if(syncTracker.hasServerData())
      return;
    
    if(!pendingPull) {
      puller.go(pullHandler);
    }
  }
  
  @Override
  public void pushLocalData(final PushCallback callback)
      throws MissingRequesterException {
    if(pusher == null) {
      throw new MissingRequesterException();
    }
    
    syncTracker.serverChange(); // about to ;)
    
    // Send request
    pusher.go(new RequestHandler<PushData>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(PushData reply, boolean success, String message) {
        if(success) {
          // We pushed the data
          syncTracker.pushedToServer();
          
          if(receivePushData(reply)) {
            if(receivePullData((PullData)reply)) {
              syncTracker.wipedLocalChanges();
            }
            syncTracker.gotServerData();
          }
          
          // And try running the callback
          if(callback != null) {
            callback.got();
          }
        }
        else if(callback != null) {
          callback.failed();
        }
      }
    });
  }
  
  /**
   * Should use received data to update itself accordingly.
   * @param pulled The pulled data.
   * @return {@code true} if processing the pulled data wiped any local changes.
   */
  protected abstract boolean receivePullData(PullData pulled);
  
  /**
   * Should use the received data to update itself accordingly.
   * @param pushed The response from the pushed data.
   * @return {@code true} if the push response is the same as the pull data.
   * In this case {@link Manager#receivePullData(Object)} will do any processing.
   * You should make <strong>absolutely sure</strong> here that {@link PushData}
   * can be cast to {@link PullData}, as this is unchecked. I'm relying on you here!
   */
  protected abstract boolean receivePushData(PushData pushed);
  
  
  /**
   * Used to handle pull requests (when they retry until success).
   */
  private class RepeatPullHandler extends RequestHandler<PullData>{
    @Override
    public void handle(PullData reply, boolean success, String message) {
      if(success) {
        if(receivePullData(reply)) {
          syncTracker.wipedLocalChanges();
        }
        syncTracker.gotServerData();
        pendingPull = false;
      }
      else {
        new Timer() {
          @Override
          public void run() {
            try {
              getServerData();
            }
            catch(MissingRequesterException e) {
              // We shouldn't ever get here...unless
              // someone changed the requesters after the first request
            }
          }
        }.schedule(5000);
      }
    }
  }
}
