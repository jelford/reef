package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.JsArrayStringSetConverter;
import uk.ac.imperial.vazels.reef.client.managers.ListedCollectionManager;

public class WorkloadManager extends ListedCollectionManager<String, SingleWorkloadManager> {
  private static WorkloadManager manager = null;
  
  private WorkloadManager() {
    setPuller(new WorkloadPuller());
  }
  
  /**
   * Get the singleton instance of this class.
   * @return A global workload manager.
   */
  public static WorkloadManager getManager() {
    if(manager == null) {
      manager = new WorkloadManager();
    }
    return manager;
  }
  
  /**
   * Get the names of all the workloads on the system.
   * @return A set of names.
   */
  public Set<String> getNames() {
    return this.getItems();
  }
  
  /**
   * Get the manager for a particular workload.
   * @param name The name of the workload to grab a manager for.
   * @return A manager for the specified workload.
   */
  public SingleWorkloadManager getWorkloadManager(String name) {
    return getItem(name);
  }

  /**
   * Called to notify the manager that a workload has been uploaded.
   * @param name Of the uploaded workload.
   */
  public void workloadUploaded(String name) {
    this.serverChange(name);
  }
  
  @Override
  protected SingleWorkloadManager createManager(String id, boolean nMan) {
    return new SingleWorkloadManager(id);
  }
  
  private class WorkloadPuller extends MultipleRequester<Set<String>> {
    public WorkloadPuller() {
      super(RequestBuilder.GET, "/workloads/", new JsArrayStringSetConverter());
    }
    
  }
}
