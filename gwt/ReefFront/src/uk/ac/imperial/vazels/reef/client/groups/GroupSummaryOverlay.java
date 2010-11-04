package uk.ac.imperial.vazels.reef.client.groups;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class GroupSummaryOverlay extends JavaScriptObject{
  protected GroupSummaryOverlay(){}
  
  public native final JsArrayString keys() /*-{
     var keys = [];
     var i=0
     for(key in this) {
       keys.push(key);
     }
     return keys;
  }-*/;
  
  public native final int lookup(String key) /*-{
    // There's a chance this has been passed as a string; let's not play that game
    return parseInt(this[key]) 
  }-*/;
}
