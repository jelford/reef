package uk.ac.imperial.vazels.reef.client.managers;

import java.util.LinkedList;

import com.google.gwt.user.client.Timer;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

public abstract class Manager<PullData, PushData> {
  //Not using interface list so we keep the removeFirst method
  private LinkedList<SyncCallback> syncWaitList = null;
  
  /**
   * Has the remote data been pulled yet?
   * This is reset if the server data could have changed.
   */
  private boolean remoteSync;
  private boolean localChange;
  
  private MultipleRequester<PullData> puller;
  private MultipleRequester<PushData> pusher;
  
  /**
   * Init the manager.
   * You still need to call {@code Manager#setRequesters}
   * before using the class.
   */
  public Manager() {
    remoteSync = false;
    localChange = false;
  }
  
  /**
   * Set the requesters used in this class.
   * This is expected to be called before any other methods (other than the constructor).
   * 
   * @param pull Request builder for pull requests.
   * @param push Request builder for push requests.
   */
  public void setRequesters(MultipleRequester<PullData> pull, MultipleRequester<PushData> push) {
    this.puller = pull;
    this.pusher = push;
  }
  
  /**
   * Have we got the latest changes from the server.
   * Note this can be incorrect as the server may change without our knowledge.
   * 
   * @return {@code true} if we have the latest changes.
   */
  public boolean remoteSync() {
    return remoteSync;
  }
  
  /**
   * Have we changed the local data since updating from the server?
   * @return {@code true} if we have local changes.
   */
  public boolean hasLocalChanges() {
    return localChange;
  }
  
  /**
   * Called whenever a local change happens.
   */
  protected void change() {
    localChange = true;
  }
  
  /**
   * Should grab the received data and update itself accordingly.
   * @param pulled The pulled data.
   * @return {@code true} if processing the pulled data wiped any local changes.
   */
  protected abstract boolean receivePullData(PullData pulled);
  
  /**
   * Should grab the received data and update itself accordingly.
   * @param pushed The response from the pushed data.
   * @return {@code true} if the push response is the same as the pull data.
   * In this case {@code Manager#receivePullData(Object)} will do any processing.
   * You should make <strong>absolutely sure</strong> here that {@code PushData}
   * can be cast to {@code PullData}, as this is unchecked. I'm relying on you here!
   */
  protected abstract boolean receivePushData(PushData pushed);
  
  /**
   * Perform callback as soon as we are synchronised with the server.
   * If we have a {@code null} puller, then the callback will be call
   * @param callback Callback to run.
   * @throws MissingRequesterException when there is a {@code null} puller.
   */
  public void afterRemoteSync(SyncCallback callback) throws MissingRequesterException {
    if(puller == null) {
      throw new MissingRequesterException();
    }
    
    if(remoteSync()) {
      // Run immediately if in sync
      if(callback != null) {
        callback.go();
      }
    }
    else {
      // Not currently in sync
      if(syncWaitList == null) {
        // No request pending so make new one
        syncWaitList = new LinkedList<SyncCallback>();
        syncWaitList.add(callback);
        
        puller.go(new RequestHandler<PullData>() {
          @Override
          public void handle(PullData reply, boolean success, String message) {
            //Got a response
            if(success) {
              justSynced(reply);
            }
            else {
              // Wait 5 seconds and try sending the request again
              new Timer() {
                @Override
                public void run() {
                  try {
                    syncNow();
                  }
                  catch(MissingRequesterException e) {
                    // We shouldn't ever get here...unless
                    // someone changed the requesters after the first request
                  }
                }
              }.schedule(5000);
            }
          }
        });
      }
      else {
        // Request pending, just add to call list
        syncWaitList.add(callback);
      }
    }
  }
  
  /**
   * Keeps trying to pull server data until it works.
   * @throws MissingRequesterException when there is a {@code null} puller.
   */
  public void syncNow() throws MissingRequesterException {
    afterRemoteSync(null);
  }
  
  /**
   * Called whenever the server data is finally pulled from
   * {@link Manager#afterRemoteSync(SyncCallback)}
   */
  private void justSynced(PullData pulled) {
    if(receivePullData(pulled)) {
      localChange = false;
    }
    remoteSync = true;
    while(!syncWaitList.isEmpty()) {
      SyncCallback cb = syncWaitList.removeFirst();
      if(cb != null) {
        cb.go();
      }
    }
    syncWaitList = null;
  }
  
  /**
   * Send a push request to the server.
   * @param callback The callback to handle the response.
   * @throws MissingRequesterException when there is a {@code null} pusher.
   */
  public void pushToServer(final PushCallback callback) throws MissingRequesterException {
    if(pusher == null) {
      throw new MissingRequesterException();
    }
    
    pusher.go(new RequestHandler<PushData>() {
      @Override
      @SuppressWarnings("unchecked") // For pull data cast
      public void handle(PushData reply, boolean success, String message) {
        if(success) {
          // Got a successful response
          
          if(receivePushData(reply)) {
            // Use pull receiver to handle
            if(receivePullData((PullData)reply)) {
              localChange = false;
            }
            remoteSync = true;
          }
          else {
            remoteSync = false;
          }
          
          if(callback != null) {
            callback.go();
          }
        }
        else {
          // Didn't get a successful response
          remoteSync = false; // We don't know what happened
          if(callback != null) {
            callback.failed();
          }
        }
      }
    });
  }
  
  /**
   * Callback interface for methods that wait for synchronisation.
   */
  public interface SyncCallback {
    public void go();
  }
  
  /**
   * Callback for a push response, it extends {@link SyncCallback}
   * so that if push and pull return the same data, this interface
   * can be used for both.
   */
  public static interface PushCallback extends SyncCallback {
    public void failed();
  }
  
  /**
   * Raised when trying to push or pull without the requesters.
   */
  public static class MissingRequesterException extends Exception {
    private static final long serialVersionUID = 1L;
  }
}
