package uk.ac.imperial.vazels.reef.client.groups;

import java.util.Set;

public class Manager {

  void syncIMMEDIATE () {
    
  }
  
  //hand a class with a callback function
  void syncCallBack(Bob b) {
    
  }
  
  public void pushToServer(Bob b) {
    
  }
  
  public Manager getGroup(String GROUPNAME) {
    return null;
  }
  
  //Manager.getGroup()
  //gets manager for single group, which will have add workload
  //then pushToServer - pushes data for current group (ie manager)
  //
  
  interface Bob {
    public void go();
  }

  public static Manager getManager() {
    // TODO Auto-generated method stub
    return null;
  }

  public void addWorkload(String itemText) {
    // TODO Auto-generated method stub
    
  }

  public String [] getWorkloads() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<String> getNames() {
    // TODO Auto-generated method stub
    return null;
  }
}
