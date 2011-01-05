package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.groups.GroupManager;
import uk.ac.imperial.vazels.reef.client.groups.SingleGroupManager;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

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

/* A class to allow the assigning of workloads to groups
 * 
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
  
  //wkldsBox is list of workloads
  //groupsBox is list of groups
  //attachedWklds is list of workloads attached to currently shown group
  @UiField ListBox wkldsBox;
  @UiField ListBox groupsBox;
  @UiField ListBox attachedWklds;

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
          updateWkldsBox();        
        }      
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
    
    //obtain current workloads assigned to default selected group in group ListBox
    updateAttachedWklds();    
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
      }
    });
    
    //selected group, which will change, in groupsBox determines attachedWklds box, which is list of workloads in that selected group
    groupsBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateAttachedWklds();        
      }
    });

    //display list of workloads
    //note: handler only required if expect workloads to change after widget loads
    WorkloadManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateWkldsBox();
      }
    });
  }
  
  @UiHandler("submitWtoG")
  void onClick(ClickEvent event) {
    addWorkload();
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

  //update the list of workloads from server
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