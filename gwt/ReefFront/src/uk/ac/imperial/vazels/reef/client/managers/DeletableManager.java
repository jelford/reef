package uk.ac.imperial.vazels.reef.client.managers;

public interface DeletableManager extends IManager {
  /**
   * After calling this, the manager will delete its object on the next push.
   */
  public void requestDeletion();
  
  /**
   * Check if the object has been deleted.
   * When this returns true, other manager getters and setters may not work.
   * @return {@code true} if this manager is waiting to delete its object,
   * Or if it has already been deleted.
   */
  public boolean pendingDelete();
}
