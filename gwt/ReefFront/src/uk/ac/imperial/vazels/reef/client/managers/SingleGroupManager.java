package uk.ac.imperial.vazels.reef.client.managers;

import uk.ac.imperial.vazels.reef.client.groups.Group;

/**
 * Manages a group object, deals with syncing to the server.
 */
public class SingleGroupManager {
  private Group group;
  private boolean synced;
  
  /**
   * This should only ever be used inside group manager.
   * We do not ever want to manually create an instance of this class.
   * @param g Group that this manager controls.
   */
  SingleGroupManager(String name, int size) {
    this.group = new Group(name, size);
    this.synced = false;
  }
  
  public boolean isSynced() {
    return synced;
  }
  
  /**
   * Get the size of this group
   * @return group size
   */
  public int getSize() {
    return group.getSize();
  }

  /**
   * Set this size of this group
   * @param size group size
   */
  public void setSize(int size) {
    synced = false;
    group.setSize(size);
  }

  /**
   * Get the name of this group.
   * @return group name
   */
  public String getName() {
    return group.getName();
  }
  
  /**
   * Add workload to the group.
   * @param wkld The name of the workload to add
   * @return {@code true} if the workload was newly added
   */
  public boolean addWorkload(String wkld) {
    synced = false;
    return group.addWorkload(wkld);
  }
  
  /**
   * Remove workload from a group.
   * @param wkld The name of the workload to remove
   * @return {@code true} if the workload used to be attached to this group.
   */
  public boolean remWorkload(String wkld) {
    synced = false;
    return group.remWorkload(wkld);
  }
}
