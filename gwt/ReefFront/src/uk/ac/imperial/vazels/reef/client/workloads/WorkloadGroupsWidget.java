package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.groups.GroupManager;
import uk.ac.imperial.vazels.reef.client.groups.SingleGroupManager;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/* A class to allow the assigning, including uploading, of workloads to groups
 * 
 */
public class WorkloadGroupsWidget extends Composite {

  //wkldsBox is list of workloads
  //groupsBox is list of groups
  //attachedWklds is list of workloads attached to currently shown group
  ListBox wkldsBox, groupsBox, attachedWklds;

  public WorkloadGroupsWidget() {
    initPanel();
    //obtain current groups on server to put in box
    try {
      GroupManager.getManager().withServerData(new PullCallback() {
        public void got() {
          updateGroupsBox();        
        }      
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }    

    try {
      WorkloadManager.getManager().withServerData(new PullCallback() {
        public void got() {
          updateWkldsBox();        
        }      
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }    
    //obtain current workloads assigned to selected group
    updateAttachedWklds();    
  }

  /* Initialises widget, initialises the 3 ListBox objects and submit button.
   */
  void initPanel() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    //display list of groups
    assignmentTab.add(new Label("Groups: "));
    groupsBox = new ListBox();

    //handler only required if expect groups to change after widget loads
    GroupManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateGroupsBox();
      }
    });

    
    groupsBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateAttachedWklds();        
      }
    });
    
    assignmentTab.add(groupsBox);

    //display list of workloads
    assignmentTab.add(new Label("Select a workload to assign:"));

    wkldsBox = new ListBox();
    //handler only required if expect workloads to change after widget loads

    WorkloadManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateWkldsBox();
      }
    });

    assignmentTab.add(wkldsBox);

    //display list of currently attached workloads
    assignmentTab.add(new Label("Workloads currently attached to selected group:"));

    attachedWklds = new ListBox();
    assignmentTab.add(attachedWklds);

    //submit button for selected workload
    Button submitWtoG = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        addWorkload();
      }
    });
    assignmentTab.add(submitWtoG);
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

  //update the list of workloads
  //Note: this will need to become more like updateGroupsBox
  private void updateWkldsBox() {
    final WorkloadManager man = WorkloadManager.getManager();
    wkldsBox.clear();
    //get the list of groups from the server and add any new items to the group box
    Set<String> wklds = man.getNames();
    for(String w: wklds) {
      wkldsBox.addItem(w);
    }
  }

  //update attached workloads for the current group
  private void updateAttachedWklds() {
    attachedWklds.clear();
    //need there to be a group present to have a group selected
    if(groupsBox.getItemCount() > 0) {
      GroupManager manager = GroupManager.getManager();
      final SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
      //update known groups from server, and on callback get and use the new workload data
      try {
        gpManager.withServerData(new PullCallback() {
          public void got() {
            String [] theAttachedWklds = gpManager.getWorkloads();
            for(String wkld: theAttachedWklds) {
              attachedWklds.addItem(wkld);
            }            
          }
        });
      }
      catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
  }

  //add selected workload to selected group and then push this new data to server
  public void addWorkload() {
    GroupManager manager = GroupManager.getManager();
    SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
    gpManager.addWorkload(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
    try {
      gpManager.pushLocalData(new PushCallback() {
        //show submission occurred
        public void got() {
          updateAttachedWklds(); 
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