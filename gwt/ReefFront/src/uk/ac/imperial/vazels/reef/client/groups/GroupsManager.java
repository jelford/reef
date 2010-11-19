package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashMap;
import java.util.Map;

public class GroupsManager {
  private GroupsManager(){
    
  }
  
  private static Map<String, Group> groups = new HashMap<String, Group>();
  
  public static Group get(final String groupName) {
    return groups.get(groupName);
  }
  
  public static Group put(final String groupName, Group group) {
    return groups.put(groupName, group);
  }
}
