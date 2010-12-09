package uk.ac.imperial.vazels.reef.client.output;

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
    System.out.println(getData());
    JSONArray groups = JSONParser.parse(getData()).isArray();
    
    /*if(groups == null) {
        return outputPanel;
    }*/
    System.out.println("foo");
    for(int i = 0; i < groups.size(); i++) {
        outputPanel.add(new Label("Group " + i));
        outputPanel.add(groupHTMLHelper(groups.get(i)));
    }
    
    return outputPanel;
  }
  
  Panel groupHTMLHelper(JSONValue group) {
    FlowPanel groupPanel = new FlowPanel();
    
    JSONArray hosts = group.isArray();
    if(hosts == null) {
      return groupPanel;
    }
    for(int i = 0; i < hosts.size(); i++) {
      
      groupPanel.add(new Label("Host " + i));
      groupPanel.add(hostHTMLHelper(hosts.get(i)));
      
    }
    
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
  
}
