package uk.ac.imperial.vazels.reef.client.actors;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Javascript overlay for returned data from the actor upload handler.
 */
public class ActorOverlay extends JavaScriptObject {
  protected ActorOverlay() {}

  /**
   * Get the name of the actor.
   * @return Actor name.
   */
  public final native String getName() /*-{
      return this.name;
    }-*/;

  /**
   * Get the type of the actor.
   * @return Actor type.
   */
  public final native String getType() /*-{
      return this.type;
    }-*/;
}
