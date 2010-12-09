package uk.ac.imperial.vazels.reef.client.managers;

import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.groups.GroupSummary;

/**
 * Should do all communication involving groups on the server.
 * 
 * Caches results to make everything behave pleasantly.
 */
public class GroupManager extends ListedCollectionManager<String, SingleGroupManager> {
  private static GroupManager manager = null;
  
  //Singleton stuff
  
  private GroupManager() {
    setPuller(new GroupSummaryRequest());
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
   * Construct a set of live groups, this can be iterated over.
   * 
   * @return Set of group names.
   */
  public Set<String> getNames(){
    return getItems();
  }
  
  /**
   * Get the manager for a particular group.
   * 
   * @param name is the name of the group
   * @return a {@link SingleGroupManager} controlling the group.
   */
  public SingleGroupManager getGroupManager(String name) {
    return getItem(name);
  }
  
  // User interaction
  
  /**
   * Delete each individual group
   */
  public void deleteGroups() {
    for(String group : getNames()) {
      deleteGroup(group);
    }
  }
  
  /**
   * Try to add a group to the list on the client side.
   * @param name The group to add.
   * @return The new manager if the add was successfull...
   * ({@code null} indicates the group already existed)
   */
  public SingleGroupManager addGroup(String name) {
    return addItem(name);
  }
  
  /**
   * Delete a group from the list on the client side
   * @param name The name of the group.
   * @return {@code true} if the group existed and was deleted.
   */
  public boolean deleteGroup(String name) {
    return removeItem(name);
  }

  @Override
  protected SingleGroupManager createManager(String id, boolean nMan) {
    return new SingleGroupManager(id, nMan);
  }
  
  /**
   * Helper class to send requests to get group info (this will send a batch
   * request to the server, and retrieve a summary of all group names and sizes).
   */
  protected class GroupSummaryRequest extends MultipleRequester<Set<String>>{
    GroupSummaryRequest() {
      super(RequestBuilder.GET, "/groups/", 
          new Converter<Set<String>>() {
        @Override
        public Set<String> convert(String original) {
          return new GroupSummary(original).getSet();
        }
      });
    }
  }
}
