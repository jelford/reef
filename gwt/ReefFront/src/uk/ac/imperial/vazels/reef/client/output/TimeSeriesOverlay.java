package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;

/**
 * An overlay for a time series. This is part of the result when you want output data from an experiment.
 */
public class TimeSeriesOverlay extends JavaScriptObject {
  
  protected TimeSeriesOverlay() { }
  
  /**
   * Get a list of time stamps stored here.
   * @return Array of integers indicating the timestamp.
   */
  native final JsArrayInteger timeStamps() /*-{
    var timeStamps = [];
  
    for(timeStamp in this) {
      timeStamps.push(parseInt(timeStamp));
    }
    
    return timeStamps;
   }-*/;

  /**
   * Get data at a specific time stamp.
   * @param index Time stamp to request.
   * @return Data from a particular time stamp.
   */
   native final SnapshotOverlay get(int index) /*-{
     return this[index];
   }-*/;
}
