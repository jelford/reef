package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages a collection of server side objects referenced by an id.
 *
 * @param <Id> The type defining the Id of each item (key type)
 * @param <Man> The type of manager for each of the items.
 */
public class CollectionManager<Id, Man extends IManager>{
  private Map<Id, Man> managers = null;
  
  public CollectionManager() {
    managers = new HashMap<Id, Man>();
  }
  
  /**
   * Try to add a new manager.
   * @param id The id associated with this manager - don't pass null
   * @param man The actual manager - don't pass null
   * @return {@code true} if the manager was added.
   * Or {@code false} if the id was already in use.
   */
  public boolean addManager(Id id, Man man) {
    if(managers.containsKey(id)) {
      return false;
    }
    else {
      managers.put(id, man);
      return true;
    }
  }
  
  /**
   * Forget the manager is here, but don't delete it.
   * @param id The id of the manager to remove.
   * @return {@code true} if the manager existed and was removed.
   * @see DeletableCollectionManager#deleteManager(Object)
   */
  public boolean forgetManager(Id id) {
    Man man = managers.get(id);
    if(man != null) {
      managers.remove(id);
      return true;
    }
    return false;
  }
  
  /**
   * Get a set of {@link Id}s for all managers.
   * @return Set of managers.
   */
  public Set<Id> getManagers() {
    return managers.keySet();
  }
  
  /**
   * Grabs a manager if one exists for this id.
   * @param id Id for this manager.
   * @return A manager or {@code null} if none exists for the given id.
   */
  public Man getManager(Id id) {
    return managers.get(id);
  }

  public boolean hasAllServerData() {
    // Decide we don't if we're missing info from any of these
    for(Man man : managers.values()) {
      if(!man.hasServerData()) {
        return false;
      }
    }
    return true;
  }
  
  public boolean hasAnyLocalChanges() {
    // Decide we have local changes if any of the items have local changes
    for(Man man : managers.values()) {
      if(man.hasLocalChanges()) {
        return true;
      }
    }
    return false;
  }

  
  public void withAllServerData(final PullCallback callback)
      throws MissingRequesterException {
    // Make a set of all the items missing data
    
    final Set<IManager> missingSet = new HashSet<IManager>();
    
    for(Man man : managers.values()) {
      if(!man.hasServerData()) {
        missingSet.add(man);
      }
    }
    
    new CollectionCallRecorder(missingSet, new ImitatorPushCallback(callback)) {
      @Override
      protected void call(IManager man, PushCallback generatedCb)
          throws MissingRequesterException {
        man.withServerData(generatedCb);
      }
    }.start();
  }

  
  public void getServerData() throws MissingRequesterException {
    withAllServerData(null);
  }

  
  public void pushAllLocalData(final PushCallback callback)
      throws MissingRequesterException {
    Set<IManager> changeSet = new HashSet<IManager>();
    
    for(Man man : managers.values()) {
      if(man.hasLocalChanges()) {
        changeSet.add(man);
      }
    }
    
    new CollectionCallRecorder(changeSet, callback) {
      @Override
      protected void call(IManager man, PushCallback generatedCb) throws MissingRequesterException {
        man.pushLocalData(generatedCb);
      }
    }.start();
  }
  
  /**
   * Add a change handler to every manager in the collection.
   * @param handler Handler to add.
   */
  public void addChangeHandlerToAll(ManagerChangeHandler handler) {
    for(Man man : managers.values()) {
      man.addChangeHandler(handler);
    }
  }
  
  /**
   * Remove a change handler from every manager in the collection if it exists.
   * @param handler Handler to remove.
   */
  public void removeChangeHandlerFromAll(ManagerChangeHandler handler) {
    for(Man man : managers.values()) {
      man.removeChangeHandler(handler);
    }
  }
}
