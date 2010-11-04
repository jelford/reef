package uk.ac.imperial.vazels.reef.client;

public abstract class RequestHandler<Type> {
  public void handle(Type reply) {
    handle(reply, true, "");
  }

  public abstract void handle(Type reply, boolean success, String message);
}