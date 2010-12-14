package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Holds data on a specific host, consists of a list of variables.
 */
public class HostDataOverlay extends JavaScriptObject {
  protected HostDataOverlay() { }

  /**
   * Get the variables we are holding.
   * @return A list of variable names.
   */
  native final JsArrayString keys() /*-{
    var keys = [];
    
    for (key in this) {
      keys.push(key);
    }
    return keys;
  }-*/;
  
  /**
   * Get the time series for a specific variable.
   * @param key The variable we are requesting.
   * @return A time series for the specified variable.
   */
  native final TimeSeriesOverlay get(String key) /*-{
    return this[key];
  }-*/;

}
