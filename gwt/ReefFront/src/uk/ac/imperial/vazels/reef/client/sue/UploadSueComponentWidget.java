package uk.ac.imperial.vazels.reef.client.sue;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.actors.ActorManager;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

/**
 * Widget to display information about current actors on the server, as
 * well to upload new actors.
 */
public class UploadSueComponentWidget extends Composite implements ManagerChangeHandler {
  /**
   * Generated code - gives us an interface to the XML-defined UI.
   */
  private static UploadActorWidgetUiBinder uiBinder = GWT
    .create(UploadActorWidgetUiBinder.class);
  
  /**
   * Generated code. 
   */
  interface UploadActorWidgetUiBinder extends UiBinder<Widget, UploadSueComponentWidget> {
  }
  
  @UiField FormPanel formPanel;
  @UiField TextBox suecomponent_name;
  @UiField FileUpload sue_file;
  @UiField Button submitBtn;
  @UiField FlexTable sueTable;

  public UploadSueComponentWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    formPanel.setAction(new AddressResolution().resolve("/sue"));
    
    sueTable.setText(0, 0, "Name");
    sueTable.setText(0, 1, "File");
    
    suecomponent_name.removeFromParent();
    sueTable.setWidget(1, 0, suecomponent_name);
    sue_file.removeFromParent();
    sueTable.setWidget(1, 1, sue_file);
    submitBtn.removeFromParent();
    sueTable.setWidget(1, 3, submitBtn);
    
    SueComponentManager.getManager().addChangeHandler(this);
    
    try {
      SueComponentManager.getManager().getAllServerData();
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
    SueComponentManager.getManager().sueComponentUploaded(event.getResults().trim());
    
    try {
     SueComponentManager.getManager().getServerData();
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Uploads an actor is the validation is correct.
   * @param event Button click event.
   */
  @UiHandler("submitBtn")
  void onClick(ClickEvent event) {
    if(validateSueComponentName(suecomponent_name.getText()) && validateSueComponentFile(sue_file.getFilename())) {
      formPanel.submit();
      setEnabled(false); // will be reenabled when the interface is updated.
    }
    else {
      clearInterface(true);
    }
  }

  /**
   * Validate the actor name, checking it's non-empty, alphanumeric and unique.
   * @param actorName Name to check.
   * @return Whether the name is valid.
   */
  private boolean validateSueComponentName(String actorName) {
    String name = actorName.trim();
    
    if(SueComponentManager.getManager().getNames().contains(name)) {
      Window.alert("You already have a Sue Component named '"+name+"'.");
      return false;
    }
    else if(!name.matches("^[0-9A-Za-z]{1,}$")) {
      Window.alert("Sue Component names must be alphanumeric.");
      return false;
    }
    
    return true;
  }
  
  /**
   * Validate the filename of an actor file.
   * @param file The file name.
   * @return Whether the filename is valid.
   */
  private boolean validateSueComponentFile(String file) {
    if(file.endsWith(".tar.gz")) {
      return true;
    }
    else {
      Window.alert("Actor files must be .tar.gz files.");
      return false;
    }
  }
  
  /**
   * Enable or disable the widget controls.
   * @param enabled Whether or not to enable.
   */
  protected void setEnabled(boolean enabled) {
    suecomponent_name.setEnabled(enabled);
    sue_file.setEnabled(enabled);
    submitBtn.setEnabled(enabled);
  }
  
  /**
   * Clear all input fields.
   * @param highlight Should we just highlight the name instead?
   */
  protected void clearInterface(boolean highlight) {
    if(highlight) {
      suecomponent_name.selectAll();
    }
    else {
      suecomponent_name.setText("");
    }
    // Cannot reset a file widget :(
  }
  
  /**
   * Updates the interface whenever local actor data changes.
   * <p>
   * If we allow editing of actors we must make sure this handler is added to
   * all actors rather than just the list.
   * <p>
   * i.e. Use {@link ActorManager#addChangeHandler(ManagerChangeHandler, boolean)}
   * with the second argument {@code true}
   */
  public void change(IManager m) {
    try {
      SueComponentManager.getManager().withAllServerData(new PullCallback() {
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
    final SueComponentManager man = SueComponentManager.getManager();
    clearTable();
    Set<String> sues = man.getNames();
    for(String sue : sues) {
      SingleSueComponentManager sMan = man.getSueComponentManager(sue);
      addSueComponentToTable(sMan.getName());
    }
    
    setEnabled(true);
    clearInterface(false);
  }
  
  /**
   * Add a single actor to the table.
   * @param name Name of the actor.
   * @param url URL to download the actor.
   * @param type The type of actor. e.g. "PYTHON"
   */
  private void addSueComponentToTable(final String name) {
    int row = sueTable.getRowCount()-1;
    sueTable.insertRow(row);
    // Add the actor to the table.
    sueTable.setText(row, 0, name);
  }
  
  /**
   * Wipe the table.
   */
  private void clearTable() {
    while(sueTable.getRowCount() > 2) {
      sueTable.removeRow(1);
    }
  }
}