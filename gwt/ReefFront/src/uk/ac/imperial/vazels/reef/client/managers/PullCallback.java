package uk.ac.imperial.vazels.reef.client.managers;

/**
 * Callback interface for methods that wait for synchronisation.
 */
public interface PullCallback {
  /**
   * Called after sync.
   */
  public void got();
}
