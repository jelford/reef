package uk.ac.imperial.vazels.reef.client.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.imperial.vazels.reef.client.groups.GroupManager;
import uk.ac.imperial.vazels.reef.client.groups.SingleGroupManager;

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
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
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
      ScatterChart chart = buildChart(groupname, item.getText());
      placeholder.setWidget(chart);
    }
    if(item.getStyleName().contains("group-level-item")) {
      String groupName = item.getText();
      Widget groupDataPanel = buildGroupDataPanel(groupName);
      placeholder.setWidget(groupDataPanel);

    }
  }

  /**
   * 
   * Takes a group name as input and builds a panel containing all the 
   * data we have about the group to display to the user
   * 
   * @param groupName
   * @return GroupDataPanel
   */

  private Widget buildGroupDataPanel(String groupName) {
    GroupDataPanel main = new GroupDataPanel();

    SingleGroupManager groupData = GroupManager.getManager().getGroupManager(groupName);
    
    main.setGroupName(groupName);

    StringBuffer workloadText = new StringBuffer("");
    for(String workload : groupData.getWorkloads()) {
      workloadText.append(workload).append(", ");
    }
    if(workloadText.length() > 2) {
      workloadText.delete(workloadText.length()-2, workloadText.length());
    }
    main.setWorkload(workloadText.toString());

    StringBuffer sueText = new StringBuffer("");
    for(String sue : groupData.getSueComponents()) {
      sueText.append(sue).append(", ");
    }
    if(sueText.length() > 2) {
      sueText.delete(sueText.length()-2, sueText.length());
    }
    main.setSUE(sueText.toString());
    
    main.setSize(groupData.getSize());
    
    String[] connected_hosts = {"host1", "host2"};
    
    main.setConnectedHosts(connected_hosts);
    
    main.setEvolvingHosts(connected_hosts);
    
    main.setVariables(getGroupVariables(groupName));

    return main;
  }
  
  
  /**
   * 
   * Find out what variables apply to a particular group
   * 
   * @param group Group to inspect
   * @return Set<String> with each variable name as an element
   * 
   */
  
  public Set<String> getGroupVariables(String group) {
    
    Set<String> variables = new HashSet<String>();
    
    for (String host : data.groupInfo(group).hostIds()) {
      
      for(String variable : data.groupInfo(group).hostInfo(host).variableNames()) {
        variables.add(variable);
      }
      
    }
    
    return variables;
    
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
      treeGroup.addStyleName("group-level-item");
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
    TreeItem tree = new TreeItem(group);

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
   * Build a scatter chart representing the supplied group and variable using data 
   * from all hosts that have data available.
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
    options.setMin(0);
    options.setEnableTooltip(true);
    options.setWidth(600);
    options.setHeight(400);
    options.setTitleX("Timestamp");
    options.setTitleY(variable);
    options.setLegend(LegendPosition.NONE);

    return new ScatterChart(dt, options);
  }
}
