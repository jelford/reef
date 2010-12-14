package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.actors.ActorManager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;

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
    final WorkloadManager man = WorkloadManager.getManager();
    wkldsBox.clear();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> workloads = man.getNames();
          for(String wkld: workloads) {
            wkldsBox.addItem(wkld);
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  
  private void updateActorBox() {
    final ActorManager man = ActorManager.getManager();
    actorsBox.clear();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> actors = man.getNames();
          for(String actor: actors) {
            wkldsBox.addItem(actor);
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }

  private void updateAttachedActorsBox() {
    attachedActors.clear();
    //need there to be a group present to have a group selected
    if(wkldsBox.getItemCount() > 0) {
      WorkloadManager manager = WorkloadManager.getManager();
      final SingleWorkloadManager singleWkldManager = manager.getWorkloadManager(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
      //get workload data from server, and on callback, use the data on actors
      try {
        singleWkldManager.withServerData(new PullCallback() {
          public void got() {
            Set <String> theAssignedActors = singleWkldManager.getActors();
            for(String actor: theAssignedActors) {
              attachedActors.addItem(actor);
            }            
          }
        });
      }
      catch (MissingRequesterException e) {
        e.printStackTrace();
      }
    }
  }
}