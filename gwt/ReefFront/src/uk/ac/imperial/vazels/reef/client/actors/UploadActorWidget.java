package uk.ac.imperial.vazels.reef.client.actors;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class UploadActorWidget extends Composite {
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
  @UiField ListBox actorLanguage;
  @UiField TextBox actor_name;
  @UiField ListBox actorList;

  public UploadActorWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    formPanel.setAction(new AddressResolution().resolve("/actors"));
    
    // to get type of uploaded file, perhaps should have some validation based
    // on the file type
    actorLanguage.addItem("JAVA");
    actorLanguage.addItem("PYTHON");
    actorLanguage.addItem("SUE");
    
    //maybe create a listener that automatically puts filename as the name (actor_name)
    
    populateActorList();
  }
  
  //tell manager that actor has been uploaded and add new item to onscreen list of actors
  @UiHandler("formPanel")
  public void onSubmitComplete(SubmitCompleteEvent event) {
    ActorManager.getManager().actorUploaded(event.getResults());
    try {
      ActorManager.getManager().getServerData();
      actorList.addItem(actor_name.getText());
      actor_name.setText("");
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  //button used when ready to submit all data
  @UiHandler("button")
  public void onClick(ClickEvent event) {
    if(validateActorName(actor_name.getText())) {
      formPanel.submit();
    }
  }

  //validate name of actor, checking non-empty and unique
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
  
  //used for initial pulling of currently existing actors from the server
  private void populateActorList() {
    final ActorManager man = ActorManager.getManager();
    try {
      //get the list of actors from the server and add them to actorList
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> actors = man.getNames();
          for(String actor : actors) {
            actorList.addItem(actor);
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
}