package uk.ac.imperial.vazels.reef.client.managers;

/**
 * Handles changes made on a manager.
 * It is best not to push or pull data here, let the manager doing the changing do that.
 */
public interface ManagerChangeHandler {
  /**
   * Called whenever a change is made that effects local data.
   * @param man The manager being changed.
   */
  public void change(IManager man);
}
