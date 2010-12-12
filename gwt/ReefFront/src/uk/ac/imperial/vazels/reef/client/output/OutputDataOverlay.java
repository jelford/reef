package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

public class OutputDataOverlay extends JavaScriptObject {

  protected OutputDataOverlay() {}
  
  public native final JsArrayString keys() /*-{
    
    var keys = [];
    
    for(key in this) {
      keys.push(key);
    }
    
    return keys;
    
  }-*/;
  
  public native final GroupsDataOverlay get(String key) /*-{
    //alert(this);
    //if(index >= 0 && index < this.length) {
        return this[key];
    //}
    
    //return this[index];
  }-*/;
  
}
