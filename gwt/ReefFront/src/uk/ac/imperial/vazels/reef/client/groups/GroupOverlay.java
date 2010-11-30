package uk.ac.imperial.vazels.reef.client.groups;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay for the group data as returned by the single group handler.
 */
public class GroupOverlay extends JavaScriptObject{
  protected GroupOverlay(){}
  
  /**
   * Get the name of the group.
   * @return name of the group.
   */
  public native final String getName() /*-{
    return this.name;
  }-*/;
  
  /**
   * Get the size of the group.
   * @return size of the group.
   */
  public native final int getSize() /*-{
    return this.size;
  }-*/;
  
  /**
   * Get the array of workloads.
   * @return array of workloads.
   */
  public native final JsArrayString getWorkloads() /*-{
    return this.workloads;
  }-*/;
  
  /**
   * Get the array of filters.
   * @return array of filters.
   */
  public native final JsArrayString getFilters() /*-{
    return this.filters;
  }-*/;
}
