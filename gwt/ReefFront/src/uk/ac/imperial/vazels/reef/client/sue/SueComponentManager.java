package uk.ac.imperial.vazels.reef.client.sue;

import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.JsArrayStringSetConverter;
import uk.ac.imperial.vazels.reef.client.managers.ListedCollectionManager;

public class SueComponentManager extends ListedCollectionManager<String, SingleSueComponentManager> {
  private static SueComponentManager manager;
  
  private SueComponentManager() {
    setPuller(new SueComponentPuller());
  }
  
  public static SueComponentManager getManager() {
    if (manager == null) {
      manager = new SueComponentManager();
    }
    return manager;
  }
  
  public Set<String> getNames() {
    return this.getItems();
  }
  
  public SingleSueComponentManager getSueComponentManager(String name) {
    return this.getItem(name);
  }
  
  public void sueComponentUploaded(String name) {
    this.serverChange(name);
  }
  
  @Override
  protected SingleSueComponentManager createManager(String id, boolean nMan) {
    return new SingleSueComponentManager(id);
  }


  
  /**
   * Request builder for getting the actor list.
   */
  private class SueComponentPuller extends MultipleRequester<Set<String>> {
    public SueComponentPuller() {
      super(RequestBuilder.GET, "/sue/", new JsArrayStringSetConverter());
      System.out.println("Just converted the JS string I think...");
    } 
  }

}
