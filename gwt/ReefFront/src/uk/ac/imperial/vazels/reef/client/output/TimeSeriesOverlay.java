package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;

public class TimeSeriesOverlay extends JavaScriptObject {
  
  protected TimeSeriesOverlay() { }
  
  native final JsArrayInteger timeStamps() /*-{
    
    var timeStamps = [];
  
    for(timeStamp in this) {
      timeStamps.push(parseInt(timeStamp));
    }
    
    return timeStamps;
    
   }-*/;

   native final TimeStamp get(int index) /*-{
     return this[index];
   }-*/;
}
