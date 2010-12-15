package uk.ac.imperial.vazels.reef.client.output;

/**
 * Describes a variable at a particular point in time.
 */
public class SnapshotData {
  private final SnapshotOverlay data;
  
  /**
   * This is expected to be called only from {@link TimeSeries}
   * @param data Init data.
   */
  SnapshotData(SnapshotOverlay data) {
    this.data = data;
  }
  
  /**
   * Get a string representation of the type of this snapshot.
   * @return A type.
   */
  public String getType() {
    return data.getType();
  }
  
  /**
   * Get this as a float.
   * @return The value of this snapshot as a float.
   */
  public float getFloat() {
    return data.getDouble();
  }
  
  /**
   * Get this as a string.
   * @return The value of this snapshot as a string.
   */
  public String getString() {
    return data.getString();
  }
  
  /**
   * Get the name of the actor that dropped this snapshot.
   * @return Actor name.
   */
  public String getActor() {
    return data.getActor();
  }
}
