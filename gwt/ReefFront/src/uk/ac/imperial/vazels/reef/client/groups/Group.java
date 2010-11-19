package uk.ac.imperial.vazels.reef.client.groups;

import java.util.ArrayList;
import java.util.List;

public class Group {
  public List<String> workloads = new ArrayList<String>();
  public List<String> filters = new ArrayList<String>();
  public final String name;
  public final int size;
  
  public Group(final String name, final int size) {
    this.name = name;
    this.size = size;
  }
}
