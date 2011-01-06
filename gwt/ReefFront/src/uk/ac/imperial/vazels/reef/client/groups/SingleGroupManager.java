package uk.ac.imperial.vazels.reef.client.groups;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.DeletableManager;
import uk.ac.imperial.vazels.reef.client.managers.SingleTypeManager;

/**
 * Manages a group object, deals with syncing to the server.
 */
public class SingleGroupManager extends SingleTypeManager<Group> implements DeletableManager {
  private Group group;
  
  /**
   * This should only ever be used inside group manager.
   * We do not ever want to manually create an instance of this class.
   * @param name Group that this manager controls.
   * @param nGroup Is this a manager for a new group or an already created one?
   */
  SingleGroupManager(String name, boolean nGroup) {
    super(nGroup);
    setPuller(new GroupRequest(name));
    setPusher(new GroupUpdate(name));
    this.group = new Group(name, 0);
  }
  
  // Getters/Setters
  
  public void requestDeletion() {
    setSize(0);
  }
  
  public boolean pendingDelete() {
    return getSize() == 0;
  }
  
  /**
   * Get the size of this group
   * @return group size
   */
  public int getSize() {
    return group.getSize();
  }

  /**
   * Set this size of this group
   * @param size group size
   */
  public void setSize(int size) {
    change();
    group.setSize(size);
  }

  /**
   * Get the name of this group.
   * @return group name
   */
  public String getName() {
    return group.getName();
  }
  
  /**
   * Add workload to the group.
   * @param wkld The name of the workload to add
   * @return {@code true} if the workload was newly added
   */
  public boolean addWorkload(String wkld) {
    change();
    return group.addWorkload(wkld);
  }
  
  /**
   * Remove workload from a group.
   * @param wkld The name of the workload to remove
   * @return {@code true} if the workload used to be attached to this group.
   */
  public boolean remWorkload(String wkld) {
    change();
    return group.remWorkload(wkld);
  }
  
  /**
   * Returns an editable array of workloads for this group.
   * @return array of workloads.
   */
  public String[] getWorkloads() {
    return group.getWorkloads();
  }
  
  /**
   * Add SueComponent to the group.
   * @param sc The name of the workload to add
   * @return {@code true} iff the SueComponent is newly added
   */
  public boolean addSueComponent(String sc) {
    change();
    return group.addSueComponent(sc);
  }
  
  /**
   * Remove SueComponent from the group
   * @param sc The name of the SueComponent to remove
   * @return {@code true} iff the SueComponent used to be attached to this group.
   */
  public boolean remSueComponent(String sc) {
    change();
    return group.remSueComponent(sc);
  }
  
  /**
   * Get a list SueComponents attached to this group.
   * @return A list of the names of the SueComponents attached to the group.
   */
  public String[] getSueComponents() {
    return group.getSueComponents();
  }
  
  /**
   * Get the list of fully connected hosts in this group
   */
  public String[] getConnectedHosts() {
    return group.getConnectedHosts();
  }
  
  /**
   * Get a list of evolving host names in the group
   */
  public String[] getEvolvingHosts() {
    return group.getEvolvingHosts();
  }
  
  // Data processing
  
  @Override
  protected boolean receiveData(Group data) {
    // TODO If the group returned has a different name we break things currently
    group = data;
    return true;
  }
  
  // Requests
  
  protected class GroupRequest extends MultipleRequester<Group> {
    public GroupRequest(String ext) {
      super(RequestBuilder.GET, "/groups/"+ext, new Converter<Group>() {
        @Override
        public Group convert(String original) {
          return new Group(original);
        }
      });
    }
  }
  
  protected class GroupUpdate extends MultipleRequester<Group> {
    public GroupUpdate(String ext) {
      super(RequestBuilder.POST, "/groups/"+ext, new Converter<Group>() {
        @Override
        public Group convert(String original) {
          return new Group(original);
        }
      });
    }

    @Override
    protected QueryArg[] getArgs() {
      final String [] workloads = getWorkloads();
      final String [] sueComponents = getSueComponents();
      QueryArg[] args = new QueryArg[1+workloads.length+sueComponents.length];
      
      /*
       *  Wow, we're really going to hand-build an array by keeping an index
       *  count?
       *  @TODO: Refactor this into a List implementation.
       */
      args[0] = new QueryArg("size", Integer.toString(getSize()));
      int index = 1;
      for(String wkld : workloads) {
        args[index] = new QueryArg("workloads", wkld);
        index++;
      }
      for (String sueComp : sueComponents) {
        args[index] = new QueryArg("sue_components", sueComp);
        index++;
      }
      return args;
    }
  }
}
