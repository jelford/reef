package uk.ac.imperial.vazels.reef.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * Used for an easy way to resolve addresses on either the proxy or our server.
 * <p>
 * The SOP will block requests from our front end (during testing only) to our
 * back end, as they are on different addresses. As a workaround, we use a proxy
 * server to route the request properly during testing. This class will deal with
 * choosing where to send our requests so long as we set the
 * {@link AddressResolution#testing} variable properly.
 * </p>
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Same_origin_policy">Same Origin Policy</a>
 *
 */
public class AddressResolution {
  public String baseURL;
  
  /**
   * Edit this whenever we move between testing and deployment!
   * <p>
   * This should be {@code true} whenever we are in testing or {@code false}
   * otherwise. It tells the class whether to route a request through our
   * proxy or not.
   * </p>
   */
  public static final boolean testing = true;
  
  public AddressResolution(){
    this(testing);
  }
  
  public AddressResolution(boolean inTesting){
    if(inTesting)
      baseURL = GWT.getModuleBaseURL() + "proxy";
    else
      baseURL = "http://"+Window.Location.getHost();
  }
  
  /**
   * Gives a correct address to request from.
   * <p>
   * For instance if we want to request "/settings/", we want one of:
   * <ul>
   * <li>"http://localhost:8000/settings/" - in deployment</li>
   * <li>"http://localhost:8999/proxy/settings/" - in testing</li>
   * </p>
   * @param url The relative address (e.g. "/settings/")
   * @return The actual address to call (e.g. "http://localhost:8000/settings/")
   */
  public String resolve(String url){
    return baseURL + url;
  }
}
