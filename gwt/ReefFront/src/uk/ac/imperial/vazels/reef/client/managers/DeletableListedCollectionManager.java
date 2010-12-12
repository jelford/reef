package uk.ac.imperial.vazels.reef.client.managers;

import java.util.Set;

public abstract class DeletableListedCollectionManager<Id, Man extends DeletableManager> extends ListedCollectionManager<Id, Man>{
  private DeletableCollectionManager<Id, Man> collectionManager;
  
  public DeletableListedCollectionManager() {
    collectionManager = null;
  }
  
  /**
   * Try to delete an item.
   * @param id The id of the manager to remove.
   * @return {@code true} if the manager existed and was removed.
   */
  protected boolean removeItem(Id id) {
    return getCollectionManager().deleteManager(id);
  }
  
  @Override
  protected DeletableCollectionManager<Id, Man> getCollectionManager() {
    if(collectionManager == null) {
      collectionManager = new DeletableCollectionManager<Id, Man>();
    }
    return collectionManager;
  }
  
  /**
   * Get ids for all the non-deleted items.
   * @return set of ids.
   */
  protected Set<Id> getNonDeletedItems() {
    return getCollectionManager().getNonDeletedManagers();
  }
}
