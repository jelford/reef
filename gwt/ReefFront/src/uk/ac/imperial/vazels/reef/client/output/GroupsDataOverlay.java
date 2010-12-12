package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class GroupsDataOverlay extends JavaScriptObject {
  
  protected GroupsDataOverlay() { }

  public native final JsArrayString keys() /*-{
  
    var keys = [];
    
    for(key in this) {
      keys.push(key);
    }
    
    return keys;
    
  }-*/;
  
  public native final VariableDataOverlay get(String key) /*-{
    return this[key];
  }-*/;
}
