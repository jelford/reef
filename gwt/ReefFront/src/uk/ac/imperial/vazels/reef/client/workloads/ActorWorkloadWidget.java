package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.actors.ActorManager;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

//future, would like deletion ability

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
    //use other handler to get update of when workloads change
    //handler only required if expect groups to change after widget loads
    wkldsBox.addClickHandler(new ClickHandler(){
      public void onClick(ClickEvent event) {
        updateWkldsBox();
      }
    });

    assignmentTab.add(wkldsBox);

    assignmentTab.add(new Label("Actors: "));
    //handler only required if expect groups to change after widget loads

    actorsBox.addClickHandler(new ClickHandler(){
      public void onClick(ClickEvent event) {
        updateActorBox();
      }
    });
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
    //add selected actor to selected workload and then push this new data to server
    WorkloadManager manager = WorkloadManager.getManager();
    SingleWorkloadManager wkldManager = manager.getWorkloadManager(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
    wkldManager.addActor(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
    try {
      wkldManager.pushLocalData(new PushCallback() {
        //show submission occurred
        public void got() {
          updateAttachedActorsBox(); 
        }

        public void failed() {
          Window.alert("Failure: no confirmation from server that actor assignment succeeded");
        }        
      });
    }
    catch(MissingRequesterException e) {
      e.printStackTrace();
    }
  }
  void updateWkldsBox() {
    final WorkloadManager man = WorkloadManager.getManager();
    wkldsBox.clear();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      man.withServerData(new PullCallback() {
        public void got() {
          Set<String> workloads = man.getNames(); //returns Set<String>
          for(int i = 0; i < wkldsBox.getItemCount() ; i++) {
            if(workloads.contains(wkldsBox.getItemText(i))) {
              workloads.remove(wkldsBox.getItemText(i));
            }
            else {
              wkldsBox.removeItem(i);
            }
          }
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
         /* Set<String> actors = man.getNames();
          for(String actor: actors) {
            wkldsBox.addItem(actor);
          }
        }*/
            Set<String> actors = man.getNames(); //returns Set<String>
            for(int i = 0; i < actorsBox.getItemCount() ; i++) {
              if(actors.contains(actorsBox.getItemText(i))) {
                actors.remove(actorsBox.getItemText(i));
              }
              else {
                actorsBox.removeItem(i);
              }
            }
            for(String a: actors) {
              actorsBox.addItem(a);
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
