package uk.ac.imperial.vazels.reef.client.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Describes the data returned when output is requested from an experiment.
 */
public class OutputData {
  private static final boolean FAKE_IT = true;
  
  private Map<String, GroupData> groups;
  
  /**
   * Defaults with no data.
   */
  public OutputData() {
    //Initialise with empty input
    this("{}");
  }
  
  /**
   * Create the object from the json input given.
   * @param jsonInput stringified json object.
   */
  public OutputData(String jsonInput) {
    groups = new HashMap<String, GroupData>();

    /*
     * Today, we will be faking out data. Just so ya know.
     * @TODO: FIX THIS FUCKING MESS.
     */
    jsonInput = FAKE_IT ? "{\"1\":" +
    "{\"0\":" + 
        "{\"returned_messages\" :" + 
            "{\"29351\" : {\"timestamp\" : \"29351\", \"type\" : \"Double\", \"value\" : \"31999.0\", \"actor\" : \"counter\"}," +
            "\"29355\" : {\"timestamp\" : \"29355\", \"type\" : \"Double\", \"value\" : \"32000.0\", \"actor\" : \"counter\"} }" +
        "}" +
    "}," +
    "\"2\": {\"0\" : {}}" +
   
    "}" : jsonInput;
    
    
    
    OutputDataOverlay data = parseData(jsonInput);
    JsArrayString keys = data.keys();
    for(int i=0; i<keys.length(); i++) {
      String key = keys.get(i);
      groups.put(key, new GroupData(data.get(key)));
    }
  }
  
  /**
   * Take a string and give an overlay.
   * @param data Stringified json object.
   * @return output data overlay represented by {@code data}.
   */
  public native OutputDataOverlay parseData(String data) /*-{
    return JSON.parse(data);
  }-*/;
  
  /**
   * Get the group ids in this data.
   * @return A set of group IDs.
   */
  public Set<String> groupIds() {
    return groups.keySet();
  }
  
  /**
   * Get data for a specific group.
   * @param grp Group to get info for.
   * @return Data specifically related to the group requested.
   */
  public GroupData groupInfo(String grp) {
    return groups.get(grp);
  }
}