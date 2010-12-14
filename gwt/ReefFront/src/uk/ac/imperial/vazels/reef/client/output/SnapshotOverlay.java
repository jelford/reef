package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Describes a variable at a point in time.
 */
public class SnapshotOverlay extends JavaScriptObject {
  
  protected SnapshotOverlay() { }
  
  /**
   * Get the type of this variable.
   * @return A string representing the type of this variable.
   */
  public native final String getType() /*-{
    return this.type;
  }-*/;
  
  /**
   * Get this variable as a double.
   * @return the floating point representation of this variable.
   */
  public native final float getDouble() /*-{
    return parseFloat(this.value);
  }-*/;
  
  /**
   * Get this variable as a string.
   * @return The string representation of this variable.
   */
  public native final String getString() /*-{
    return this.value;
  }-*/;
  
  /**
   * Get the actor this came from.
   * @return The actor name.
   */
  public native final String getActor() /*-{
    return this.actor;
  }-*/;
}
