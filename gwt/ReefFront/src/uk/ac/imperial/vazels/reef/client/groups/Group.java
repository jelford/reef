package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashSet;
import java.util.Set;

public class Group {
  private Set<String> workloads;
  private Set<String> filters;
  private final String name;
  private int size;
  
  public Group(final String name, final int size) {
    this.name = name;
    this.size = size;
    this.workloads = new HashSet<String>();
    this.filters = new HashSet<String>();
  }

  /**
   * Add workload to the group.
   * @param wkld The name of the workload to add
   * @return {@code true} if the workload was newly added
   */
  public boolean addWorkload(String wkld) {
    return workloads.add(wkld);
  }
  
  /**
   * Remove workload from a group.
   * @param wkld The name of the workload to remove
   * @return {@code true} if the workload used to be attached to this group.
   */
  public boolean remWorkload(String wkld) {
    return workloads.remove(wkld);
  }
  
  /**
   * Get the size of this group
   * @return group size
   */
  public int getSize() {
    return size;
  }

  /**
   * Set this size of this group
   * @param size group size
   */
  public void setSize(int size) {
    this.size = size;
  }

  /**
   * Get the name of this group.
   * @return group name
   */
  public String getName() {
    return name;
  }
}
