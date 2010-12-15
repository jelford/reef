package uk.ac.imperial.vazels.reef.client.output;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.ScatterChart;
import com.google.gwt.visualization.client.visualizations.ScatterChart.Options;

/**
 * Displays the supplied output in a pretty little way.
 */
public class OutputView extends Composite {

  private static OutputViewUiBinder uiBinder = GWT
      .create(OutputViewUiBinder.class);

  interface OutputViewUiBinder extends UiBinder<Widget, OutputView> {
  }

  @UiField Tree tree;
  @UiField SimplePanel placeholder;
  
  private OutputData data = null;
  
  public OutputView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("tree")
  void onSelection(SelectionEvent<TreeItem> event) {
    TreeItem item = event.getSelectedItem();
    if(item.getChildCount() == 0 && item.getStyleName().contains("graphable-var")) {
      String groupname = item.getParentItem().getParentItem().getText();
      groupname = groupname.substring(6, groupname.length());
      ScatterChart chart = buildChart(groupname, item.getText());
      placeholder.setWidget(chart);
    }
  }
  
  /**
   * Tell the widget to use the given data.
   * @param data Data to display.
   */
  public void useData(OutputData data) {
    this.data = data;
    
    VisualizationUtils.loadVisualizationApi(new Runnable() {
      @Override
      public void run() {
        buildDisplay();
      }
    }, ScatterChart.PACKAGE);
  }
  
  /**
   * Build the display using the given output data.
   */
  private void buildDisplay() {
    buildTree();
    placeholder.clear();
  }
  
  /**
   * Build the exploration tree.
   */
  private void buildTree() {
    tree.clear();

    for(String group : data.groupIds()) {
      final TreeItem treeGroup = buildGroupTree(group, data.groupInfo(group));
      tree.addItem(treeGroup);
    }
  }
  
  /**
   * Build a tree representing the given group.
   * @param group Name of the group.
   * @param data Data for the group.
   * @return A tree representation.
   */
  private TreeItem buildGroupTree(String group, GroupData data) {
    TreeItem tree = new TreeItem("Group " + group);
    
    for(String host : data.hostIds()) {
      final TreeItem treeHost = buildHostTree(host, data.hostInfo(host));
      tree.addItem(treeHost);
    }
    
    return tree;
  }
  
  /**
   * Build a tree representing a certain host.
   * @param host The name of the host.
   * @param data Data for the host.
   * @return A tree representation of the host.
   */
  private TreeItem buildHostTree(String host, HostData data) {
    TreeItem tree = new TreeItem("Host " + host);
    
    for(String varname : data.variableNames()) {
      TreeItem variable = new TreeItem(varname);
      
      final TimeSeries series = data.variableSeries(varname);
      final Integer firstTimestamp = series.stamps().iterator().next();
      final SnapshotData firstSnapshot = series.snapshot(firstTimestamp);
        
      if(firstSnapshot.getType().equals("Double")) {
        variable.addStyleName("graphable-var");
      }
      tree.addItem(variable);
    }
    
    return tree;
  }
  
  /**
   * Build a scatter chart representing the supplied group and variable.
   * @param data The object with all our data.
   * @param group Group to represent.
   * @param variable Variable name to represent.
   * @return A scatter graph.
   */
  private ScatterChart buildChart(String group, String variable) {
    DataTable dt = DataTable.create();
    dt.addColumn(ColumnType.NUMBER, "TimeStamp");
    
    GroupData groupData = data.groupInfo(group);
    
    int rowIndex = 0;
    for(String host : groupData.hostIds()) {
      TimeSeries timeline = groupData.hostInfo(host).variableSeries(variable);
      if(timeline == null) {
        continue;
      }
      
      dt.addColumn(ColumnType.NUMBER, "Host " + host);
      int columnIndex = 1;
      
      for(Integer timestamp : timeline.stamps()) {
        Float value = timeline.snapshot(timestamp).getFloat();
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
