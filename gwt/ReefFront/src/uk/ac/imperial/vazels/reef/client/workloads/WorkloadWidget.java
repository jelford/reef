package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

//TODO: error of not reaching server must be displayed

/**
 * Widget to display information about current workloads on the server, as
 * well to upload new workloads.
 */
public class WorkloadWidget extends Composite implements ManagerChangeHandler {
  
  /**
   * Generated code - gives us an interface to the XML-defined UI.
   */
  private static WorkloadWidgetUiBinder uiBinder = GWT
    .create(WorkloadWidgetUiBinder.class);
  
  /**
   * Generated code. 
   */
  interface WorkloadWidgetUiBinder extends UiBinder<Widget, WorkloadWidget> {
  }
  
  @UiField FlexTable workloadTable;
  @UiField FormPanel formPanel;
  @UiField TextBox wkld_name;
  @UiField FileUpload wkld_file;
  @UiField Button submitBtn;
  
  ListBox listWklds;

  public WorkloadWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    formPanel.setAction(new AddressResolution().resolve("/workloads"));
    
    workloadTable.setText(0, 0, "Name");
    workloadTable.setText(0, 1, "File");
    
    wkld_name.removeFromParent();
    workloadTable.setWidget(1, 0, wkld_name);
    wkld_file.removeFromParent();
    workloadTable.setWidget(1, 1, wkld_file);
    submitBtn.removeFromParent();
    workloadTable.setWidget(1, 2, submitBtn);
    
    WorkloadManager.getManager().addChangeHandler(this);
    
    try {
      WorkloadManager.getManager().getAllServerData();
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }

  /**
   * Grab new data and update display.
   * @param event Form submit event.
   */
  @UiHandler("formPanel")
  void onSubmitComplete(SubmitCompleteEvent event) {
    WorkloadManager.getManager().workloadUploaded(event.getResults().trim());
    
    try {
      WorkloadManager.getManager().getServerData();
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Uploads an workload is the validation is correct.
   * @param event Button click event.
   */
  @UiHandler("submitBtn")
  void onClick(ClickEvent event) {
    if(validateWorkloadName(wkld_name.getText()) && validateWorkloadFile(wkld_file.getFilename())) {
      formPanel.submit();
      setEnabled(false);
    }
    else {
      clearInterface(true);
    }
  }
  
  /**
   * Validate the workload name, checking it's non-empty, alphanumeric and unique.
   * @param wkldName Name to check.
   * @return Whether the name is valid.
   */
  private boolean validateWorkloadName(String wkldName) {
    String name = wkldName.trim();
    
    if(WorkloadManager.getManager().getNames().contains(name)) {
      Window.alert("You already have a group named '"+name+"'.");
      return false;
    }
    else if(!name.matches("^[0-9A-Za-z]{1,}$")) {
      Window.alert("Workload names must be alphanumeric.");
      return false;
    }
    
    return true;
  }
  
  /**
   * Validate the filename of an workload file.
   * @param file The file name.
   * @return Whether the filename is valid.
   */
  private boolean validateWorkloadFile(String file) {
    if(file.endsWith(".wkld")) {
      return true;
    }
    else {
      Window.alert("Workload files must have the '.wkld' extension.");
      return false;
    }
  }
  
  /**
   * Enable or disable the widget controls.
   * @param enabled Whether or not to enable.
   */
  protected void setEnabled(boolean enabled) {
    wkld_name.setEnabled(enabled);
    wkld_file.setEnabled(enabled);
    submitBtn.setEnabled(enabled);
  }
  
  /**
   * Clear all input fields.
   * @param highlight Should we just highlight the name instead?
   */
  protected void clearInterface(boolean highlight) {
    if(highlight) {
      wkld_name.selectAll();
    }
    else {
      wkld_name.setText("");
    }
    // Cannot reset a file widget :(
  }
  
  /**
   * Updates the interface whenever local workload data changes.
   * <p>
   * If we allow editing of workloads we must make sure this handler is added to
   * all workloads rather than just the list.
   * <p>
   * i.e. Use {@link WorkloadManager#addChangeHandler(ManagerChangeHandler, boolean)}
   * with the second argument {@code true}
   */
  public void change(IManager m) {
    try {
      WorkloadManager.getManager().withAllServerData(new PullCallback() {
        @Override
        public void got() {
          updateInterface();
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Updates the interface with the newest data.
   */
  protected void updateInterface() {
    final WorkloadManager man = WorkloadManager.getManager();
    clearTable();
    Set<String> wklds = man.getNames();
    for(String wkld : wklds) {
      SingleWorkloadManager wMan = man.getWorkloadManager(wkld);
      addWorkloadToTable(wMan.getName(), wMan.getDownloadURL());
    }
    
    setEnabled(true);
    clearInterface(false);
  }
  
  /**
   * Add a single workload to the table.
   * @param name Name of the workload.
   * @param url URL to download the workload.
   */
  private void addWorkloadToTable(final String name, final String url) {
    int row = workloadTable.getRowCount()-1;
    workloadTable.insertRow(row);
    // Add the workload to the table.
    workloadTable.setText(row, 0, name);
    
    Button download = new Button("Download");
    download.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open(url, "", "");
      }
    });
    workloadTable.setWidget(row, 1, download);
  }
  
  /**
   * Wipe the table.
   */
  private void clearTable() {
    while(workloadTable.getRowCount() > 2) {
      workloadTable.removeRow(1);
    }
  }
}