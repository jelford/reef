package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay for the level below the root when output is requested from an experiment.
 * This holds a list of hosts with their data.
 */
public class GroupDataOverlay extends JavaScriptObject {
  
  protected GroupDataOverlay() { }

  /**
   * Get a list of the host ids in this group.
   * @return Array of host IDs.
   */
  public native final JsArrayString keys() /*-{
    var keys = [];
    
    for(key in this) {
      keys.push(key);
    }
    
    return keys;
  }-*/;
  
  /**
   * Get the variables left by this host.
   * @param key The host to check.
   * @return A list of variables.
   */
  public native final HostDataOverlay get(String key) /*-{
    return this[key];
  }-*/;
}
