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
      QueryArg[] args = new QueryArg[1+getWorkloads().length];
      args[0] = new QueryArg("size", Integer.toString(getSize()));
      int index = 1;
      for(String wkld : getWorkloads()) {
        args[index] = new QueryArg("workloads", wkld);
        index++;
      }
      return args;
    }
  }

  public String getRestrictions() {
    return group.getRestrictions();
  }

  public void setRestrictions(final String restrictions) {
    group.setRestrictions(restrictions);
  }
}
