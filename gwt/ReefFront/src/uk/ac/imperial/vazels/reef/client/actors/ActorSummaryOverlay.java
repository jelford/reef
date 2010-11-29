package uk.ac.imperial.vazels.reef.client.actors;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ActorSummaryOverlay extends JavaScriptObject {
    protected ActorSummaryOverlay(){}
    
    public native final JsArrayString getArray() /*-{
      return this;
    }-*/; //perhaps needs parsing since its just a string atm
    
  //  public native final String lookup(int n) /*-{
  //    return this.get(n);
  //  }-*/
    
//    public native final int lookup(String key) /*-{
      // There's a chance this has been passed as a string; let's not play that game
 //     return parseInt(this[key]) 
  //  }-*/;
  
}
