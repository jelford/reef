package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
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
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

//future, would like deletion ability, possibly an option within this widget is best

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
    assignmentTab.add(wkldsBox);
    WorkloadManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateWkldsBox();
      }
    });
    wkldsBox.addChangeHandler( new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateAttachedActorsBox();        
      }
    });

    assignmentTab.add(new Label("Actors: "));
    actorsBox = new ListBox();
    assignmentTab.add(actorsBox);
    ActorManager.getManager().addChangeHandler(new ManagerChangeHandler() {
      @Override
      public void change(IManager man) {
        updateActorBox();
      }
    });
    
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

  protected class ActorAssignment extends MultipleRequester<Workload> {
    public ActorAssignment() {
      super(RequestBuilder.POST, "/actorassign/", null);
    }
    protected QueryArg[] getArgs() {
      QueryArg[] args = new QueryArg[3]; //do -add/rem, actorName, workloadName
      args[0] = new QueryArg("do", "add"); //TODO allow for "rem"
      args[1] = new QueryArg("workload", wkldsBox.getItemText(wkldsBox.getSelectedIndex()));
      args[2] = new QueryArg("actor", actorsBox.getItemText(actorsBox.getSelectedIndex()));
      return args;
    }
  }
  void assignActor() {
    String wkldAssignTo = wkldsBox.getItemText(wkldsBox.getSelectedIndex());
    String actorToAssign = actorsBox.getItemText(actorsBox.getSelectedIndex());
    
    //this line does all the assigning to the server
    new ActorAssignment().go(null);
    
    //add selected actor to selected workload and then push this new data to server
    WorkloadManager manager = WorkloadManager.getManager();
    SingleWorkloadManager wkldManager = manager.getWorkloadManager(wkldAssignTo);
    wkldManager.addActor(actorToAssign);
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