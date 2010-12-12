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
public class CollectionManager<Id, Man extends IManager> implements IManager{
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
  
  @Override
  public void serverChange() {
    for(Man man : managers.values()) {
      man.serverChange();
    }
  }

  @Override
  public boolean hasServerData() {
    // Decide we don't if we're missing info from any of these
    for(Man man : managers.values()) {
      if(!man.hasServerData()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean hasLocalChanges() {
    // Decide we have local changes if any of the items have local changes
    for(Man man : managers.values()) {
      if(man.hasLocalChanges()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void withServerData(final PullCallback callback)
      throws MissingRequesterException {
    // Make a set of all the items missing data
    
    final Set<Man> missingSet = new HashSet<Man>();
    
    for(Man man : managers.values()) {
      if(!man.hasServerData()) {
        missingSet.add(man);
      }
    }
    
    new CollectionCallRecorder(missingSet, new ImitatorPushCallback(callback)) {
      @Override
      protected void call(Man man, PushCallback generatedCb)
          throws MissingRequesterException {
        man.withServerData(generatedCb);
      }
    }.start();
  }

  @Override
  public void getServerData() throws MissingRequesterException {
    withServerData(null);
  }

  @Override
  public void pushLocalData(final PushCallback callback)
      throws MissingRequesterException {
    Set<Man> changeSet = new HashSet<Man>();
    
    for(Man man : managers.values()) {
      if(man.hasLocalChanges()) {
        changeSet.add(man);
      }
    }
    
    new CollectionCallRecorder(changeSet, callback) {
      @Override
      protected void call(Man man, PushCallback generatedCb) throws MissingRequesterException {
        man.pushLocalData(generatedCb);
      }
    }.start();
  }

  /**
   * Makes a number of method calls to various objects and 
   */
  protected abstract class CollectionCallRecorder {
    private final Set<Man> toCall;
    private final PushCallback callback;
    private boolean failed;
    
    /**
     * Create a call recorder that will call methods on all of the set given
     * and return after all responses have been received.
     * @param toCall A set of all the managers to call. This will be modified.
     * @param callback A callback to call with the response.
     */
    public CollectionCallRecorder(Set<Man> toCall, PushCallback callback) {
      this.toCall = toCall;
      this.callback = callback;
      this.failed = false;
    }
    
    /**
     * Fire off all of the requests, only call callback once all have returned
     */
    public void start() throws MissingRequesterException {
      if(toCall.isEmpty()) {
        cb();
        return;
      }
      
      for(final Man man : toCall) {
        call(man, new PushCallback() {
          @Override
          public void got() {
            checkOff(man);
          }
          
          @Override
          public void failed() {
            failed = true;
            checkOff(man);
          }
        });
      }
    }
    
    /**
     * Called to check off a manager from the list and process if we're done
     * @param man Manager to check off
     */
    private void checkOff(Man man) {
      toCall.remove(man);
      if(toCall.isEmpty()) {
        cb();
      }
    }
    
    private void cb() {
      if(callback != null) {
        if(failed) {
          callback.failed();
        }
        else {
          callback.got();
        }
      }
    }
    
    /**
     * Call the required method.
     * @param man The manager to call the method on.
     * @param generatedCb The callback to hand to the function.
     */
    protected abstract void call(Man man, PushCallback generatedCb) throws MissingRequesterException;
  }
}
