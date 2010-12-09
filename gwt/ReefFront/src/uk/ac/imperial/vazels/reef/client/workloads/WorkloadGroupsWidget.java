package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.managers.GroupManager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.SingleGroupManager;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkloadGroupsWidget extends Composite {

  //  private WorkloadSummary workloads;
  ListBox wkldsBox, groupsBox;
  TextBox attachedWklds; //make this non static?

  public WorkloadGroupsWidget() {
    initPanel();
    //    refresh();
  }

  void initPanel() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    assignmentTab.add(new Label("Workloads: "));

    wkldsBox = new ListBox();
    assignmentTab.add(wkldsBox);

    assignmentTab.add(new Label("Groups: "));

    groupsBox = new ListBox();
/*    ChangeHandler handler = new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        updateAttachedWklds();
      }
    };*/
    ClickHandler updateGroupList = new ClickHandler(){
      @Override
      public void onClick(ClickEvent event) {
        updateGroupsBox();
      }
    };
    groupsBox.addClickHandler(updateGroupList);
//    groupsBox.addChangeHandler(handler);
    assignmentTab.add(groupsBox);

    assignmentTab.add(new Label("Currently attached workloads: "));

    attachedWklds = new TextBox();
    attachedWklds.setReadOnly(true);
    assignmentTab.add(attachedWklds);
    updateAttachedWklds();
    for(String wkld: Workloads.returnWorkloads()) {
      wkldsBox.addItem(wkld);
    }

    updateGroupsBox();

    //need groups and workloads info input into box
    //need choose which workloads to attach workload to?
    Button submitWtoG = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        addWorkload();
        showSubmission();
      }
    });
    assignmentTab.add(submitWtoG);
  }

  //update attached workloads for the current 
  private void updateAttachedWklds() {
    if(groupsBox.getItemCount() > 0) {
      String groupWklds = "";
      GroupManager manager = GroupManager.getManager();
      SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
      String [] theAttachedWklds = gpManager.getWorkloads();
      for(String wkld: theAttachedWklds) {
        groupWklds += (wkld + "\n");
      }
      attachedWklds.setText(groupWklds);
    }
  }
  private void updateGroupsBox() {
    final GroupManager man = GroupManager.getManager();
    try {
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> groups = man.getNames(); //returns Set<String>
          for(String group: groups) {
            groupsBox.addItem(group);
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }

  }
  public void addWorkload() {
    GroupManager manager = GroupManager.getManager();
    SingleGroupManager gpManager = manager.getGroupManager(groupsBox.getItemText(groupsBox.getSelectedIndex()));
    gpManager.addWorkload(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
    try {
      gpManager.pushLocalData(null);
    }
    catch(MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  private void showSubmission() {
    updateAttachedWklds(); 
  }
}

/* The below is the code for server interaction.*/
/**
 * Post all the current group data to the server & update the local data with
 * the returned info from the server (i.e. check that local and remote records
 * are the same).
 */
/*  private class GroupDataUpdate extends MultipleRequester<GroupSummary>{
    public GroupDataUpdate() {
      super(RequestBuilder.POST, "/groups/", new Converter<GroupSummary>(){
        public GroupSummary convert(String original) {
          return new GroupSummary(original);
        }      });}
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
    protected void received(GroupSummary reply, boolean success, String message) {
      if(success)
        refreshGroupData(reply);
    }
  }

  private class GroupWorkloadPostRequest extends MultipleRequester<GroupSummary>{
    GroupWorkloadPostRequest() {
      super(RequestBuilder.POST, "/groups/", null);
    }



  }


  /**
 * Dispatch a request to the server letting it know we'd like to hear about
 * workload info.
 *//*
  private void refresh() {
    new WorkloadDataRequest().go(new RequestHandler<WorkloadSummary>(){
      @Override
      public void handle(WorkloadSummary reply, boolean success, String message) {
        if (success) {
          refreshWorkloadListBox(reply);
        }
      }
    });
  }

  private void refreshWorkloadListBox(final WorkloadSummary summary) {
    wkldsBox.clear();
    workloads = summary;    
    for(int i = 0; i < workloads.size(); i++) {
      wkldsBox.addItem(workloads.get(i));
    }
    //    refreshWorkloadsInfo();
  }*/

/**
 * Helper class to send requests to get workload info (this will send a batch
 * request to the server, and retrieve a summary of all workload info).
 *//*
  private class WorkloadDataRequest extends MultipleRequester<WorkloadSummary>{
    WorkloadDataRequest() {
      super(RequestBuilder.GET, "/workloads/", 
          new Converter<WorkloadSummary>() {
        @Override
        public WorkloadSummary convert(String original) {
          return new WorkloadSummary(original);
        }
      });
    }
  }
}*/
/**
 * refreshWorkloadData with no data.
 */
//  private void clearWorkloadData() {
//    WorkloadSummary clearAllWorkloads = new WorkloadSummary();
//    if (workloads != null) {
//      for (String workloadName : workloads.keySet()) {
//        clearAllWorkloads.put(workloadName, 0);
//     }
//   }
//    refreshWorkloadData(clearAllWorkloads);
//}
/** no group updating here
 * Post all the current workload data to the server & update the local data with
 * the returned info from the server (i.e. check that local and remote records
 * are the same).
 */ 
/* 
  private class WorkloadDataUpdate extends MultipleRequester<WorkloadSummary>{
    /*  public WorkloadDataUpdate() {
      super(RequestBuilder.GET, "/workloads/", new Converter<WorkloadSummary>(){
        @Override
        public WorkloadSummary convert(String original) {
          return new WorkloadSummary(original);
        }
      });
    }*/
/*
    @Override
    protected void received(WorkloadSummary reply, boolean success, String message) {
      if(success)
        refreshWorkloadData(reply);
    }
  }*/

/**
 * Tell the server about the workloads in our table
 */
//  private void batchUpdateServerWorkloads() {
/*      protected QueryArg[] getArgs() {deleted     */