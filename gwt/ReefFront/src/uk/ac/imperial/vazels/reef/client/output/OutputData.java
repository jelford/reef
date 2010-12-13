package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;


public class OutputData {
  
  private String data;
  HorizontalPanel dataPanel = new HorizontalPanel();
  
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
    outputPanel.add(dataPanel);
    
    tree.addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        System.out.println("Foo1");
        
        event.getSelectedItem();
        System.out.println(event.getSelectedItem().getChild(0));
        if (event.getSelectedItem().getChild(0).getStyleName() == "varname") {
          System.out.println("Foo2");
          dataPanel.add((IsWidget) event.getSelectedItem().getChild(0));
          dataPanel.getWidget(0).setVisible(true);
        }
        
      }
    
        
    
    });
    
    
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
    
    JsArrayString varNames = varData.keys();
    
    for(int i = 0; i < varNames.length(); i++) {
      TreeItem varTree = new TreeItem(varNames.get(i));
      varTree.setStyleName("varname");
      
      varTreeHelper(varData.get(varNames.get(i)), varTree);
      
      hostTree.addItem(varTree);
    }
    
  }
  
  void varTreeHelper(TimeSeriesOverlay timeSeries, TreeItem varTree) {
    
    JsArrayString timeStamps = timeSeries.timeStamps();
    HorizontalPanel timeStampsPanel = new HorizontalPanel();
    FlexTable dataTable = new FlexTable();
    timeStampsPanel.add(dataTable);
    
    createHeaders(dataTable);
    for(int i = 0; i < timeStamps.length(); i++) {
      
      TimeStamp timestamp = timeSeries.get(timeStamps.get(i));
      
      dataTable.setWidget(i, 0, new Label(timeStamps.get(i)));
      dataTable.setWidget(i, 1, new Label(timestamp.getActor()));
      dataTable.setWidget(i, 2, new Label(Integer.toString(timestamp.getDouble())));
      
    }
    timeStampsPanel.setVisible(false);
    varTree.addItem(timeStampsPanel);
  }
  
  private void createHeaders(FlexTable table) {
    
    Label valueLabel = new Label("Value");
    Label actorLabel = new Label("Actor");
    Label timeStampLabel = new Label("Time Stamp");
    
    table.setWidget(0, 0, timeStampLabel);
    table.setWidget(0, 1, actorLabel);
    table.setWidget(0, 2, valueLabel);
  }
  
  native OutputDataOverlay parseData(String data) /*-{
    
    return JSON.parse(data);
    
  }-*/;

  
}
