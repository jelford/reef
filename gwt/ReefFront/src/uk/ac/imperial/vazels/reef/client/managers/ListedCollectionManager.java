package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;

/**
 * Like {@link CollectionManager} but takes the ids of its managers from
 * a requester. This way we don't need to manually add all the items.
 * <p>
 * Remember that manager methods affect the collection rather than individual items.
 * Data still needs to be individually pulled or pushed for each item.
 *
 * @param <Id> The type of item ids
 * @param <Man> The type of item managers
 */
public abstract class ListedCollectionManager<Id, Man extends IManager> implements IManager {

  private Manager<Set<Id>, Void> listManager;
  private CollectionManager<Id, Man> collectionManager;
  private Set<IManager> collectionChangers;
  
  public ListedCollectionManager() {
    listManager = new ListManager();
    collectionManager = null;
    collectionChangers = new HashSet<IManager>();
  }
  
  /**
   * Set the requester to grab the ids for the available managers.
   * @param puller Request builder for the pull request.
   */
  protected void setPuller(MultipleRequester<Set<Id>> puller) {
    listManager.setPuller(puller);
  }

  /**
   * Add a new item to the list.
   * Warning: if the new manager thinks the item is already deleted,
   * your returned manager will be your only link to it.
   * @param id Id for the new manager.
   * @return New manager for the item, or {@code null} if the id was already taken.
   */
  protected Man addItem(Id id) {
    Man curMan = getCollectionManager().getManager(id);
    
    if(curMan != null)
      return null;
    
    Man man = createManager(id, true);
    getCollectionManager().addManager(id, man);
    addCollectionChanger(curMan);
    
    return man;
  }
  
  /**
   * Get ids for all the items.
   * @return set of ids.
   */
  protected Set<Id> getItems() {
    return getCollectionManager().getManagers();
  }
  
  /**
   * Grab the manager for the item with this id.
   * @param id id of the item.
   * @return a manager or {@code null} if none exists for this id.
   */
  protected Man getItem(Id id) {
    return getCollectionManager().getManager(id);
  }
  
  @Override
  public boolean hasLocalChanges() {
    return listManager.hasLocalChanges();
  }
  
  /**
   * Like {@link ListedCollectionManager#hasLocalChanges()} but
   * looks at all the child managers too.
   * @return {@code true} if there are any visible local changes.
   */
  public boolean hasAnyLocalChanges() {
    return listManager.hasLocalChanges() && getCollectionManager().hasAnyLocalChanges();
  }
  
  @Override
  public boolean hasServerData() {
    return listManager.hasServerData();
  }
  
  /**
   * Like {@link ListedCollectionManager#hasServerData()} bu
   * looks at all the child managers too.
   * @return {@code true} if there are any visible local changes.
   */
  public boolean hasAllServerData() {
    return listManager.hasServerData() && getCollectionManager().hasAllServerData();
  }
  
  @Override
  public void serverChange() {
    listManager.serverChange();
  }
  
  /**
   * Called when there has been a server change that we know of.
   * @param id The id of the changed workload
   */
  public void serverChange(Id id) {
    serverChange();
    Man man = collectionManager.getManager(id);
    if(man != null) {
      man.serverChange();
    }
  }
  
  @Override
  public void withServerData(final PullCallback callback)
    throws MissingRequesterException {
    listManager.withServerData(callback);
  }
  
  public void withAllServerData(final PullCallback callback)
      throws MissingRequesterException {
    withServerData(new PullCallback() {
      @Override
      public void got() {
        try {
          getCollectionManager().withAllServerData(callback);
        }
        catch(MissingRequesterException e) {
          // Ignore this, I can't see a good way to relay this to the user
          if(callback != null) {
            callback.got();
          }
        }
      }
    });
  }

  @Override
  public void getServerData() throws MissingRequesterException {
    listManager.getServerData();
  }
  
  /**
   * Get server data for this and all the child managers.
   * @throws MissingRequesterException
   */
  public void getAllServerData() throws MissingRequesterException {
    listManager.withServerData(new PullCallback() {
      @Override
      public void got() {
        try {
          getCollectionManager().getServerData();
        }
        catch(MissingRequesterException e) {
          // Again cannot see a nice way to relay this to the user
        }
      }
    });
  }
  
  @Override
  public void pushLocalData(PushCallback callback) {
    try {
      // This will remove managers from collectionChangers
      new CollectionCallRecorder(collectionChangers,callback) {
        @Override
        protected void call(IManager man, PushCallback generatedCb)
          throws MissingRequesterException {
          man.pushLocalData(generatedCb);
        }
      }.start();
    }
    catch(MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Push all the local data including child managers.
   * @param callback
   * @throws MissingRequesterException
   */
  public void pushAllLocalData(final PushCallback callback)
      throws MissingRequesterException {
    getCollectionManager().pushAllLocalData(new PushCallback() {
      @Override
      public void got() {
        clearCollectionChangers();
        listManager.serverChange();
        callback.got();
      }
      
      @Override
      public void failed() {
        callback.failed();
      }
    });
  }
  
  /**
   * Add a change handler to the item list. Optionally can add the handler
   * to all items too.
   * @param handler The actual change handler.
   * @param all Whether to add to all the items as well as the list.
   */
  public void addChangeHandler(ManagerChangeHandler handler, boolean all) {
    listManager.addChangeHandler(handler);
    
    if(all) {
      collectionManager.addChangeHandlerToAll(handler);
    }
  }
  
  @Override
  public void addChangeHandler(ManagerChangeHandler handler) {
    addChangeHandler(handler, false);
  }

  /**
   * Remove a change handler from the item list. Optionally can remove the handler
   * from all items too.
   * @param handler The actual change handler.
   * @param all Whether to remove from all the items as well as the list.
   */
  public void removeChangeHandler(ManagerChangeHandler handler, boolean all) {
    listManager.removeChangeHandler(handler);
    
    if(all) {
      collectionManager.removeChangeHandlerFromAll(handler);
    }
  }
  
  @Override
  public void removeChangeHandler(ManagerChangeHandler handler) {
    removeChangeHandler(handler, false);
  }

  /**
   * Takes the new set of ids and make sure the collection corresponds.
   * @param pulled the new set of ids.
   */
  protected void receivedNewIds(Set<Id> pulled) {
    Set<Id> managers = getCollectionManager().getManagers();
    
    // Create a forget list
    Set<Id> forget = new HashSet<Id>();
    for(Id id : managers) {
      if(!pulled.contains(id)) {
        forget.add(id);
      }
    }
    
    // Create add list
    Set<Id> add = new HashSet<Id>();
    for(Id id : pulled) {
      if(!managers.contains(id)) {
        add.add(id);
      }
    }
    
    // Forget now we're not iterating
    for(Id id : forget) {
      getCollectionManager().forgetManager(id);
    }
    
    // Now add all the things left to add
    for(Id id : add) {
      getCollectionManager().addManager(id, createManager(id, false));
    }
    
  }
  
  /**
   * Creates a manager for the given id.
   * @param id Id for the object to be managed.
   * @param nMan Is this a manager for a new item or one that maps to an existing one?
   */
  protected abstract Man createManager(Id id, boolean nMan);
  
  /**
   * Returns the collection manager to be used by this manager.
   * <p>
   * This can be overwritten to allow more extensive managers.
   * @return The collection manager for this manager.
   */
  protected CollectionManager<Id, Man> getCollectionManager() {
    if(collectionManager == null) {
      collectionManager = new CollectionManager<Id, Man>();
    }
    return collectionManager;
  }
  
  /**
   * Called when a manager is added or removed that would change the collection.
   * This means a real addition or deletion that would change the requested item list.
   * @param man The manager in question.
   */
  protected void addCollectionChanger(IManager man) {
    collectionChangers.add(man);
    listManager.change();
  }
  
  /**
   * Clears out the list of collection changes.
   */
  protected void clearCollectionChangers() {
    collectionChangers = new HashSet<IManager>();
  }
  
  /**
   * Manages the receipt of the list data.
   */
  protected class ListManager extends Manager<Set<Id>, Void> {
    @Override
    protected boolean receivePullData(Set<Id> pulled) {
      receivedNewIds(pulled);
      return true;
    }

    @Override
    protected boolean receivePushData(Void pushed) {
      // Unused
      return false;
    }
  }
}
