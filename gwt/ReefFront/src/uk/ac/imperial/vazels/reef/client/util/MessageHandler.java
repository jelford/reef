package uk.ac.imperial.vazels.reef.client.util;

public abstract class MessageHandler<T> {
  /**
   * Handle a message of the expected type.
   * @param incoming A Message object wrapping some data
   */
  public abstract void handle(Message<T> incoming);
  
  /**
   * Handle the raw object with no Message wrapper.
   * @param incoming
   */
  public abstract void handle(T incoming);
}
