package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

//error of not reaching server must be displayed
//also when 2 wklds given same name

public class WorkloadWidget extends Composite {
  ListBox listWklds;

  public WorkloadWidget() {
    initPanel();
  }

  void initPanel() {
    final FormPanel formPanel = new FormPanel();    
    initWidget(formPanel);

    formPanel.setAction(new AddressResolution().resolve("/workloads"));
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

    VerticalPanel uploadPanel = new VerticalPanel();
    formPanel.setWidget(uploadPanel); 

    //necessary for fileUpload
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setMethod(FormPanel.METHOD_POST);

    FileUpload wkld_file = new FileUpload();
    wkld_file.setName("wkld_file");
    uploadPanel.add(new Label("Workload file: "));
    uploadPanel.add(wkld_file);
    //validation for .wkld needed

    final TextBox wkld_name = new TextBox();

    wkld_name.setName("wkld_name");
    wkld_name.setText("");
    //maybe create a listener that automatically puts filename as the name
    uploadPanel.add(new Label("Workload name: "));
    uploadPanel.add(wkld_name);

    // eventually a textbox for workload writing here

    //listbox to display list of workloads    
    listWklds = new ListBox();
    uploadPanel.add(new Label("Previously uploaded workloads: "));
    uploadPanel.add(listWklds);
    populateListWklds();

    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
        //TODO: HANDLER!!!
        //assuming success, give new workload to workloads class
        listWklds.addItem(wkld_name.getText());
        wkld_name.setText("");
      }
    });
    uploadPanel.add(button);  
  }

  private void populateListWklds() {
    final WorkloadManager man = WorkloadManager.getManager();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> workloads = man.getNames(); //returns Set<String>
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