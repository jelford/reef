package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
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
    Tree tree = new Tree();
    //TreeItem root = new TreeItem();
    outputPanel.setSize("50%", "50%");
    OutputDataOverlay groups = parseData(getData());
    JsArrayString keys = groups.keys();
    
    for(int i = 0; i < keys.length(); i++) {
      TreeItem group = new TreeItem("Group " + keys.get(i));
      groupHTMLHelper(groups.get(keys.get(i)), group);
      tree.addItem(group);
    }
    outputPanel.add(tree);
    
    
    return outputPanel;
  }
  
  void groupHTMLHelper(GroupsDataOverlay groupsDataOverlay, TreeItem groupTree) {
    
    JsArrayString keys = groupsDataOverlay.keys();
    for (int i = 0; i < keys.length(); i++) {
      TreeItem hostTree = new TreeItem("Host " + keys.get(i));
      hostTreeHelper(groupsDataOverlay.get(keys.get(i)), hostTree);
      groupTree.addItem(hostTree);
    }
    
  }
  
  void hostTreeHelper(VariableDataOverlay varData, TreeItem hostTree) {
    
    
    
    
    
    
  }
  
  native OutputDataOverlay parseData(String data) /*-{
    
    return JSON.parse(data);
    
  }-*/;

  
}
