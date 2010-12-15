package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Holds a group, as represented by an underlying GroupOverlay.
 */
public class Group {
  private Set<String> workloads;
  private Set<String> filters;
  private final String name;
  private int size;
  private String restrictions;
  
  /**
   * Create a synthetic group.
   * It will have empty filter and workload list.
   * @param name Name of the group.
   * @param size Size of the group.
   */
  public Group(final String name, final int size) {
    this.name = name;
    this.size = size;
    this.workloads = new HashSet<String>();
    this.filters = new HashSet<String>();
  }
  
  /**
   * Create a new group from an overlay object.
   * @param json String representation of the JSON object.
   */
  public Group(final String json) {
    GroupOverlay overlay = parseJSON(json);
    this.name = overlay.getName();
    this.size = overlay.getSize();
    
    JsArrayString nWorkloads = overlay.getWorkloads();
    this.workloads = new HashSet<String>();
    for(int i=0; i<nWorkloads.length(); i++) {
      this.workloads.add(nWorkloads.get(i));
    }
    
    JsArrayString nFilters = overlay.getFilters();
    this.filters = new HashSet<String>();
    for(int i=0; i<nFilters.length(); i++) {
      this.filters.add(nFilters.get(i));
    }
  }
  
  /**
   * Parse a string representation of a JSON object.
   * @param json The string to convert.
   * @return An overlay to represent the group.
   */
  private native final GroupOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;

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
   * Get an array of all workloads.
   * This array does not hold any references to the data in the array.
   * Hence it can be edited.
   * @return An array of workloads.
   */
  public String[] getWorkloads() {
    String[] workloadArray = new String[workloads.size()];
    return workloads.toArray(workloadArray);
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

  public String getRestrictions() {
    return restrictions;
  }

  public void setRestrictions(final String restrictions) {
    this.restrictions = new String(restrictions);
  }
}
