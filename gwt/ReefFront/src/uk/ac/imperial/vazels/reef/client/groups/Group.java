package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Holds a group, as represented by an underlying GroupOverlay.
 */
public class Group {
  private Set<String> workloads;
  private Set<String> sue_components;
  private final String name;
  private int size;
  
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
    this.sue_components = new HashSet<String>();
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
    
    JsArrayString nSueComponents = overlay.getSueComponents();
    this.sue_components = new HashSet<String>();
    for (int i=0; i<nSueComponents.length(); i++) {
      this.sue_components.add(nSueComponents.get(i));
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
   * @return An array of workload names.
   */
  public String[] getWorkloads() {
    String[] workloadArray = new String[workloads.size()];
    return workloads.toArray(workloadArray);
  }
  
  /**
   * Add a SUE component to the group.
   * @param sc The name of the SueComponent to add
   * @return {@code true} iff the SueComponent is newly added.
   */
  public boolean addSueComponent(String sc) {
    return sue_components.add(sc);
  }
  
  /**
   * Remove a SUE component from the group
   * @param sc The name of the SueComponent to remove
   * @return {@code true} iff the component used to be attached to this group.
   */
  public boolean remSueComponent(String sc) {
    return sue_components.remove(sc);
  }
  
  /**
   * Get an array of all the SueComponents.
   * This array does not hold any references to the data in the array.
   * @return An array of SueComponent names.
   */
  public String[] getSueComponents() {
    String [] sueComponentArray = new String[sue_components.size()];
    return sue_components.toArray(sueComponentArray);
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
