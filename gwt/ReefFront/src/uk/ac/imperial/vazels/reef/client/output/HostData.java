package uk.ac.imperial.vazels.reef.client.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * Holds data on a specific host after output is requested from an experiment.
 */
public class HostData {
  private Map<String, TimeSeries> variables;
  
  /**
   * This is expected to be called by {@link GroupData}.
   * @param data The data to initialise with.
   */
  HostData(HostDataOverlay data) {
    variables = new HashMap<String, TimeSeries>();
    JsArrayString keys = data.keys();
    for(int i=0;i<keys.length();i++) {
      final String key = keys.get(i);
      variables.put(key, new TimeSeries(data.get(key)));
    }
  }
  
  /**
   * Get a list of the variables this host has dumped.
   * @return A list of variables.
   */
  public Set<String> variableNames() {
    return variables.keySet();
  }
  
  /**
   * Get the time series for a specific variable.
   * @param variable The variable to get the series for.
   * @return A time series.
   */
  public TimeSeries variableSeries(String variable) {
    return variables.get(variable);
  }
}
