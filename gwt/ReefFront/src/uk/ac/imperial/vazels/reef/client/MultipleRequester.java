package uk.ac.imperial.vazels.reef.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;

public class MultipleRequester<Type> extends EasyRequest {
  private Map<RequestTicket, RequestHandler<Type>> handlers;
  private final String addr;
  private final RequestBuilder.Method method;
  private final Converter<Type> converter;
  
  public MultipleRequester(RequestBuilder.Method method, String addr, Converter<Type> converter){
    if(addr == null || method == null || converter == null)
      throw new NullPointerException();
    handlers = new HashMap<RequestTicket, RequestHandler<Type>>();
    this.addr = addr;
    this.method = method;
    this.converter = converter;
  }
  
  @Override
  protected void requested(RequestTicket ticket, Integer code, String reason, String content) {
    RequestHandler<Type> handler = handlers.get(ticket);
    
    if(isSuccess(code)){
      Type reply = converter.convert(content);
      handler.handle(reply);
    }
    else{
      handler.handle(null, false, getMessage(code, reason));
    }
    
    handlers.remove(ticket);
  }
  
  // Override to supply arguments
  protected QueryArg[] getArgs(){
    return null;
  }
  
  // Override to process before the actual handler is called
  protected void received(Type reply, boolean success, String message){
  }
  
  public void go(final RequestHandler<Type> handler){
    RequestTicket ticket = request(method, addr, getArgs());
    handlers.put(ticket, new RequestHandler<Type>() {
      @Override
      public void handle(Type reply, boolean success, String message) {
        received(reply, success, message);
        if(handler != null){
          handler.handle(reply, success, message);
        }
      }
    });
  }
  
  private String getMessage(Integer code, String reason) {
    if (isSuccess(code))
      return "";

    if (code != null) {
      if (reason == null)
        return code.toString();
      else
        return code.toString() + " " + reason;
    }
    return "Error in request";
  }

  private boolean isSuccess(Integer code) {
    return (code != null && code == 200);
  }
  
  protected interface Converter<Type>{
    public Type convert(String original);
  }
}
