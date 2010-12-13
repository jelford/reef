package uk.ac.imperial.vazels.reef.client.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.visualizations.ScatterChart;
import com.google.gwt.visualization.client.visualizations.ScatterChart.Options;


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
    //data = jsonInput;
    
    data = "{\"1\":" +
    "{\"0\":" +  
        "{\"returned_messages\" :" +  
            "{\"29351\" : {\"timestamp\" : \"29351\", \"type\" : \"Double\", \"value\" : \"31999.0\", \"actor\" : \"counter\"}," +
            "\"29355\" : {\"timestamp\" : \"29355\", \"type\" : \"Double\", \"value\" : \"32000.0\", \"actor\" : \"counter\"} }" +
        "}" + 
    "}," +
    "\"2\": {\"0\" : {}}" +
    
    "}";
  }
  
  public Panel getPanel() {
    final FlowPanel outputPanel = new FlowPanel(); 
    Runnable onLoadCallback = new Runnable() {
      public void run() {
        Window.alert("Foo");
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
            System.out.println("Foo1: " + event.getSelectedItem().getStylePrimaryName());
            
            TreeItem item = event.getSelectedItem();
            if (event.getSelectedItem().getChildCount() == 0 && event.getSelectedItem().getStylePrimaryName().contentEquals("Double")) {
              System.out.println("Foo2");
              String groupName = item.getParentItem().getParentItem().getText();
              if(dataPanel.getWidgetCount() != 0) {
                dataPanel.remove(0);
              }
              dataPanel.add(buildChart(groupName.subSequence(6, groupName.length()).toString(), item.getText())); //pass on group label and variable name
            }
          };
        
        });
      }
    };

    VisualizationUtils.loadVisualizationApi(onLoadCallback, ScatterChart.PACKAGE);
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
      
      
      JsArrayInteger foo = varData.get(varNames.get(i)).timeStamps();
      if(foo.length() > 0) {
        varTree.setStylePrimaryName(varData.get(varNames.get(i)).get(foo.get(0)).getType());
      }
      
      hostTree.addItem(varTree);
    }
    
  }
  
  native OutputDataOverlay parseData(String data) /*-{
    
    return JSON.parse(data);
    
  }-*/;

  private Map<String, Map<Integer, Float>> collectGroupVarDoubleData(String group, String varName) {
    
    GroupsDataOverlay groupData = parseData(this.data).get(group);
    
    Map<String, Map<Integer, Float>> varData = new HashMap<String, Map<Integer, Float>>(); 
    
    TimeSeriesOverlay data;
    JsArrayString hostsKeys = groupData.keys();
    
    for(int i = 0; i < hostsKeys.length(); i++) {
      data = groupData.get(hostsKeys.get(i)).get(varName);
      
      varData.put(hostsKeys.get(i), buildVarDataDouble(data));
    }
    
    return varData;
    
  }
  private HashMap<Integer, Float> buildVarDataDouble(TimeSeriesOverlay data) {
    HashMap<Integer, Float> finalData = new HashMap<Integer,Float>();
    
    JsArrayInteger timestamps = data.timeStamps();
    
    for(int i = 0; i < timestamps.length(); i++) {
     finalData.put(timestamps.get(i), data.get(timestamps.get(i)).getDouble());
    }
    
    return finalData;
    
  }
  
  private ScatterChart buildChart(String groupName, String varName) {
      DataTable dt = DataTable.create();
      
      Map<String, Map<Integer, Float>> data = collectGroupVarDoubleData(groupName, varName);
      
      dt.addColumn(ColumnType.NUMBER, "TimeStamp");
      
      Set<String> keyset = data.keySet();
      int rowIndex = 0;
      for (String key : keyset) {
        Map<Integer, Float> hostData = data.get(key);
        dt.addColumn(ColumnType.NUMBER, "Host " + key);
        int columnIndex = 1;
        for(int timestamp : hostData.keySet()) {
          Float value = hostData.get(timestamp);
          dt.addRow();
          dt.setValue(rowIndex, 0, timestamp);
          dt.setValue(rowIndex, columnIndex, value);
          rowIndex++;
          
        }
        columnIndex++;
        
      }
      
      Options options = Options.create();
      options.setLineSize(2);
      options.setPointSize(10);
      options.setEnableTooltip(true);
      options.setWidth(500);
      options.setHeight(400);
      
      return new ScatterChart(dt, options);
  }
  
}
