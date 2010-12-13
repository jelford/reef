package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class TimeSeriesOverlay extends JavaScriptObject {
  
  protected TimeSeriesOverlay() { }
  
  native final JsArrayString timeStamps() /*-{
    
    var timeStamps = [];
  
    for(timeStamp in this) {
      timeStamps.push(timeStamp);
    }
    
    return timeStamps;
    
   }-*/;

   native final TimeStamp get(String index) /*-{
     return this[index];
   }-*/;
}
