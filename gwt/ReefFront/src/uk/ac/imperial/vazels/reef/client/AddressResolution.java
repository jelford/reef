package uk.ac.imperial.vazels.reef.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class AddressResolution {
  public String baseURL;
  
  public AddressResolution(){
    this(true);
  }
  
  public AddressResolution(boolean inTesting){
    if(inTesting)
      baseURL = GWT.getModuleBaseURL() + "proxy";
    else
      baseURL = "http://"+Window.Location.getHost();
  }
  
  public String resolve(String url){
    return baseURL + url;
  }
}
