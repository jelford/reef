package uk.ac.imperial.vazels.reef.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.DecoratedTabBar;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.PushButton;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.DecoratedStackPanel;

public class AllocateGroups extends Composite {
  private VerticalPanel mainPanel = new VerticalPanel();
  private DecoratorPanel decoratorPanel = new DecoratorPanel();
  private DecoratorPanel decoratorPanel2 = new DecoratorPanel();
  private DecoratorPanel mainDecPanel = new DecoratorPanel();
  private VerticalPanel vertPanel = new VerticalPanel();
  private FlexTable groupsFlexTable = new FlexTable();
  private FlexTable groupsPanel = new FlexTable();
  private HorizontalPanel addPanel = new HorizontalPanel();
  private TextBox newGroupTextBox = new TextBox();
  private TextBox newHostsTextBox = new TextBox();
  private Button addGroupButton = new Button("Add");
  private Button addHostsButton = new Button("Add");
  private Label lastUpdatedLabel = new Label();
  private ArrayList<String> groups = new ArrayList<String>();
  private ArrayList<String> hosts = new ArrayList<String>();
  private final Label lblGroupName = new Label("Group Name  ");
  private final Label lblHostsNumber = new Label("  Hosts Number  ");
  private final HorizontalPanel hPanel = new HorizontalPanel();
  private final Button btnReset = new Button("Reset");
  private final Button btnSubmit = new Button("Submit");

  public AllocateGroups() {

    VerticalPanel mainPanel = new VerticalPanel();
 
    initWidget(mainPanel);
    mainPanel.setSize("521px", "222px");

    addPanel.add(lblGroupName);
    addPanel.add(newGroupTextBox);
    addPanel.add(lblHostsNumber);
    addPanel.add(newHostsTextBox);
    addPanel.add(addGroupButton);

    addPanel.setSpacing(2);
    // Assemble Add Group panel.
    addPanel.setSize("521px", "22px");
    decoratorPanel.add(addPanel);
    vertPanel.add(decoratorPanel);
    //mainPanel.add(addPanel);
    

    groupsFlexTable.setText(0, 0, "Group ");
    groupsFlexTable.setText(0, 1, "Hosts ");
    // groupsFlexTable.setText(0, 2, "RemoveHosts ");
    groupsFlexTable.setText(0, 2, "Remove ");
    // groupsPanel.setWidget( 0, 1, newGroupTextBox) ;

    // Assemble Main panel.
   // decoratorPanel2.add(groupsFlexTable);
    groupsFlexTable.setSize("521px", "33px");
    //vertPanel.add(decoratorPanel2);
    vertPanel.add(groupsFlexTable);
    

    setGroupsPanel();
   vertPanel.add(groupsPanel);

 
    // Move cursor focus to the input box.
    newGroupTextBox.setFocus(true);
    hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    hPanel.setSpacing(5);
    btnReset.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {

        groups.remove(groups);
        groups = new ArrayList<String>();
        groupsFlexTable.removeAllRows();

        groupsFlexTable.setText(0, 0, "Group ");
        groupsFlexTable.setText(0, 1, "Hosts ");
        groupsFlexTable.setText(0, 2, "Remove ");

        setGroupsPanel();

      }
    });
    hPanel.add(btnReset);
    hPanel.setCellHorizontalAlignment(btnReset, HasHorizontalAlignment.ALIGN_RIGHT);
    hPanel.add(btnSubmit);
    hPanel.setCellHorizontalAlignment(btnSubmit, HasHorizontalAlignment.ALIGN_RIGHT);

    vertPanel.add(hPanel);
    vertPanel.setCellHorizontalAlignment(hPanel, HasHorizontalAlignment.ALIGN_RIGHT);
    //mainDecPanel.setCellHorizontalAlignment(hPanel,
    //    HasHorizontalAlignment.ALIGN_RIGHT);
    
    mainDecPanel.add(vertPanel);
    mainPanel.add(mainDecPanel);
      
 

    // Listen for mouse events on the Add button.
    addGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addGroup();
        setGroupsPanel();
      }
    });
    // Listen for keyboard events in the input box.
    newGroupTextBox.addKeyPressHandler(new KeyPressHandler() {
      public void onKeyPress(KeyPressEvent event) {
        if (event.getCharCode() == KeyCodes.KEY_ENTER) {
          addGroup();
          setGroupsPanel();
        }
      }
    });

  }

  /**
   * Add group to FlexTable. Executed when the user clicks the addGroupButton or
   * presses enter in the newGroupTextBox.
   */
  private void addGroup() {
    final String symbol = newGroupTextBox.getText().trim();
    newGroupTextBox.setFocus(true);

    // Group code must be between 1 and 10 chars that are numbers, letters, or
    // dots.
    if (!symbol.matches("^[0-9A-Za-z\\.]{1,10}$")) {
      Window.alert("'" + symbol + "' is not a valid symbol.");
      newGroupTextBox.selectAll();
      newHostsTextBox.setText("");
      return;
    }

    newGroupTextBox.setText("");

    // Don't add the group if it's already in the table.
    if (groups.contains(symbol)) {
      newHostsTextBox.setText("");
      return;
    }

    // Add the group to the table.
    int row = groupsFlexTable.getRowCount();
    groups.add(symbol);
    groupsFlexTable.setText(row, 0, symbol);
    groupsFlexTable.setText(row, 1, "0");

    // Add a button to remove this group from the table.
    Button removeGroupButton = new Button("x");
    removeGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        removeGroup(symbol);
      }
    });
    groupsFlexTable.setWidget(row, 2, removeGroupButton);

    addHosts();

  }

  private void removeGroup(String symbol) {
    int removedIndex = groups.indexOf(symbol);
    groups.remove(removedIndex);
    groupsFlexTable.removeRow(removedIndex + 1);
    setGroupsPanel();

  }

  private void addHosts() {
    final String symbol = newHostsTextBox.getText().trim();
    newHostsTextBox.setFocus(true);

    // Host code must be a number
    if (!symbol.matches("^[0-9]{1,10}$")) {
      Window.alert("'" + symbol + "' is not a valid symbol.");
      newHostsTextBox.selectAll();
      return;
    }

    newHostsTextBox.setText("");

    // Add the hosts number to the group in the table.
    int row = groupsFlexTable.getRowCount() - 1;

    groupsFlexTable.setText(row, 1, symbol);

  }

  private void setGroupsPanel() {
    groupsPanel.setText(0, 0, (groupsFlexTable.getRowCount() - 1) + "");
    if (groupsFlexTable.getRowCount() == 2) {
      groupsPanel.setText(0, 1, " group added to the system.");
    } else {
      groupsPanel.setText(0, 1, " groups added to the system.");
    }

  }
}
