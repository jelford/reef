package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Represents a workload as stored on the server.
 */
public class Workload {
  private final String name;
  private Set<String> actors;
  
  /**
   * Create the workload representation
   * @param name Either the workload name, or a json string representing the workload.
   */
  public Workload(String name) {
    WorkloadOverlay wkld = parseJSON(name);
    if(wkld == null) {
      this.name = name;
    }
    else {
      this.name = wkld.getName();
      this.actors = new HashSet<String>();
      // Get all the stuff from the overlay
      JsArrayString wkld_actors = wkld.getActors();
      for(int i=0; i<wkld_actors.length(); i++) {
        this.actors.add(wkld_actors.get(i));
      }
    }
  }
  
  public String getName() {
    return this.name;
  }
  
  /**
   * Try to parse json string into actual json.
   * @param json String to parse
   * @return json object if parse succeeds, or {@code null}
   */
  protected native WorkloadOverlay parseJSON(String json) /*-{
    try {
      return JSON.parse(json);
    }
    catch(e) {
      return null;
    }
  }-*/;
}
