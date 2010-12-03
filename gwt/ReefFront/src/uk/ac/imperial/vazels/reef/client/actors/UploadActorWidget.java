package uk.ac.imperial.vazels.reef.client.actors;

//import java.awt.TextField;

import uk.ac.imperial.vazels.reef.client.AddressResolution;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UploadActorWidget extends Composite {

//  private ActorSummary actors;

  public UploadActorWidget() {
    initPanel();
//    refresh();
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
    uploadPanel.add(actor_file);
    //validation here

    TextBox actor_name = new TextBox();
    actor_name.setName("actor_name");//for server
    actor_name.setText("");
//maybe create a listener that automatically puts filename as the name
    uploadPanel.add(actor_name);

    final ListBox actorLanguage = new ListBox();
    actorLanguage.addItem("JAVA");
    actorLanguage.addItem("PYTHON");
    actorLanguage.addItem("SUE");
    uploadPanel.add(actorLanguage);
    
    final TextBox actorLang = new TextBox(); //for server
    actorLang.setName("actor_type"); 
    uploadPanel.add(actorLang);
    actorLang.setVisible(false);

    formPanel.addSubmitHandler(new SubmitHandler() {
      public void onSubmit(SubmitEvent event) {
        actorLang.setText(actorLanguage.getValue(actorLanguage.getSelectedIndex()));
        Actors.add(actorLang.getText());
      }
    });    
    
    Button button = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        formPanel.submit();
      }
    });
    uploadPanel.add(button);
  }
}