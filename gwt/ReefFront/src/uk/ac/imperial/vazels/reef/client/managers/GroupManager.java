package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Timer;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.groups.Group;
import uk.ac.imperial.vazels.reef.client.groups.GroupSummary;

/**
 * Should do all communication involving groups on the server.
 * 
 * Caches results to make everything behave pleasantly.
 */
public class GroupManager {
  private static GroupManager manager = null;
  
  private Map<String, SingleGroupManager> groups = null;
  
  // Not using interface list so we keep the removeFirst method
  private LinkedList<SyncCallback> syncWaitList = null;
  
  private GroupSummaryRequest summaryRequest;
  private GroupSummaryUpdate summaryUpdater;
  private boolean remoteInit;
  private boolean localChange;
  
  //Singleton stuff
  
  private GroupManager() {
    clearGroups();
    summaryRequest = new GroupSummaryRequest();
    summaryUpdater = new GroupSummaryUpdate();
    remoteInit = false;
    localChange = false;
    doWhenInited();
  }
  
  /**
   * Gets the singleton instance of the manager
   * @return singleton group manager
   */
  public static GroupManager getManager() {
    if(manager == null) {
      manager = new GroupManager();
    }
    return manager;
  }
  
  // Data getters
  
  /**
   * Is the group summary in sync with the server.
   * This includes the group names and sizes.
   * @return is the server synced.
   */
  public boolean isInitialised() {
    return remoteInit;
  }
  
  /**
   * Has the local representation changed since data was retrieved from the server?
   * @return true if the local data has changed.
   */
  public boolean hasLocalChange() {
    return localChange;
  }
  
  /**
   * Get a set of group names, this can be iterated over.
   * 
   * @return Set of group names
   */
  public Set<String> getNames(){
    return groups.keySet();
  }
  
  /**
   * Get the data for a particular group.
   * 
   * @param name is the name of the group
   * @return a {@link Group} object describing the group.
   */
  public SingleGroupManager getGroupManager(String name) {
    return groups.get(name);
  }
  
  // Server interaction
  
  /**
   * Get the newest info from the server.
   * It is safer to use the {@link GroupManager#doWhenInited(SyncCallback)} method.
   * 
   * @param handler handles the response of the pull.
   * On success, data is automatically cached.
   */
  public void pull(RequestHandler<GroupSummary> handler) {
    summaryRequest.go(handler);
  }
  
  /**
   * As in {@link GroupManager#pull(RequestHandler)} but with no handler.
   */
  public void pull() {
    pull(null);
  }
  
  /**
   * Send all current info to the server.
   * 
   * @param handler handles the response of the push.
   * The response is handled exactly as in {@link GroupManager#pull}.
   */
  public void push(RequestHandler<GroupSummary> handler) {
    summaryUpdater.go(handler);
  }
  
  /**
   * As in {@link GroupManager#push(RequestHandler)} but with no handler.
   */
  public void push() {
    push(null);
  }
  
  /**
   * Try to pull data from the server and call this callback when done.
   * This method ensures that only one request (from this method) is pending at any time.
   * 
   * If the server data has already been pulled then just run.
   * 
   * @param callback The callback to run when the request returns successfully.
   */
  public void doWhenInited(SyncCallback callback) {
    // If we are inited then just run it.
    if(remoteInit) {
      if(callback != null) {
        callback.go();
      }
      return;
    }
    
    if(syncWaitList == null) {
      // Need to create the list and send a sync request
      syncWaitList = new LinkedList<GroupManager.SyncCallback>();
      syncWaitList.add(callback);
      
      pull(new RequestHandler<GroupSummary>() {
        @Override
        public void handle(GroupSummary reply, boolean success, String message) {
          if(success) {
            justSynced();
          }
          else {
            // Wait 5 seconds and try sending the request again
            new Timer() {
              @Override
              public void run() {
                doWhenInited();
              }
            }.schedule(5000);
          }
        }
      });
    }
    else {
      syncWaitList.add(callback);
    }
  }
  
  /**
   * Exactly as {@link GroupManager#doWhenInited(SyncCallback)} but with no handler.
   * This just means only one pull request will be pending at a time.
   */
  public void doWhenInited() {
    doWhenInited(null);
  }
  
  /**
   * Callback for {@link GroupManager#doWhenInited(SyncCallback)}
   */
  public interface SyncCallback {
    public void go();
  }
  
  /**
   * Called when doWhenSynced has finished syncing
   */
  private void justSynced() {
    while(!syncWaitList.isEmpty()) {
      SyncCallback cb = syncWaitList.removeFirst();
      if(cb != null) {
        cb.go();
      }
    }
    syncWaitList = null;
  }
  
  // User interaction
  
  /**
   * Clear the group map.
   * This does not actually delete the groups, just forgets they were ever here.
   */
  private void clearGroups() {
    groups = new HashMap<String, SingleGroupManager>();
    // localChange = true;
    // Assume no local change as we haven't deleted
  }
  
  /**
   * Delete each individual group (currently by setting their size to 0)
   */
  public void deleteGroups() {
    for(String group : groups.keySet()) {
      deleteGroup(group);
    }
  }
  
  /**
   * Try to add a group to the list on the client side.
   * @param g The group to add.
   * @return If the add was successful...
   * ({@code false} indicates the group already existed)
   */
  public boolean addGroup(String name, int size) {
    if(groups.containsKey(name)) {
      return false;
    }
    SingleGroupManager gm = new SingleGroupManager(name, size);
    groups.put(name, gm);
    localChange = true;
    return true;
  }
  
  /**
   * Delete a group from the list on the client side
   * @param name The name of the group.
   * @return {@code true} if the group existed and was deleted.
   */
  public boolean deleteGroup(String name) {
    if(!groups.containsKey(name)) {
      return false;
    }
    groups.get(name).setSize(0);
    localChange = true;
    return true;
  }
  
  /**
   * Called on receipt of a new group summary object.
   * It is used for any processing or caching.
   * 
   * @param summary the group summary
   */
  protected void gotGroupSummary(GroupSummary summary){
    // Create a new group map, move only the old groups
    // that are needed into the new map.
    
    // JavaScript cannot ever be threaded so we are safe
    // that nothing else will be called during this function...hence
    final Map<String, SingleGroupManager> oldGroups = groups;
    clearGroups();
    
    for(String group : summary.keySet()) {
      if(oldGroups.containsKey(group)) {
        // Move group from old map to new
        SingleGroupManager g = oldGroups.get(group);
        g.setSize(summary.get(group));
        groups.put(group, g);
      } 
      else {
        // Add new group
        addGroup(group, summary.get(group));
      }
    }
    
    localChange = false;
    remoteInit = true;
  }
  
  /**
   * Helper class to send requests to get group info (this will send a batch
   * request to the server, and retrieve a summary of all group names and sizes).
   */
  protected class GroupSummaryRequest extends MultipleRequester<GroupSummary>{
    GroupSummaryRequest() {
      super(RequestBuilder.GET, "/groups/", 
          new Converter<GroupSummary>() {
        @Override
        public GroupSummary convert(String original) {
          return new GroupSummary(original);
        }
      });
    }

    @Override
    protected void received(GroupSummary reply, boolean success, String message) {
      if(success) {
        gotGroupSummary(reply);
      }
    }
  }
  
  
  /**
   * Post all the current group data to the server & replace the local data with
   * the returned info from the server (i.e. check that local and remote records
   * are the same).
   */
  private class GroupSummaryUpdate extends MultipleRequester<GroupSummary>{
    public GroupSummaryUpdate() {
      super(RequestBuilder.POST, "/groups/", new Converter<GroupSummary>(){
        @Override
        public GroupSummary convert(String original) {
          return new GroupSummary(original);
        }
      });
    }

    protected QueryArg[] getArgs() {
      // Construct an array of QueryArgs we'll use for our post request
      QueryArg[] queryArguments = new QueryArg[groups.size()];
      int index = 0;
      for (String groupName : groups.keySet()) {
        SingleGroupManager man = groups.get(groupName);
        if(man.getSize() == 0) {
          queryArguments[index] = new DeleteQuery(groupName);
        }
        else {
          queryArguments[index] = new AddEditQuery(groupName, man.getSize());
        }
        index++;
      }

      return queryArguments;
    }

    @Override
    protected void received(GroupSummary reply, boolean success, String message) {
      if(success) {
        gotGroupSummary(reply);
      }
      else {
        // We don't know what the server state is now...
        remoteInit = false;
      }
    }
    
    protected class AddEditQuery extends QueryArg {
      public AddEditQuery(String name, int value) {
        super(name, Integer.toString(value));
      }
    }
    
    protected class DeleteQuery extends QueryArg {
      public DeleteQuery(String name) {
        super(name, "0");
      }
    }
  }

}
