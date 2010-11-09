package uk.ac.imperial.vazels.reef.client;

/**
 * Used to handle a response for a request send using {@link MultipleRequester}.
 * 
 * @param <Type> The type that the request should return.
 */
public abstract class RequestHandler<Type> {
  /**
   * Success handler used as a shortcut when everything went well.
   * <p>
   * Just calls the full handler with code 200 and empty message.
   * </p>
   * @param reply The object returned in the response.
   */
  public final void handle(Type reply) {
    handle(reply, true, "");
  }

  /**
   * The actual handler for request responses.
   * 
   * @param reply The object returned in the response.
   * @param success {@code true} if and only if we got a successful response.
   * (Currently this is only code 200, we need to fix that.)
   * @param message A justification for any non-successful response code.
   */
  public abstract void handle(Type reply, boolean success, String message);
}