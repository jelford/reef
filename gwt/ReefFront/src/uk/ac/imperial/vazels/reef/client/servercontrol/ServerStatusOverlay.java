package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.core.client.JavaScriptObject;

public class ServerStatusOverlay extends JavaScriptObject {
  public native final String getStatusString()/*-{
    return String(this.control_centre_status)
  }-*/;
  
  protected ServerStatusOverlay() {
    
  }
  
}
