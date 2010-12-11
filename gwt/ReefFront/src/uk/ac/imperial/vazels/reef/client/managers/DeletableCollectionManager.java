package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashSet;
import java.util.Set;

/**
 * Like a {@link CollectionManager} but allows items to be deleted.
 *
 * @param <Id> Type to represent each item
 * @param <Man> Manager for each item
 */
public class DeletableCollectionManager<Id, Man extends DeletableManager> extends CollectionManager<Id, Man>{
  /**
   * Try to delete a manager (but don't forget about it).
   * @param id The id of the manager to remove.
   * @return {@code true} if the manager existed and was removed.
   * @see CollectionManager#forgetManager(Object)
   */
  public boolean deleteManager(Id id) {
    Man man = super.getManager(id);
    
    if(man != null) {
      man.requestDeletion();
      return true;
    }

    return false;
  }
  
  /**
   * Get a set of {@link Id}s for all non-deleted managers.
   * @return Set of managers.
   */
  public Set<Id> getManagers() {
    Set<Id> liveManagers = new HashSet<Id>();
    
    // Create list of live manager (not deleted)
    for(Id id : getAllManagers()) {
      if(!super.getManager(id).pendingDelete()) {
        liveManagers.add(id);
      }
    }
    
    return liveManagers;
  }
  
  /**
   * Like {@link DeletableCollectionManager#getManagers()} but returns even deleted managers.
   * @return List of managers.
   */
  public Set<Id> getAllManagers() {
    return super.getManagers();
  }
  
  /**
   * Grabs a non-deleted manager if one exists for this id.
   * @param id Id for this manager.
   * @return A manager or {@code null} if none exists for the given id.
   */
  public Man getManager(Id id) {
    Man manager = super.getManager(id);
    
    if(manager != null && !manager.pendingDelete()) {
      return manager;
    }
    else {
      return null;
    }
  }
}
