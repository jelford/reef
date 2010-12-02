package uk.ac.imperial.vazels.reef.client.workloads;

import java.util.LinkedList;

public class WorkloadsManager {
  private WorkloadsManager(){
    
  }
  private static LinkedList <Workload> workloads = new LinkedList<Workload>();

  public static Workload get(final String workloadName) {
    Workload toReturn = null;
    for(Workload wkld: workloads) {
      if(wkld.name.equals(workloadName)) {
        toReturn = wkld;
        break; 
      }
    }
    return toReturn;
  }

  public static void put(Workload wkld) {
    workloads.add(wkld);
  }
  
  public static LinkedList<String> getWorkloadNames() {
    LinkedList<String> wkldNames = new LinkedList<String> ();
    for(Workload wkld: workloads) {
      wkldNames.add(wkld.name);
    }
    return wkldNames;
  }
}