package uk.ac.imperial.vazels.reef.client.managers;

/**
 * Like manager, but push and pull requests return exactly the same information.
 */
public abstract class SingleTypeManager<Type> extends Manager<Type, Type> {
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
