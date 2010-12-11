package uk.ac.imperial.vazels.reef.client.workloads;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class WorkloadOverlay extends JavaScriptObject{
  protected WorkloadOverlay() {}
  
  public native String getName() /*-{
    return this.name;
  }-*/;
  
  public native JsArrayString getActors() /*-{
    return this.actors;
  }-*/;
}
