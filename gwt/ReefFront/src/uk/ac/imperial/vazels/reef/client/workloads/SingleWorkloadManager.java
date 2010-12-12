package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;

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
    // TODO If the group returned has a different name we break things currently
    wkld = data;
    return true;
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