package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

//TODO: error of not reaching server must be displayed

public class WorkloadWidget extends Composite {
  ListBox listWklds;

  public WorkloadWidget() {
    initPanel();
  }

  void initPanel() {
    //create FormPanel to send to server
    final FormPanel formPanel = new FormPanel();    
    initWidget(formPanel);
    formPanel.setAction(new AddressResolution().resolve("/workloads"));
    
    //on submission to server, inform manager that workload was uploaded
    formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        WorkloadManager.getManager().workloadUploaded(event.getResults());
        try {
          WorkloadManager.getManager().getServerData();
        } catch (MissingRequesterException e) {
          e.printStackTrace();
        }
      }
    });

    //principal display panel for widget
    VerticalPanel uploadPanel = new VerticalPanel();
    formPanel.setWidget(uploadPanel); 

    //necessary for fileUpload
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setMethod(FormPanel.METHOD_POST);

    //to select file to upload
    final FileUpload wkld_file = new FileUpload();
    wkld_file.setName("wkld_file");
    uploadPanel.add(new Label("Workload file: "));
    uploadPanel.add(wkld_file);
    
    //to name file to be uploaded
    uploadPanel.add(new Label("Workload name: "));
    final TextBox wkld_name = new TextBox();
    wkld_name.setName("wkld_name");
    wkld_name.setText("");
    //maybe create a listener that automatically puts filename -.wkld as the name
    uploadPanel.add(wkld_name);

    //eventually a textbox for workload writing here

    //ListBox to display list of workloads    
    listWklds = new ListBox();
    uploadPanel.add(new Label("Previously uploaded workloads: "));
    uploadPanel.add(listWklds);
    populateListWklds();

    //button to submit all the above when ready
    //validation occurs when pressed prior to form submission
    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        if(validateWorkloadName(wkld_name.getText())) {
          if(wkld_file.getFilename().endsWith(".wkld")) {
            formPanel.submit();
          }
          else {
            Window.alert("Workloads must be of type: .wkld");
          }
          //assuming success, give new workload to workloads class
          listWklds.addItem(wkld_name.getText());
          wkld_name.setText("");
        }
      }

    });
    uploadPanel.add(button);  
  }

  //validate name of workload, checking non-empty and unique
  private boolean validateWorkloadName(String wkldName) {
    if(WorkloadManager.getManager().getNames().contains(wkldName)) {
      Window.alert("You already have a group named '"+wkldName+"'.");
      return false;
    }
    if(wkldName.equals("")) {
      Window.alert("Must have non-empty workload name");
      return false;
    }
    //groups were alphanumeric, is this something enforced for workloads?
    return true;
  }
  
  //initial pulling of list of workloads from server, to populate listWklds
  private void populateListWklds() {
    final WorkloadManager man = WorkloadManager.getManager();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> workloads = man.getNames();
          for(String wkld : workloads) {
            listWklds.addItem(wkld);
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
}