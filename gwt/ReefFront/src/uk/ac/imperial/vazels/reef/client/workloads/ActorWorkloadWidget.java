package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.actors.ActorManager;
import uk.ac.imperial.vazels.reef.client.managers.IManager;
import uk.ac.imperial.vazels.reef.client.managers.ManagerChangeHandler;
import uk.ac.imperial.vazels.reef.client.managers.MissingRequesterException;
import uk.ac.imperial.vazels.reef.client.managers.PullCallback;
import uk.ac.imperial.vazels.reef.client.managers.PushCallback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

//future, would like deletion ability, possibly an option within this widget is best

public class ActorWorkloadWidget extends Composite {
  //wkldsBox holds list of workloads
  //actorsBox holds list of actors
  //attachedActors holds list of those actors attached to selected workload in wkldsBox
  ListBox wkldsBox, actorsBox, attachedActors;

  private void setUiElementsEnabled(boolean enabled) {
    wkldsBox.setEnabled(enabled);
    actorsBox.setEnabled(enabled);
    attachedActors.setEnabled(enabled);
  }

  public ActorWorkloadWidget() {
    initWidget();
    updateWkldsBox();
    updateActorBox();
    updateAttachedActorsBox();
  }

  /** 
   * Initialises widget, initialises the 3 ListBox objects and submit button.
   */
  public void initWidget() {
    VerticalPanel assignmentTab = new VerticalPanel();
    initWidget(assignmentTab);

    //for list of workloads
    assignmentTab.add(new Label("Select a workload to manage: "));
    wkldsBox = new ListBox();
    assignmentTab.add(wkldsBox);

    //if expect list of workloads to change during execution of widget, require update on change
    WorkloadManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateWkldsBox();
        updateAttachedActorsBox();
      }
    });

    //currently selected workload in wkldsBox, which changes, determines content of attachedActors list
    wkldsBox.addChangeHandler( new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateActorBox();
        updateAttachedActorsBox();
      }
    });

    //for list of actors
    assignmentTab.add(new Label("Select an actor to assign to the workload: "));
    actorsBox = new ListBox();
    assignmentTab.add(actorsBox);

    //in case list of actors changes during execution of this widget
    ActorManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateActorBox();
      }
    });

    //list of actors attached to selected workload in wkldsBox
    assignmentTab.add(new Label("Actors already assigned to this group: "));
    attachedActors = new ListBox();
    assignmentTab.add(attachedActors);

    //button to submit currently selected actor to currently selected workload
    Button submitButton = new Button ("Assign Actor to Workload", new ClickHandler() {
      public void onClick(ClickEvent event) {
        /*
         * TODO: Why is this all inlined so? Because this Widget has not been
         * properly Xml'd, so I've hacked the functionality I wanted into it.
         * @author James
         */
        setUiElementsEnabled(false);
        final String actorName = actorsBox.getItemText(actorsBox.getSelectedIndex());
        final String workloadName = wkldsBox.getItemText(wkldsBox.getSelectedIndex());
        final WorkloadManager wMan = WorkloadManager.getManager();
        wMan.getWorkloadManager(workloadName).addActor(actorName);
        wMan.pushLocalData(new PushCallback() {
          @Override
          public void failed() {
            Window.alert("Server communication failure: Can't be sure that your last change was properly sent.");
            setUiElementsEnabled(true);
          }
     
          @Override
          public void got() {
            updateAttachedActorsBox();
            updateActorBox();
            setUiElementsEnabled(true);
          }
        });
      }
    });
    assignmentTab.add(submitButton);
  }


  private void updateWkldsBox() {
    final WorkloadManager man = WorkloadManager.getManager();
    try {
      man.withAllServerData(new PullCallback() {
        public void got() {
          updateWkldsBoxNow();
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    } finally {
      updateWkldsBoxNow();
    }
  }

  private void updateWkldsBoxNow() {
    final WorkloadManager man = WorkloadManager.getManager();
    wkldsBox.clear();
    Set<String> wklds = man.getNames();
    for (String w : wklds) {
      wkldsBox.addItem(w);
    }
  }

  private void updateActorBox() {
    final ActorManager actMan = ActorManager.getManager();
    final SingleWorkloadManager wkldMan;
    try {
      wkldMan = WorkloadManager.getManager()
      .getWorkloadManager(wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
      if (wkldMan == null) {
        return;
      }
    } catch (NullPointerException e) {
      return; // Can't assign an actor to a workload if we can't get the workload.
    } catch (IndexOutOfBoundsException e) {
      return; // Box isn't yet initialised
    }
    actorsBox.clear();
    try {
      //get the list of workloads from the server and add them to wkldsBox
      actMan.withServerData(new PullCallback() {
        public void got() {
          Set<String> actors = actMan.getNames();
          for(String a: actors) {
            if (wkldMan == null ||
                wkldMan.getActors() == null ||
                !wkldMan.getActors().contains(a)) {
              actorsBox.addItem(a);
            }
          }
        }
      });
    } catch (MissingRequesterException e) {
      e.printStackTrace();
    }
  }

  //update attachedActors ListBox with the list of attachedActors for the selected workload in wkldsBox
  private void updateAttachedActorsBox() {
    attachedActors.clear();
    //need there to be a workload present to have a workload selected
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
