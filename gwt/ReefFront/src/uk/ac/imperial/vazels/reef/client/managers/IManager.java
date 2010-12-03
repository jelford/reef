package uk.ac.imperial.vazels.reef.client.managers;

public interface IManager {
  /**
   * Have we got the latest changes from the server.
   * Note this can be incorrect if the server has changed without our knowledge.
   * 
   * @return {@code true} if we have the latest changes.
   */
  public boolean hasServerData();
  
  /**
   * Have we changed the local data since updating from the server?
   * @return {@code true} if we have local changes.
   */
  public boolean hasLocalChanges();
  
  /**
   * Perform callback as soon as we have the server data.
   * This will send the pull request automatically.
   * @param callback Callback to run.
   * @throws MissingRequesterException when there is no puller.
   */
  public void withServerData(PullCallback callback) throws MissingRequesterException;
  
  /**
   * Keeps trying to pull server data until it works.
   * This will return immediately if we already have the server data.
   * @throws MissingRequesterException when there is no puller.
   */
  public void getServerData() throws MissingRequesterException;
  
  /**
   * Send a push request to the server.
   * @param callback The callback to handle the response.
   * @throws MissingRequesterException when there is no pusher.
   */
  public void pushLocalData(final PushCallback callback) throws MissingRequesterException;
}
