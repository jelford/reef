package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Manages a group object, deals with syncing to the server.
 */
public class SingleWorkloadManager extends Manager<Workload, Void> {
  private Workload wkld;
  
  /**
   * This should only ever be used inside workload manager.
   * We do not ever want to manually create an instance of this class.
   * <p>
   * This cannot edit a workload, that needs to be done with forms.
   * @param name Workload that this manager controls.
   */
  SingleWorkloadManager(String name) {
    super(false);
    setPuller(new WorkloadRequest(name));
    this.wkld = new Workload(name);
  }

  /**
   * Get the name of this group.
   * @return group name
   */
  public String getName() {
    return wkld.getName();
  }
  
  /**
   * Get the URL to download the workload file from.
   * @return A URL pointing to the file to download.
   */
  public String getDownloadURL() {
    return new AddressResolution().resolve("/workloads/"+ wkld.getName() + ".wkld");
  }
  
  public Set<String> getActors() {
    return wkld.getActors();
  }
  
  // Data processing
  
  @Override
  protected boolean receivePullData(Workload data) {
    wkld = data;
    return true;
  }
  
  /**
   * Add actor to the workload.
   * @param actor The name of the actor to add
   * @return {@code true} if the actor was newly added
   */
  public boolean addActor(final String actor) {
    
    //    change(); change not good enough atm possibly adaptable for better code - need to use different handler
    /*
     * @TODO: Change the backend to work like with groups!
     * Today, the way workloads are managed with actors is very
     * different indeed to the way Groups are managed. This means
     * we need to send calls to the server every time we add
     * or remove an actor, and we can't do it in a bulk fashion.
     * I have inlined anonymous handlers to do this job, because I
     * don't have time right now to re-work the entire backend for
     * actor assignment.
     */
    new MultipleRequester<Void>(RequestBuilder.POST, "/actorassign/", null) {
      @Override
      protected QueryArg[] getArgs() {
        QueryArg[] args = new QueryArg[3];
        args[0] = new QueryArg("do", "add");
        args[1] = new QueryArg("workload", getName());
        args[2] = new QueryArg("actor", actor);
        return args;
      }
    }.go(null);
    serverChange();
    return wkld.addActor(actor);
  }
  /**
   * Add actor to the workload.
   * @param actor The name of the actor to add
   * @return {@code true} if the actor was newly added
   */
  public boolean remActor(final String actor) {
//    change();
    /*
     * @TODO: Change the backend to work like with groups!
     * Today, the way workloads are managed with actors is very
     * different indeed to the way Groups are managed. This means
     * we need to send calls to the server every time we add
     * or remove an actor, and we can't do it in a bulk fashion.
     * I have inlined anonymous handlers to do this job, because I
     * don't have time right now to re-work the entire backend for
     * actor assignment.
     */
    new MultipleRequester<Void>(RequestBuilder.POST, "/actorassign/", null) {
      @Override
      protected QueryArg[] getArgs() {
        QueryArg[] args = new QueryArg[3];
        args[0] = new QueryArg("do", "rem");
        args[1] = new QueryArg("workload", getName());
        args[2] = new QueryArg("actor", actor);
        return args;
      }
    }.go(null);
    serverChange();
    return wkld.remActor(actor);
  }
  
  
  @Override
  protected boolean receivePushData(Void data) {
    // Unused
    return false;
  }
  
  // Requests
  
  protected class WorkloadRequest extends MultipleRequester<Workload> {
    public WorkloadRequest(String ext) {
      super(RequestBuilder.GET, "/workloads/"+ext, new Converter<Workload>() {
        @Override
        public Workload convert(String original) {
          return new Workload(original);
        }
      });
    }
  }
}