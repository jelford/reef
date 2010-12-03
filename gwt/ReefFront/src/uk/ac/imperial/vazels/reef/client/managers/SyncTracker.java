package uk.ac.imperial.vazels.reef.client.managers;

import java.util.LinkedList;

/**
 * Keep track of the synchronisation state of a manager.
 */
public class SyncTracker {
  private boolean serverData;
  private boolean localChanges;
  
  private LinkedList<PullCallback> pullWaitList;
  
  /**
   * Create the default tracker.
   * This assumes we don't have server data or local changes.
   */
  public SyncTracker() {
    this(false, false);
  }
  
  /**
   * Create the sync tracker with specific values for local and server changes.
   * @param serverData Do we have the server data?
   * @param localChanges Do we have any local changes?
   */
  public SyncTracker(boolean serverData, boolean localChanges) {
    this.serverData = serverData;
    this.localChanges = localChanges;
    this.pullWaitList = null;
  }
  
  /**
   * Have we recorded receiving the server data?
   * @return {@code true} if we think we have the server data.
   */
  public boolean hasServerData() {
    return serverData;
  }
  
  /**
   * Have we recorded changing anything locally?
   * @return {@code true} if we have changed something locally.
   */
  public boolean hasLocalChanges() {
    return localChanges;
  }
  
  /**
   * Called to indicate that we have changed something locally.
   */
  public void change() {
    localChanges = true;
  }
  
  /**
   * Called when we suspect the server has changed.
   */
  public void serverChange() {
    serverData = false;
  }
  
  /**
   * Called when the local data has been pushed to the server.
   */
  public void pushedToServer() {
    localChanges = false;
    // We probably don't know what's on the server now
    serverChange();
  }
  
  /**
   * Called when we retrieve server data.
   */
  public void gotServerData() {
    serverData = true;
    processWaitingList();
  }
  
  /**
   * Called whenever local changes are wiped.
   */
  public void wipedLocalChanges() {
    localChanges = false;
  }
  
  /**
   * Add a callback to the waiting list.
   * As soon as we know we have the server data, this is called.
   * @param callback The callback to be called.
   */
  public void addServerDataCallback(PullCallback callback) {
    if(callback != null) {
      if(hasServerData()) {
        callback.got();
      }
      else {
        if(pullWaitList == null) {
          pullWaitList = new LinkedList<PullCallback>();
        }
        pullWaitList.add(callback);
      }
    }
  }
  
  /**
   * Called whenever we retrieve server data to fire off
   * the waiting callbacks
   */
  protected void processWaitingList() {
    if(pullWaitList != null) {
      while(pullWaitList.isEmpty()) {
        PullCallback cb = pullWaitList.removeFirst();
        cb.got(); // We don't have nulls
      }
      pullWaitList = null;
    }
  }
}
