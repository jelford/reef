package uk.ac.imperial.vazels.reef.client.workloads;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

public class WorkloadTestWidget extends Composite {

  @UiField
  FormPanel form;
  
  @UiField
  ListBox workloads;
  
  @UiField
  Button submit;
  
  private static WorkloadTestWidgetUiBinder uiBinder = GWT
      .create(WorkloadTestWidgetUiBinder.class);

  interface WorkloadTestWidgetUiBinder extends
      UiBinder<Widget, WorkloadTestWidget> {
  }

  public WorkloadTestWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    form.setAction(new AddressResolution().resolve("/workloads"));
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);
    
    updateWorkloadList();
  }

  @UiHandler("submit")
  void onClick(ClickEvent event) {
    form.submit();
  }
  
  @UiHandler("form")
  void onSubmitComplete(SubmitCompleteEvent event) {
    WorkloadManager.getManager().workloadUploaded(event.getResults());
    updateWorkloadList();
  }
  
  private void updateWorkloadList() {
    final WorkloadManager man = WorkloadManager.getManager();
    try {
      man.withServerData(new PullCallback() {
        @Override
        public void got() {
          workloads.clear();
          for(String name : man.getNames()) {
            workloads.addItem(name);
          }
        }
      });
    } catch(MissingRequesterException e) {
      
    }
  }
}
