package uk.ac.imperial.vazels.reef.client.workloads;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ActorWorkloadWidget extends Composite {
  ListBox wkldsBox, actorsBox, attachedActors;

  public ActorWorkloadWidget() {
    initWidget();
    updateWkldsBox();
    updateActorBox();
    updateAttachedActorsBox();
  }
  public void initWidget() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    assignmentTab.add(new Label("Workloads: "));
    wkldsBox = new ListBox();
    //use manager handler to get update of when workloads change
    assignmentTab.add(wkldsBox);

    assignmentTab.add(new Label("Actors: "));
    actorsBox = new ListBox();
    assignmentTab.add(actorsBox);

<<
    assignmentTab.add(new Label("Already assigned: "));
    attachedActors = new ListBox();
    assignmentTab.add(attachedActors);

    //using local workloads
    //for(String wkld: Workloads.returnWorkloads()) {
    //wkldsBox.addItem(wkld);
    //}

    Button submitButton = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        assignActor();
      }
    });
    assignmentTab.add(submitButton);
  }
  void assignActor() {

  }
  void updateWkldsBox() {

  }
  private void updateActorBox() {
    // TODO Auto-generated method stub
  }
  
  private void updateAttachedActorsBox() {
    // TODO Auto-generated method stub

  }
}