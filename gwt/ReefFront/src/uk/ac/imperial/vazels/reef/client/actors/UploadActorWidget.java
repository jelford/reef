package uk.ac.imperial.vazels.reef.client.actors;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.workloads.WorkloadManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
    //validation here

    TextBox actor_name = new TextBox();
    actor_name.setName("actor_name");//for server
    actor_name.setText("");
//maybe create a listener that automatically puts filename as the name
    uploadPanel.add(new Label("Actor name: "));
    uploadPanel.add(actor_name);

    final ListBox actorLanguage = new ListBox();
    actorLanguage.addItem("JAVA");
    actorLanguage.addItem("PYTHON");
    actorLanguage.addItem("SUE");
    uploadPanel.add(new Label("Actor type: "));
    uploadPanel.add(actorLanguage);
    
    final TextBox actorLang = new TextBox(); //for server
    actorLang.setName("actor_type"); 
    uploadPanel.add(actorLang);
    actorLang.setVisible(false);

    formPanel.addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        actorLang.setText(actorLanguage.getValue(actorLanguage.getSelectedIndex()));
      }
    });
    
    formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {
      public void onSubmitComplete(SubmitCompleteEvent event) {
        ActorManager.getManager().actorUploaded(event.getResults());
        try {
          ActorManager.getManager().getServerData();
          actorList.addItem(actorLang.getText());
        } catch (MissingRequesterException e) {
          e.printStackTrace();
        }
      }
    });

    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
      }
    });
    uploadPanel.add(button);

    actorList = new ListBox();
    populateActorList();
    uploadPanel.add(new Label("Previously uploaded actors"));
    uploadPanel.add(actorList);
  }
  
  private void populateActorList() {
    final ActorManager man = ActorManager.getManager();
    try {
      //get the list of actors from the server and add them to actorList
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> actors = man.getNames(); //returns Set<String>
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