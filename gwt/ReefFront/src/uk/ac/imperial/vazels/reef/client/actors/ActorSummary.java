package uk.ac.imperial.vazels.reef.client.actors;

import java.util.ArrayList;

import com.google.gwt.core.client.JsArrayString;
import uk.ac.imperial.vazels.reef.client.workloads.WorkloadSummaryOverlay;

public class ActorSummary {
  private ArrayList<String> workloads;

//  onModuleLoad()
  
  public ActorSummary () {
    initialise("{}");
  }
  public ActorSummary (String jsonInput) {
    initialise(jsonInput);
  }
  private void initialise (String jsonInput) {
    WorkloadSummaryOverlay thisObject = parseJSON(jsonInput);
    workloads = new ArrayList<String> ();

    final JsArrayString wkldNames = thisObject.getArray();
    final int keysLength = wkldNames.length();
    for (int i=0; i < keysLength; i++) {
      workloads.add(wkldNames.get(i));
    }
  }
  private native final WorkloadSummaryOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;
  public int size() {
    return workloads.size();
  }
  public String get(int i) {
    return workloads.get(i);
  }
}
/**
 * A data class containing information on groups. Basically just a
 * map which will take a JSON object constructor. Protects you from
 * having to deal with JSobjects directly.
 * @author james
 *
 */
/*  public class GroupSummary { 
    /**
 * We'll store a map of the groups
 *
    private Map<String,Integer> groups;

    /**
 * If no argument is given, will assume an empty list of groups.
 *
    public GroupSummary() {
      /* Initialize with an empty JSON string *
      initialize("{}");
    }

    /**
 * Given a JSON input, will construct the map
 * for you.
 * @param jsonInput

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
    }-*;



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



  }*/