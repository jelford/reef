package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.groups.Group;
import uk.ac.imperial.vazels.reef.client.groups.GroupSummary;

/**
 * Should do all communication involving groups on the server.
 * 
 * Caches results to make everything behave pleasantly.
 */
public class GroupManager extends SingleTypeManager<GroupSummary> {
  private static GroupManager manager = null;
  
  private Map<String, SingleGroupManager> groups = null;
  
  //Singleton stuff
  
  private GroupManager() {
    setRequesters(new GroupSummaryRequest(), new GroupSummaryUpdate());
    clearGroups();
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
  
  // User interaction
  
  /**
   * Clear the group map.
   * This does not actually delete the groups, just forgets they were ever here.
   */
  private void clearGroups() {
    groups = new HashMap<String, SingleGroupManager>();
    // change();
    // Assume no local change as we haven't actually deleted the groups
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
    change();
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
    change();
    return true;
  }

  protected boolean receiveData(GroupSummary pulled){
    // Create a new group map, move only the old groups
    // that are needed into the new map.
    
    // JavaScript cannot ever be threaded so we are safe
    // that nothing else will be called during this function...hence
    final Map<String, SingleGroupManager> oldGroups = groups;
    clearGroups();
    
    for(String group : pulled.keySet()) {
      if(oldGroups.containsKey(group)) {
        // Move group from old map to new
        SingleGroupManager g = oldGroups.get(group);
        g.setSize(pulled.get(group));
        groups.put(group, g);
      } 
      else {
        // Add new group
        addGroup(group, pulled.get(group));
      }
    }
    
    return true;
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
