package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class VariableDataOverlay extends JavaScriptObject {
  
  protected VariableDataOverlay() { }

  native final JsArrayString keys() /*-{
    
    var keys = [];
    
    for (key in this) {
      keys.push(key);
    }
    return keys;
  }-*/;
  
  native final TimeSeriesOverlay get(String key) /*-{
  
    return this[key];
  
  }-*/;

}
