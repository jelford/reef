package uk.ac.imperial.vazels.reef.client.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.JsArrayInteger;

/**
 * An overlay for a time series. This is part of the result when you want output data from an experiment.
 */
public class TimeSeries {
  private Map<Integer, SnapshotData> snapshots;
  
  /**
   * This is expected to be called from {@link HostData}.
   * @param data Data to initialise with.
   */
  TimeSeries(TimeSeriesOverlay data) {
    snapshots = new HashMap<Integer, SnapshotData>();
    JsArrayInteger stamps = data.timeStamps();
    for(int i=0; i<stamps.length(); i++) {
      final Integer stamp = stamps.get(i);
      snapshots.put(stamp, new SnapshotData(data.get(stamp)));
    }
  }
  
  /**
   * Get a list of recorded timestamps.
   * @return An set of timestamps.
   */
  public Set<Integer> stamps() {
    return snapshots.keySet();
  }
  
  /**
   * Get the data at a particular timestamp.
   * @param stamp The timestamp to check.
   * @return A snapshot from a particular point in time.
   */
  public SnapshotData snapshot(Integer stamp) {
    return snapshots.get(stamp);
  }
}
