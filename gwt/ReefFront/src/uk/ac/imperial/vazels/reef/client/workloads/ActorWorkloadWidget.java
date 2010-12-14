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
  }
  public void initWidget() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    assignmentTab.add(new Label("Actors: "));

    wkldsBox = new ListBox();
    assignmentTab.add(wkldsBox);
/*
    assignmentTab.add(new Label("Groups: "));

    groupsBox = new ListBox();
/*    ChangeHandler handler = new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        updateAttachedWklds();
      }
    };*/
/*    ClickHandler updateGroupList = new ClickHandler(){
      @Override
      public void onClick(ClickEvent event) {
        updateGroupsBox();
      }
    };
    groupsBox.addClickHandler(updateGroupList);
//    groupsBox.addChangeHandler(handler);
    assignmentTab.add(groupsBox);

    assignmentTab.add(new Label("Currently attached workloads: "));

    attachedWklds = new TextBox();
    attachedWklds.setReadOnly(true);
    assignmentTab.add(attachedWklds);
    updateAttachedWklds();
    for(String wkld: Workloads.returnWorkloads()) {
      wkldsBox.addItem(wkld);
    }

    updateGroupsBox();

    //need groups and workloads info input into box
    //need choose which workloads to attach workload to?
    Button submitWtoG = new Button ("Submit", new ClickHandler() {
      public void onClick(ClickEvent event) {
        addWorkload();
        showSubmission();
      }
    });
    assignmentTab.add(submitWtoG);
*/
  }
}
