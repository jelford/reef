package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;


public class OutputData {
  
  private String data;
  
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
  }
  
  
  public String getData() {
    return data;
  }
  
  public void setData(String jsonInput) {
    data = jsonInput;
  }
  
  public Panel getPanel() {
    
    FlowPanel outputPanel = new FlowPanel();
    outputPanel.setSize("50%", "50%");
    System.out.println(getData());
    OutputDataOverlay groups = parseData(getData());
    JsArrayString keys = groups.keys();
    for(int i = 0; i < keys.length(); i++) {
      outputPanel.add(new Label("Group " + keys.get(i)));
      outputPanel.add(groupHTMLHelper(groups.get(keys.get(i))));
    }
    
    
    return outputPanel;
  }
  
  Panel groupHTMLHelper(String groupData) {
    FlowPanel groupPanel = new FlowPanel();
    
    OutputDataOverlay hosts = parseData(groupData);
    System.out.println(hosts.keys().get(0));
    
    return groupPanel;
  }
  
  Panel hostHTMLHelper(JSONValue host) {
    FlowPanel hostPanel = new FlowPanel();
    
    JSONArray data = host.isArray();
    if(data == null) {
      return hostPanel;
    }
    for(int i = 0; i < data.size(); i++) {
      
    }
    return hostPanel;
  }
  
  native OutputDataOverlay parseData(String data) /*-{
    
    return JSON.parse(data);
    
  }-*/;
  
}
