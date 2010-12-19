package uk.ac.imperial.vazels.reef.client.util;

public class Message<T> {
  final T message;
  
  public T getMessage() {
    return message;
  }
  
  Message(final T message) {
    this.message = message;
  }
}
