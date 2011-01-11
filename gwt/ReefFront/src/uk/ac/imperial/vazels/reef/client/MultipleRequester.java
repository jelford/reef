package uk.ac.imperial.vazels.reef.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;

/**
 * Currently the preferred way to make any application/x-www-form-urlencoded requests.
 * <p>
 * It allows the user to send multiple requests, each with it's own individual
 * handler that is matched back to it when a response is returned.
 * </p>
 * <p>
 * The preferred way to use this is to subclass it for each type of request you would
 * like to make. So one per address usually. In the new constructor you should pass in
 * all the defaults the particular request will need. This allows you to use your
 * subclass with a default constructor, avoiding errors.
 * </p>
 * <p>
 * There are a couple of methods you can override to affect behaviour:
 * <ul>
 * <li>{@link MultipleRequester#received(Object, boolean, String)}</li>
 * <li>{@link MultipleRequester#getArgs()}</li>
 * </ul>
 * </p>
 * <p>
 * And use {@link MultipleRequester#go} to actually send a request.
 * </p>
 *
 * @param <Type> The type that should be returned by a response to one
 * of these requests.
 */
public class MultipleRequester<Type> extends EasyRequest {
  private Map<RequestTicket, RequestHandler<Type>> handlers;
  private final String addr;
  private final RequestBuilder.Method method;
  private final Converter<Type> converter;
  
  /**
   * Set up the factory request details.
   * 
   * @param method {@link RequestBuilder.Method} to use (POST or GET)
   * @param addr The address to use (e.g. "/settings/")
   * @param converter To convert between the response body, a {@link String} to
   * the correct {@code <Type>}
   * @throws NullPointerException If any of the parameters are {@code null}
   */
  public MultipleRequester(RequestBuilder.Method method, String addr, Converter<Type> converter){
    if(addr == null || method == null)
      throw new NullPointerException();
    handlers = new HashMap<RequestTicket, RequestHandler<Type>>();
    this.addr = addr;
    this.method = method;
    this.converter = converter;
  }
  
  /**
   * @deprecated Use {@link MultipleRequester#received(Object, boolean, String)}
   * <p>
   * This should only be called from the base class {@link EasyRequest} as the
   * callback method. It will actually deal with calling the correct handlers here
   * so must not be overridden. This will avoid breaking the expectations of other
   * callbacks.
   * </p>
   */
  protected final void requested(RequestTicket ticket, Integer responseCode, String reason, String content) {
    RequestHandler<Type> handler = handlers.get(ticket);
    
    if (RequestHandler.isSuccessful(responseCode)) {
      Type reply = (converter != null) ? converter.convert(content) : null;
      handler.handle(reply, responseCode, null);
    } else {
      handler.handle(null, responseCode, null);
      handlers.remove(ticket);
    }
  }
  
  /**
   * Provides the query arguments for any request.
   * <p>
   * This is called for each request and should be overridden if the request
   * is to provide any {@link QueryArg}s. In current implementations of this
   * method, we subclass {@link MultipleRequester} as an inner class of the
   * class using it. This means that we can rip query arguments straight from
   * the class that contains the relevant data.
   * </p>
   * 
   * @return A list of {@link QueryArg}s to provide with the request.
   */
  protected QueryArg[] getArgs(){
    return null;
  }
  
  /**
   * A callback that is run for <strong>every</strong> received response.
   * <p>
   * This has the same input as
   * {@link RequestHandler#handle(Object, boolean, String)} and is called directly
   * before the individual response handler.
   * </p>
   * <p>
   * You should only override this if you need to use it.
   * </p>
   */
  protected void received(Type reply, Integer requestCode, String message){
  }
  
  /**
   * Send a request.
   * <p>
   * Use all the request information given in the constructor to send a request.
   * If {@code handler} is not {@code null} we use it to handle any received
   * response.
   * </p>
   * 
   * @param handler The handler that is called on result of this request.
   */
  public void go(final RequestHandler<Type> handler){
    go(handler, "");
  }
  
  /**
   * Send a request to a modified address.
   * <p>
   * This works just as {@link MultipleRequester#go(RequestHandler)} but modifies
   * the address that the request is sent to.
   * </p>
   * 
   * @param handler The handler that is called on result of this request.
   * @param addrExt A {@link String} tagged to the end of the request address.
   * For instance a request constructed with the request address "/settings/"
   * and with {@code addrExt} as "global" will send the request to
   * "/settings/global".
   */
  public void go(final RequestHandler<Type> handler, String addrExt){
    RequestTicket ticket = request(method, addr+addrExt, getArgs());
    handlers.put(ticket, new RequestHandler<Type>() {
      @Override
      public void handle(Type reply, Integer requestCode, String message) {
        received(reply, requestCode, message);
        if(handler != null){
          handler.handle(reply, requestCode, message);
        }
      }
    });
  }
  
  /**
   * Implement this to show we can convert to {@code <Type>}.
   * 
   * @param <Type> The type to convert to.
   */
  public interface Converter<Type>{
    /**
     * Converts a {@link String} to the given type.
     * 
     * @param original Input string.
     * @return Output object of type {@code <Type>}.
     */
    public Type convert(String original);
  }
}
