package uk.ac.imperial.vazels.reef.client.groups;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AllocateGroups extends Composite {

  /**
   * Some constants incase we ever want to change the layout of the table.
   */
  private static final int GROUP_NAME_COLUMN = 0;
  private static final int GROUP_HOSTS_COLUMN = GROUP_NAME_COLUMN+1;
  private static final int GROUP_REMOVE_COLUMN = GROUP_HOSTS_COLUMN+1;
  
  /**
   * Table for holding the information on each group.
   */
  @UiField FlexTable groupsFlexTable;
  
  /**
   * Label for information about groups as a whole (number of groups).
   */
  @UiField Label groupsInfo;
  
  /**
   * Specify the name of a new group.
   */
  @UiField TextBox newGroupTextBox;
  
  /**
   * Specify the number of hosts in a new group.
   */
  @UiField IntegerBox newHostsTextBox;
  
  /**
   * Store a GroupSummary object containing summary information about all the
   * groups.
   */
  private GroupSummary groups;
  
  /**
   * Generated code - gives us an interface to the XML-defined UI.
   */
  private static AllocateGroupsUiBinder uiBinder = GWT
      .create(AllocateGroupsUiBinder.class);

  /**
   * Generated code. 
   */
  interface AllocateGroupsUiBinder extends UiBinder<Widget, AllocateGroups> {
  }

  public AllocateGroups() {
    // Method provided by Composite to initialize the widget from the XML
    initWidget(uiBinder.createAndBindUi(this));

    readyForInput();
    
    // Init empty table & group summary.
    clearGroupData();
    
    // Get new info
    refresh();
  }

  /**
   * Clear the group data and update the server to reflect the changes.
   */
  @UiHandler("btnReset")
  void resetClicked(ClickEvent event) {
    clearGroupData();
    batchUpdateServerGroups();
  }
  
  /**
   * Add a new group to the datastructures from the input text fields.
   */
  @UiHandler("addGroupButton")
  void addGroupClicked(ClickEvent event) {
    addGroupFromInputBoxes();
  }
  
  /**
   * Submit when the user hits return in an inputbox.
   */
  @UiHandler({"newGroupTextBox", "newHostsTextBox"})
  void textSubmission(KeyPressEvent event) {
    if (event.getCharCode() == KeyCodes.KEY_ENTER) {
      addGroupFromInputBoxes();
    }
  }

  /**
   * Initialize UI elements to a fresh state & set user focus.
   */
  private void readyForInput(){
    newHostsTextBox.setText("");
    newGroupTextBox.setText("");
    newGroupTextBox.setFocus(true);
  }
  
  /**
   * Helper function grabs and validates data from the input boxes then passes
   * the data to the addGroup function
   */
  private void addGroupFromInputBoxes() {
    final String newGroupName = 
      newGroupTextBox.getText().trim();

    if (!validateGroupName(newGroupName)) {
      newGroupTextBox.selectAll();
      return;
    }

    final Integer newGroupSize = newHostsTextBox.getValue();
    
    if(!validateNumHosts(newGroupSize)) {
      newHostsTextBox.selectAll();
      return;
    }

    /* We're done with these values; clear them */
    readyForInput();

    addGroup(newGroupName, newGroupSize);
    batchUpdateServerGroups();
  }

  /**
   * Check that groupName is an alphanumeric string.
   */
  private boolean validateGroupName(final String groupName) {
    // Don't add the group if it's already in the table.
    // TODO: Do we want to just update an existing entry?
    if (groups.contains(groupName)) {
      Window.alert("You already have a group named '"+groupName+"'.");
      return false;
    } else if (!groupName.matches("^[0-9A-Za-z]{1,}$")){
      Window.alert("Group names must be made up of alphanumeric characters.");
      return false;
    } else {
      return true;
    }
  }
  
  /**
   * Check that numHosts is a positive integer.
   */
  private boolean validateNumHosts(final Integer numHosts) {
    if (numHosts == null) {
      Window.alert("You need to enter a number of hosts (e.g. '10')");
      return false;
    } else if (numHosts < 0) {
      Window.alert("You must have at least one host in a group.");
      return false;
    }
    return true;
  }

  /**
   * Add group to FlexTable. Expects inputs to already be validated.
   * This will add the group to the group map and update a single table entry.
   * The server will not be notified.
   */
  private void addGroup(final String newGroupName, final int numberOfHosts) {
    // Add to group map
    groups.put(newGroupName, numberOfHosts);
    
    addGroupToTable(newGroupName, numberOfHosts);
    
    // Update number of groups
    refreshGroupsInfo();
  }
  
  /**
   * Update the table to reflect the fact that a new group has been added.
   * @param newGroupName
   * @param numberOfHosts
   */
  private void addGroupToTable(final String newGroupName, final int numberOfHosts) {
    // Add the group to the table.
    int row = groupsFlexTable.getRowCount();
    groupsFlexTable.setText(row, GROUP_NAME_COLUMN, newGroupName);
    groupsFlexTable.setText(row, GROUP_HOSTS_COLUMN, Integer.toString(numberOfHosts));

    // Add a button to remove this group from the table.
    Button removeGroupButton = new Button("x");
    removeGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        removeGroup(newGroupName);
      }
    });
    groupsFlexTable.setWidget(row, GROUP_REMOVE_COLUMN, removeGroupButton);
  }

  /**
   * Remove group named grpName and notify server.
   */
  private void removeGroup(final String grpName) {
    removeGroup(grpName,true);
  }
  
  /**
   * Remove group named grpName from map and table.
   * Possibly notify server.
   */
  private void removeGroup(final String grpName, final boolean notifyServer) {
    removeTableRow(grpName);
    groups.put(grpName,0);
    if (notifyServer) {
      batchUpdateServerGroups();
    }
    refreshGroupsInfo();
  }

  /**
   * Remove the first row from the table with the group name groupName
   */
  private void removeTableRow(String groupName) {
    for (int i=0; i<groupsFlexTable.getRowCount(); i++) {
      String currentGroup = groupsFlexTable.getText(i, GROUP_NAME_COLUMN);
      if (currentGroup.equals(groupName)) {
        groupsFlexTable.removeRow(i);
        break;
      }
    }
  }

  /**
   * Updates the displayed number of groups.
   */
  private void refreshGroupsInfo() {
    int size = groups.size();
    String txt = size + " group";
    if(size != 1)
      txt += "s";
    txt += " added to the system.";
    groupsInfo.setText(txt);
  }


  /*
   * The below is the code required to get group information from the server.
   */

  /**
   * Dispatch a request to the server letting it know we'd like to hear about
   * group info.
   */
  private void refresh() {
    new GroupDataRequest().go(new RequestHandler<GroupSummary>(){
      @Override
      public void handle(GroupSummary reply, boolean success, String message) {
        if (success) {
          refreshGroupData(reply);
        }
      }
    });
  }

  /**
   * refreshGroupData with no data.
   */
  private void clearGroupData() {
    refreshGroupData(new GroupSummary());
  }
  
  private void refreshGroupData(final GroupSummary summary) {
    /*
     * No need to add groups individually; we'd just be re-building
     * the summary. Instead, update groups to match, then make sure the 
     * table is up-to-date.
     */
    groups = summary;
    
    groupsFlexTable.removeAllRows();
    // Column headers
    groupsFlexTable.setText(0, GROUP_NAME_COLUMN, "Group");
    groupsFlexTable.setText(0, GROUP_HOSTS_COLUMN, "Hosts");
    groupsFlexTable.setText(0, GROUP_REMOVE_COLUMN, "Remove");
    
    for (String key : summary.keySet()) {
      String groupName = key;
      int groupSize = summary.get(key);
      addGroupToTable(groupName,groupSize);
    }
    
    refreshGroupsInfo();
  }


  /**
   * Helper class to send requests to get group info (this will send a batch
   * request to the server, and retrieve a summary of all group info).
   */
  private class GroupDataRequest extends MultipleRequester<GroupSummary>{
    GroupDataRequest() {
      super(RequestBuilder.GET, "/groups/", 
          new Converter<GroupSummary>() {
        @Override
        public GroupSummary convert(String original) {
          return new GroupSummary(original);
        }
      });
    }
  }

  /**
   * Post all the current group data to the server & update the local data with
   * the returned info from the server (i.e. check that local and remote records
   * are the same).
   */
  private class GroupDataUpdate extends MultipleRequester<GroupSummary>{
    public GroupDataUpdate() {
      super(RequestBuilder.POST, "/groups/", new Converter<GroupSummary>(){
        @Override
        public GroupSummary convert(String original) {
          return new GroupSummary(original);
        }
      });
    }
    
    protected QueryArg[] getArgs() {
      // Construct an array of QueryArgs we'll use for our post request
      QueryArg[] queryArguments = new QueryArg[groups.keySet().size()];
      int index = 0;
      for (String groupName : groups.keySet()) {
        queryArguments[index] = new QueryArg(groupName, 
            Integer.toString(groups.get(groupName)));
        index++;
      }
      
      return queryArguments;
    }

    @Override
    protected void received(GroupSummary reply, boolean success, String message) {
      if(success)
        refreshGroupData(reply);
    }
  }
  
  /**
   * Tell the server about the groups in our table
   */
  private void batchUpdateServerGroups() {
    new GroupDataUpdate().go(null);
  }
}