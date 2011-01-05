package uk.ac.imperial.vazels.reef.client.actors;

import com.google.gwt.core.client.JavaScriptObject;

public class ActorOverlay extends JavaScriptObject {
  protected ActorOverlay() {}

  public final native String getName() /*-{
      return this.name;
    }-*/;

  public final native String getType() /*-{
      return this.type;
    }-*/;
}
