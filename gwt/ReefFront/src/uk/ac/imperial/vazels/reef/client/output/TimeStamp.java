package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;

public class TimeStamp extends JavaScriptObject {
  
  protected TimeStamp() { }
  
  native final String getType() /*-{
    return this.type;
  }-*/;
  
  native final int getDouble() /*-{
    return parseInt(this.value);
  }-*/;
  
  native final String getString() /*-{
    return this.value;
  }-*/;
  
  native final String getActor() /*-{
    return this.actor;
  }-*/;
}
