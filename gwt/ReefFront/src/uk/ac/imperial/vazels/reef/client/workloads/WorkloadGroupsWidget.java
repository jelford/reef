package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.groups.GroupManager;
import uk.ac.imperial.vazels.reef.client.groups.SingleGroupManager;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;
import uk.ac.imperial.vazels.reef.client.sue.SueComponentManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *  A class to allow the assigning of workloads to groups
 */
public class WorkloadGroupsWidget extends Composite {
  /**
   * Generated code - gives us an interface to the XML-defined UI.
   */
  private static WorkloadGroupsWidgetUiBinder uiBinder = GWT
  .create(WorkloadGroupsWidgetUiBinder.class);

  /**
   * Generated code. 
   */
  interface WorkloadGroupsWidgetUiBinder extends UiBinder<Widget, WorkloadGroupsWidget> {
  }

  @UiField ListBox groupsBox;
  @UiField ListBox attachedWklds;
  @UiField ListBox executablesBox;
  
  private void setUiElementsEnabled(boolean enabled) {
    updateExecutablesBox();
    groupsBox.setEnabled(enabled);
    attachedWklds.setEnabled(enabled);
    executablesBox.setEnabled(enabled);
  }

  private int mSueLabelIndex;
  private int mWkldLabelIndex;

  public WorkloadGroupsWidget() {
    initWidget(uiBinder.createAndBindUi(this));

    initPanel();

    //obtain current groups to put in a ListBox
    try {
      GroupManager.getManager().withAllServerData(new PullCallback() {
        public void got() {
          updateGroupsBox();
        }      
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }    

    //obtain current workloads to put in a ListBox
    try {
      WorkloadManager.getManager().withAllServerData(new PullCallback() {
        public void got() {  
          updateExecutablesBox();
        }      
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }

    try {
      SueComponentManager.getManager().withAllServerData(new PullCallback() {
        public void got() {
          updateExecutablesBox();
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }

    //obtain current workloads assigned to default selected group in group ListBox
    updateAttachedWkldsAndSue();
  }

  /* 
   * Initialises widget, initialises the 3 ListBox objects and submit button.
   */
  void initPanel() {
    //on event of change to groups, update list of groups
    GroupManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateGroupsBox();
        updateAttachedWkldsAndSue();
        updateExecutablesBox();
      }
    });

    //selected group, which will change, in groupsBox determines attachedWklds box, which is list of workloads in that selected group
    groupsBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateAttachedWkldsAndSue();  
        updateExecutablesBox();
      }
    });

    //display list of workloads
    //note: handler only required if expect workloads to change after widget loads
    WorkloadManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateExecutablesBox();
      }
    });

    SueComponentManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateExecutablesBox();
      }
    });
  }

  @UiHandler("submitWtoG")
  void onClick(ClickEvent event) {
    addItems();
  }

  //update the groups box using list of groups from server, possibly only needed once each time widget runs
  private void updateGroupsBox() {
    final GroupManager man = GroupManager.getManager();
    groupsBox.clear();
    Set<String> groups = man.getNames();
    for(String g: groups) {
      groupsBox.addItem(g);
    }
  }

  private void updateExecutablesBox() {
    final WorkloadManager wkldMan = WorkloadManager.getManager();
    final SueComponentManager sueMan = SueComponentManager.getManager();
    final SingleGroupManager gMan;
    try {
      gMan = GroupManager.getManager().getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
      if (gMan == null) {
        return;
      }
    } catch (NullPointerException e) {
      return; // widget not properly initialised
    } catch (IndexOutOfBoundsException e) {
      return; // list box not initialised
    }
    executablesBox.clear();
    mWkldLabelIndex = executablesBox.getItemCount();
    executablesBox.addItem("--- Workloads ---");
    for (String wkldName : wkldMan.getNames()) {
      boolean toAdd = true;
      String[] assignedWorkloads = gMan.getWorkloads();
      for(int i=0; i<assignedWorkloads.length; i++) {
        if (assignedWorkloads[i].equals(wkldName)) {
          toAdd = false;
        }
      }
      if (toAdd) {
        executablesBox.addItem(wkldName);
      }
    }
    mSueLabelIndex = executablesBox.getItemCount();
    executablesBox.addItem("--- SUE Components ---");
    for (String sueName : sueMan.getNames()) {
      boolean toAdd = true;
      String[] assignedSueComponents = gMan.getSueComponents();
      for(int i=0; i<assignedSueComponents.length; i++) {
        if (assignedSueComponents[i].equals(sueName)) {
          toAdd = false;
        }
      }
      if (toAdd) {
        executablesBox.addItem(sueName);
      }
    }
  }

  //update attached workloads for the current group
  private void updateAttachedWkldsAndSue() {
    attachedWklds.clear();
    //need there to be a group present to have a group selected
    if(groupsBox.getItemCount() > 0) {
      GroupManager manager = GroupManager.getManager();
      final SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
      if (gpManager == null) {
        return; // The GroupsManager hasn't finished initialising, or the selection is empty.
      }
      //update known groups from server, and on callback get and use the new workload data
      try {
        gpManager.withServerData(new PullCallback() {
          public void got() {
            String [] theAttachedWklds = gpManager.getWorkloads();
            String [] theAttachedSueComponents = gpManager.getSueComponents();
            for(String wkld: theAttachedWklds) {
              attachedWklds.addItem(wkld);
            }
            for (String sue: theAttachedSueComponents) {
              attachedWklds.addItem(sue);
            }
          }
        });
      } catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
  }

  //add selected workload to selected group and then push this new data to server
  public void addItems() {
    GroupManager manager = GroupManager.getManager();
    SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));

    int selectedItem = executablesBox.getSelectedIndex();
    if (selectedItem == mSueLabelIndex || selectedItem == mWkldLabelIndex) {
      Window.alert("You need to pick either a Workload or a Sue Component to assign to the group");
      return;
    } 

    if (selectedItem < mSueLabelIndex) {
      gpManager.addWorkload(executablesBox.getItemText(selectedItem));
    } else {
      gpManager.addSueComponent(executablesBox.getItemText(selectedItem));
    }

    try {
      setUiElementsEnabled(false);
      gpManager.pushLocalData(new PushCallback() {
        //show submission occurred
        public void got() {
          updateAttachedWkldsAndSue();
          setUiElementsEnabled(true);
        }

        public void failed() {
          Window.alert("Failure: no confirmation from server that workload assignment succeeded");
        }        
      });
    }
    catch(MissingRequesterException e) {
      e.printStackTrace();
    }
  }
}