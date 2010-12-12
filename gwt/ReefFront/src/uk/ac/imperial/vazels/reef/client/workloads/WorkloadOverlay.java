package uk.ac.imperial.vazels.reef.client.workloads;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class WorkloadOverlay extends JavaScriptObject{
  protected WorkloadOverlay() {}
  
  public final native String getName() /*-{
    return this.name;
  }-*/;
  
  public final native JsArrayString getActors() /*-{
    return this.actors;
  }-*/;
}
