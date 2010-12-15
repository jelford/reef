package uk.ac.imperial.vazels.reef.client.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Describes the level beneath root when requesting output data from an experiment.
 * This holds a list of hosts with their variables.
 */
public class GroupData {
  private Map<String, HostData> hosts;
  
  /**
   * Expected to be called by {@link OutputData}.
   * @param groupData The data to initialise with.
   */
  GroupData(GroupDataOverlay groupData) {
    hosts = new HashMap<String, HostData>();
    JsArrayString keys = groupData.keys();
    for(int i=0; i<keys.length(); i++) {
      final String key = keys.get(i);
      hosts.put(key, new HostData(groupData.get(key)));
    }
  }
  
  /**
   * Get a list of host ids in this group.
   * @return List of host ids.
   */
  public Set<String> hostIds() {
    return hosts.keySet();
  }
  
  /**
   * Get info on a specific host.
   * @param host The host to get info on.
   * @return The info for the specified host.
   */
  public HostData hostInfo(String host) {
    return hosts.get(host);
  }
}
