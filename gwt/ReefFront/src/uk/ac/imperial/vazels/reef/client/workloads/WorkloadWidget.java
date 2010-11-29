package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.ArrayList;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

//error of not reaching server must be displayed
//also when 2 wklds given same name

public class WorkloadWidget extends Composite {

  public WorkloadWidget() {
    initPanel();
  }

  void initPanel() {
    final FormPanel formPanel = new FormPanel();    
    initWidget(formPanel);

    formPanel.setAction(new AddressResolution().resolve("/workloads"));

    VerticalPanel uploadPanel = new VerticalPanel();
    formPanel.setWidget(uploadPanel); 

    //necessary for fileUpload
    formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
    formPanel.setMethod(FormPanel.METHOD_POST);

    FileUpload wkld_file = new FileUpload();
    wkld_file.setName("wkld_file");
    uploadPanel.add(wkld_file);
    //validation for .wkld needed

    final TextBox wkld_name = new TextBox();
    wkld_name.setName("wkld_name");
    wkld_name.setText("");
//maybe create a listener that automatically puts filename as the name
    uploadPanel.add(wkld_name);

    // eventually a textbox for workload writing here
    
//listbox to display list of workloads    
    final ListBox listWklds = new ListBox();
    uploadPanel.add(listWklds);
    
    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
        Workloads.add(wkld_name.getText());
        listWklds.addItem(wkld_name.getText());
        wkld_name.setText("");
      }
    });
    uploadPanel.add(button);  
    
  }
}
