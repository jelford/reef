package uk.ac.imperial.vazels.reef.client.output;

import java.util.List;
import java.util.Map;

import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

public class OutputData {
  
  private String data;
  
  private HashMap<String, VariableData> groupData = new HashMap<String, VariableData>(); 
  
  
  public OutputData() {
    //Initialise with empty input
    initialise("{}");
  }
  
  public OutputData(String jsonInput) {
    //Forward the data on
      initialise(jsonInput);
  }

  
  private void initialise(String jsonInput) {
    data = jsonInput;
    try {
      setData(jsonInput);
    } catch (JSONException e) {
      // TODO sensible error handling.
      Window.alert(e.getMessage());
    }
  }
  
  
  public String getData() {
    return data;
  }
  
  public void setData(String jsonInput) {
    
    JSONValue obj = JSONParser.parse(jsonInput);
    
    // will return a reference to data if there is data, null otherwise
    JSONArray jsonData = obj.isArray();
    
    if(jsonData != null) {
      // we got some data, go forth and populate
      parseJSON(jsonData);
    } else {
      throw new JSONException("Error in receiving data.");
    }
      
    
  }
  
  private void parseJSON(JSONArray jsonData) {
      
      
  }
  
  
  class VariableData {
    
    String varName;
    
    Map<Integer,TimeSeries> hostData = new HashMap<Integer, TimeSeries>();
    
    
  }
  
  class TimeSeries {
    
    List<Snapshot> snapshots;
    
  }
  
  class Snapshot {
    
    private long timestamp;
    private String actor;
    private String valueType;
    private Object value;
    
  }
  
}
