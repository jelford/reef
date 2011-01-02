package uk.ac.imperial.vazels.reef.client.actors;

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
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UploadActorWidget extends Composite {
  ListBox actorList;

  public UploadActorWidget() {
    initPanel();
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
    uploadPanel.add(new Label("Actor file: "));
    uploadPanel.add(actor_file);

    //to give name to uploaded file
    final TextBox actor_name = new TextBox();
    actor_name.setName("actor_name");//for server
    actor_name.setText("");
//maybe create a listener that automatically puts filename as the name
    uploadPanel.add(new Label("Actor name: "));
    uploadPanel.add(actor_name);

    //to get type of uploaded file, perhaps should have some validation based on the file type
    final ListBox actorLanguage = new ListBox();
    actorLanguage.addItem("JAVA");
    actorLanguage.addItem("PYTHON");
    actorLanguage.addItem("SUE");
    uploadPanel.add(new Label("Actor type: "));
    uploadPanel.add(actorLanguage);

    //for server, create TextBox for data from ListBox
    final TextBox actorLang = new TextBox();
    actorLang.setName("actor_type"); 
    uploadPanel.add(actorLang);
    actorLang.setVisible(false);
    //set this TextBox to have ListBox data onSubmit
    formPanel.addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        actorLang.setText(actorLanguage.getValue(actorLanguage.getSelectedIndex()));
      }
    });
    
    //tell manager that actor has been uploaded and add new item to onscreen list of actors
    formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
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
    });

    //actorList to hold a list of all uploaded actors
    uploadPanel.add(new Label("Previously uploaded actors"));
    actorList = new ListBox();
    populateActorList();
    uploadPanel.add(actorList);
    
    //button used when ready to submit all data
    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        if(validateActorName(actor_name.getText())) {
          formPanel.submit();
        }
      }
    });
    uploadPanel.add(button);
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