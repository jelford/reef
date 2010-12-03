package uk.ac.imperial.vazels.reef.client.managers;

/**
 * Callback for a push response, it extends {@link PullCallback}
 * so that if push and pull return the same data, this interface
 * can be used for both.
 */
public interface PushCallback extends PullCallback {
  /**
   * Called when a push fails.
   */
  public void failed();
}