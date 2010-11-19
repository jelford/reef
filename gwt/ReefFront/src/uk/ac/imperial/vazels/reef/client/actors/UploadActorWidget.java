package uk.ac.imperial.vazels.reef.client.actors;

//import java.awt.TextField;
import uk.ac.imperial.vazels.reef.client.AddressResolution;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

public class UploadActorWidget extends Composite {

  //private ActorSummary actors;

  public UploadActorWidget() {
    initPanel();
//    refresh();
  }

  void initPanel() {
    final FormPanel formPanel = new FormPanel();    
    initWidget(formPanel);

    formPanel.setAction(new AddressResolution().resolve("/actors"));
    
    VerticalPanel uploadPanel = new VerticalPanel();
    formPanel.setWidget(uploadPanel); 

    //necessary for fileUpload
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setMethod(FormPanel.METHOD_POST);

    FileUpload actor_file = new FileUpload();
    actor_file.setName("actor_file");//for server
    uploadPanel.add(actor_file);
    //validation here

    TextBox actor_name = new TextBox();
    actor_name.setName("actor_name");//for server
    actor_name.setText("");
//maybe create a listener that automatically puts filename as the name
    uploadPanel.add(actor_name);

    final ListBox actorLanguage = new ListBox();
    actorLanguage.addItem("JAVA");
    actorLanguage.addItem("PYTHON");
    uploadPanel.add(actorLanguage);
    
    final TextBox actorLang = new TextBox(); //for server
    actorLang.setName("actor_type"); 
    uploadPanel.add(actorLang);
    actorLang.setVisible(false);

    formPanel.addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        actorLang.setText(actorLanguage.getValue(actorLanguage.getSelectedIndex()));
        //Actors.add(actorLang.getText());
      }
    });    
    
    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
      }
    });
    uploadPanel.add(button);
  }

  /* The below is the code required to get workload information from the server.*/
  /**
   * Dispatch a request to the server letting it know we'd like to hear about
   * workload info.
   */
/*  private void refresh() {
    new WorkloadDataRequest().go(new RequestHandler<ActorSummary>(){
      @Override
      public void handle(ActorSummary reply, boolean success, String message) {
        if (success) {
          refreshWorkloadListBox(reply);
        }
      }
    });
  }*/

/*  private void refreshWorkloadListBox(final ActorSummary summary) {
    actorsBox.clear();
    actors = summary;    
    for(int i = 0; i < actors.size(); i++) {
      actorsBox.addItem(actors.get(i));
    }
    //    refreshWorkloadsInfo();
  }
*/
  /**
   * Helper class to send requests to get workload info (this will send a batch
   * request to the server, and retrieve a summary of all workload info).
   */
/*  private class WorkloadDataRequest extends MultipleRequester<ActorSummary>{
    WorkloadDataRequest() {
      super(RequestBuilder.GET, "/workloads/", 
          new Converter<ActorSummary>() {
        @Override
        public ActorSummary convert(String original) {
          return new ActorSummary(original);
        }
      });
    }
  }*/
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