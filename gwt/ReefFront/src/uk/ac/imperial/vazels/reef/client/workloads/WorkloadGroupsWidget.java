package uk.ac.imperial.vazels.reef.client.workloads;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.MultipleRequester.Converter;
//import uk.ac.imperial.vazels.reef.client.workloads.WorkloadWidget.WorkloadDataRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WorkloadGroupsWidget extends Composite {

  private WorkloadSummary workloads;
  ListBox wkldsBox; //make this non static?
  
  //panel
  //group and workload data in panel
  //later display + obtain existing workload data for groups
  //allow selection of workload data to group
  //submit to the group
  
  public WorkloadGroupsWidget() {
    //initialise group and workloads global object
    initPanel();
    refresh();
  }

  void initPanel() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    wkldsBox = new ListBox();
    assignmentTab.add(wkldsBox);
    ListBox groupsBox = new ListBox(true);
    assignmentTab.add(groupsBox);

    for(String wkld: Workloads.returnWorkloads()) {
      wkldsBox.addItem(wkld);
    }
    
//get current versions of workloads and groups on server...    
    //delete following 5 lines after tested
//    wkldsBox.addItem("Workload1");
//    wkldsBox.addItem("Workload2");
//    groupsBox.addItem("Group1");
//    groupsBox.addItem("Group2");
//    groupsBox.addItem("Group3");

    //need groups and workloads info input into box
    //need choose which workloads to attach workload to?
    Button submitWtoG = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
       // formPanel.submit();//?????
      }
    });
    assignmentTab.add(submitWtoG);    

    //is this automatic already?
    Button refreshOptions = new Button ("Refresh", new ClickHandler() {
      public void onClick(ClickEvent event) {
     //   refresh();
      }
    });     
    assignmentTab.add(refreshOptions);
  }

  
  /* The below is the code required to get workload information from the server.*/
  /**
   * Dispatch a request to the server letting it know we'd like to hear about
   * workload info.
   */
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
  }

  /**
   * Helper class to send requests to get workload info (this will send a batch
   * request to the server, and retrieve a summary of all workload info).
   */
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
}
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