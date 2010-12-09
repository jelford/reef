package uk.ac.imperial.vazels.reef.client.managers;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;

/**
 * Like manager, but push and pull requests return exactly the same information.
 */
public abstract class SingleTypeManager<Type> extends Manager<Type, Type> {
  /**
   * Init the manager.
   * You still need to call {@link Manager#setPuller(MultipleRequester)}
   * and {@link Manager#setPusher(MultipleRequester)}
   * before using the class.
   * @param nManager Is this a newly created manager or does it already exist on the server?
   */
  public SingleTypeManager(boolean nManager) {
    super(nManager);
  }
  
  @Override
  protected final boolean receivePullData(Type pulled) {
    return receiveData(pulled);
  }

  @Override
  protected final boolean receivePushData(Type pushed) {
    return true;
  }

  /**
   * Should grab the received data and update itself accordingly.
   * 
   * @param data The data that has been received.
   * @return {@code true} if processing the pulled data wiped any local changes.
   */
  protected abstract boolean receiveData(Type data);
}
