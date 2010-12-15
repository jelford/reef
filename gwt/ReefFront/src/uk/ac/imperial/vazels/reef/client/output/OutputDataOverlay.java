package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Overlay for the type returned when output is requested from an experiment.
 */
public class OutputDataOverlay extends JavaScriptObject {

  protected OutputDataOverlay() {}
  
  /**
   * Gets all keys, these happen to be group ids.
   * @return An array of keys.
   */
  public native final JsArrayString keys() /*-{
    var keys = [];
    
    for(key in this) {
      keys.push(key);
    }
    
    return keys;
  }-*/;
  
  /**
   * Gets info on an individual group.
   * @param key The group to get the info for.
   * @return Group info.
   */
  public native final GroupDataOverlay get(String key) /*-{
        return this[key];
  }-*/;
}
