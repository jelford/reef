package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * A data class containing information on groups. Basically just a
 * map which will take a JSON object constructor. Protects you from
 * having to deal with JSobjects directly.
 * @author james
 *
 */
public class GroupSummary { 
  /**
   * We'll store a map of the groups
   */
  private Map<String,Integer> groups;
  
  /**
   * If no argument is given, will assume an empty list of groups.
   */
  public GroupSummary() {
    /* Initialize with an empty JSON string */
    initialize("{}");
  }
  
  /**
   * Given a JSON input, will construct the map
   * for you.
   * @param jsonInput
   */
  public GroupSummary(String jsonInput) {
    initialize(jsonInput);
  }
  
  private void initialize(String jsonInput) {
    GroupSummaryOverlay thisObject = parseJSON(jsonInput);
    groups = new HashMap<String,Integer>();
    
    final JsArrayString keys = thisObject.keys();
    final int keysLength = keys.length();
    for (int i=0; i<keysLength; i++) {
      String key = keys.get(i);
      groups.put(key, new Integer(thisObject.lookup(key)));
    }
  }
  
  private native final GroupSummaryOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;
  
  public int get(final String key) {
    return groups.get(key);
  }
  
  public Set<String> keySet() {
    return groups.keySet();
  }
  
  public Integer put(String newGroupName, Integer newGroupSize) {
    return groups.put(newGroupName, newGroupSize);
  }
  
  public Integer remove(String groupToRemove) {
    return groups.remove(groupToRemove);
  }

  public boolean contains(String newGroupName) {
    return groups.containsKey(newGroupName);
  }
  
  public int size() {
    int numberOfEmptyGroups=0;
    for (String key : groups.keySet()) {
      if (groups.get(key) == 0) {
        numberOfEmptyGroups++;
      }
    }
    return groups.size() - numberOfEmptyGroups;
  }
  
  
}
