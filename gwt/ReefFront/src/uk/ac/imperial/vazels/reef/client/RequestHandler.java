package uk.ac.imperial.vazels.reef.client;

/**
 * Used to handle a response for a request send using {@link MultipleRequester}.
 * 
 * @param <Type> The type that the request should return.
 */
public abstract class RequestHandler<Type> {
  
  /**
   * Returns whether or not this request code is successful.
   * @param responseCode
   * @return
   */
  protected static boolean isSuccessful(Integer responseCode) {
    return (responseCode != null && (responseCode >= 200 && responseCode < 300));
  }

  /**
   * The actual handler for request responses.
   * 
   * @param reply The object returned in the response.
   * @param responseCode Response code from the server (e.g. 200 == success)
   * @param message A justification for any non-successful response code.
   */
  public abstract void handle(Type reply,final Integer responseCode, String message);
}