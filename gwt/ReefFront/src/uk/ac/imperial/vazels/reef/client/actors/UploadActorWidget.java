package uk.ac.imperial.vazels.reef.client.actors;

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
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Widget to display information about current actors on the server, as
 * well to upload new actors.
 */
public class UploadActorWidget extends Composite implements ManagerChangeHandler {
  /**
   * Generated code - gives us an interface to the XML-defined UI.
   */
  private static UploadActorWidgetUiBinder uiBinder = GWT
    .create(UploadActorWidgetUiBinder.class);
  
  /**
   * Generated code. 
   */
  interface UploadActorWidgetUiBinder extends UiBinder<Widget, UploadActorWidget> {
  }
  
  @UiField FormPanel formPanel;
  @UiField TextBox actor_name;
  @UiField FileUpload actor_file;
  @UiField ListBox actor_type;
  @UiField Button submitBtn;
  @UiField FlexTable actorTable;

  public UploadActorWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    formPanel.setAction(new AddressResolution().resolve("/actors"));
    
    actorTable.setText(0, 0, "Name");
    actorTable.setText(0, 1, "File");
    actorTable.setText(0, 2, "Type");
    
    actor_name.removeFromParent();
    actorTable.setWidget(1, 0, actor_name);
    actor_file.removeFromParent();
    actorTable.setWidget(1, 1, actor_file);
    actor_type.removeFromParent();
    actorTable.setWidget(1, 2, actor_type);
    submitBtn.removeFromParent();
    actorTable.setWidget(1, 3, submitBtn);
    
    // to get type of uploaded file, perhaps should have some validation based
    // on the file type
    actor_type.addItem("JAVA");
    actor_type.addItem("PYTHON");
    actor_type.addItem("SUE");
    
    ActorManager.getManager().addChangeHandler(this);
    
    try {
      ActorManager.getManager().getAllServerData();
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
    ActorManager.getManager().actorUploaded(event.getResults().trim());
    
    try {
      ActorManager.getManager().getServerData();
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
    if(validateActorName(actor_name.getText())) {
      formPanel.submit();
    }
  }

  /**
   * Validate the actor name, checking it's non-empty and unique.
   * @param actorName Name to check.
   * @return Whether the name is valid.
   */
  private boolean validateActorName(String actorName) {
    if(ActorManager.getManager().getNames().contains(actorName)) {
      Window.alert("You already have a group named '"+actorName+"'.");
      return false;
    }
    else if(actorName.equals("")) {
      Window.alert("Must give actor a name");
      return false;
    }
    //groups were alphanumeric, is this something enforced for actors?
    return true;
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
      ActorManager.getManager().withAllServerData(new PullCallback() {
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
    final ActorManager man = ActorManager.getManager();
    clearTable();
    Set<String> actors = man.getNames();
    for(String actor : actors) {
      SingleActorManager aMan = man.getActorManager(actor);
      addActorToTable(aMan.getName(), aMan.getDownloadURL(), aMan.getType());
    }
  }
  
  /**
   * Add a single actor to the table.
   * @param name Name of the actor.
   * @param url URL to download the actor.
   * @param type The type of actor. e.g. "PYTHON"
   */
  private void addActorToTable(final String name, final String url, final String type) {
    int row = actorTable.getRowCount()-1;
    actorTable.insertRow(row);
    // Add the group to the table.
    actorTable.setText(row, 0, name);
    
    Button download = new Button("Download");
    download.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Window.open(url, "", "");
      }
    });
    actorTable.setWidget(row, 1, download);
    
    actorTable.setText(row, 2, type);
  }
  
  /**
   * Wipe the table.
   */
  private void clearTable() {
    while(actorTable.getRowCount() > 2) {
      actorTable.removeRow(1);
    }
  }
}