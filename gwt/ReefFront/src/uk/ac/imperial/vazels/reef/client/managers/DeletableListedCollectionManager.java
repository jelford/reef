package uk.ac.imperial.vazels.reef.client.managers;

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
  public boolean removeItem(Id id) {
    return collectionManager.deleteManager(id);
  }
  
  @Override
  public DeletableCollectionManager<Id, Man> getCollectionManager() {
    if(collectionManager == null) {
      collectionManager = new DeletableCollectionManager<Id, Man>();
    }
    return collectionManager;
  }
}
