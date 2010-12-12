package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AllocateGroups extends Composite {

  /**
   * Some constants in case we ever want to change the layout of the table.
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
   * Button to add a new group
   */
  @UiField Button addGroupButton;
  
  /**
   * Button to remove all groups
   */
  @UiField Button btnReset;
  
  /**
   * List of widgets that should be disabled to avoid user interaction
   */
  protected Set<FocusWidget> userInteractionWidgets;
  
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

    userInteractionWidgets = new HashSet<FocusWidget>();
    userInteractionWidgets.add(addGroupButton);
    userInteractionWidgets.add(btnReset);
    userInteractionWidgets.add(newGroupTextBox);
    userInteractionWidgets.add(newHostsTextBox);
    
    refreshData();
  }

  /**
   * Clear the group data and update the server to reflect the changes.
   */
  @UiHandler("btnReset")
  void resetClicked(ClickEvent event) {
    removeAllGroups();
    refreshData();
  }

  /**
   * Add a new group to the data structures from the input text fields.
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

  // UI
  
  /**
   * Enable or disable this widget (just blocks user interaction)
   * @param enabled What to do.
   */
  protected void setEnabled(boolean enabled){
    for(FocusWidget wgt : userInteractionWidgets) {
      wgt.setEnabled(enabled);
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
   * Grabs and validates data from the input boxes,
   * then adds group to the local store and refreshes.
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
    
    addGroup(newGroupName, newGroupSize.intValue());
    refreshData();
  }

  // Validation
  
  /**
   * Check that groupName is an alphanumeric string.
   */
  private boolean validateGroupName(final String groupName) {
    // Don't add the group if it's already in the table.
    // TODO: Do we want to just update an existing entry?
    if (GroupManager.getManager().getNames().contains(groupName)) {
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
    } else if (numHosts <= 0) {
      Window.alert("You must have at least one host in a group.");
      return false;
    }
    return true;
  }
  
  // Server communication
  /**
   * Get new data and update display.
   * If we have previously got server data then push and then retrieve.
   * Otherwise we get new server data.
   */
  private void refreshData() {
    setEnabled(false);
    
    final GroupManager man = GroupManager.getManager();
    final PullCallback callback = new PullCallback() {
      @Override
      public void got() {
        updateTable();
        setEnabled(true);
        readyForInput();
      }
    };
    
    if(man.hasServerData()) {
      try {
        man.pushLocalData(new PushCallback() {
          @Override
          public void got() {
            try {
              man.withServerData(callback);
            } catch (MissingRequesterException e) {
              e.printStackTrace();
            }
          }
          
          @Override
          public void failed() {
            new Timer() {
              @Override
              public void run() {
                refreshData();
              }
            };
          }
        });
      } catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
    else {
      try {
        man.withServerData(callback);
      } catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
  }
  
  // Local data
  /**
   * Update the table using the locally stored data.
   */
  private void updateTable() {
    clearTable();
    final GroupManager man = GroupManager.getManager();
    for(String group : man.getNames()) {
      addGroupToTable(group, man.getGroupManager(group).getSize());
    }
    setNumGroups(man.getNames().size());
  }
  
  /**
   * Add group to the local store.
   * @param name Name of the group.
   * @param size Size of the group.
   */
  private void addGroup(String name, int size) {
    SingleGroupManager man = GroupManager.getManager().addGroup(name);
    if(man != null) {
      man.setSize(size);
    }
  }
  
  /**
   * Delete a group from the local store.
   * @param name Name of the group to remove.
   */
  private void removeGroup(String name) {
    GroupManager.getManager().deleteGroup(name);
  }
  
  /**
   * Delete all the groups in the local store.
   */
  private void removeAllGroups() {
    GroupManager.getManager().deleteGroups();
  }
  
  // Display methods
  /**
   * Wipe the table.
   */
  private void clearTable() {
    groupsFlexTable.removeAllRows();
    // Column headers
    groupsFlexTable.setText(0, GROUP_NAME_COLUMN, "Group");
    groupsFlexTable.setText(0, GROUP_HOSTS_COLUMN, "Hosts");
    groupsFlexTable.setText(0, GROUP_REMOVE_COLUMN, "Remove");
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
    userInteractionWidgets.add(removeGroupButton);
    removeGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        removeGroup(newGroupName);
        refreshData();
      }
    });
    groupsFlexTable.setWidget(row, GROUP_REMOVE_COLUMN, removeGroupButton);
  }
  
  /**
   * Updates the displayed number of groups.
   */
  private void setNumGroups(int groups) {
    String txt = groups + " group";
    if(groups != 1)
      txt += "s";
    txt += " added to the system.";
    groupsInfo.setText(txt);
  }
}