package uk.ac.imperial.vazels.reef.client.groups;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

/**
 * A data class containing information on groups. Basically just a
 * map which will take a JSON object constructor. Protects you from
 * having to deal with JSobjects directly.
 * @author james
 *
 */
public class GroupSummary implements Iterable<String>{ 
  /**
   * We'll store a map of the groups
   */
  private Set<String> groups;
  
  /**
   * If no argument is given, will assume an empty list of groups.
   */
  public GroupSummary() {
    this(null);
  }
  
  /**
   * Given a JSON input, will construct the map
   * for you. {@code null} will create an empty summary.
   * @param jsonInput
   */
  public GroupSummary(String jsonInput) {
    groups = new HashSet<String>();
    if(jsonInput != null) {
      JsArrayString names = parseArray(jsonInput);
      for(int i=0; i<names.length(); i++)
        groups.add(names.get(i));
    }
  }
  
  protected native JsArrayString parseArray(String json) /*-{
    return eval(json);
  }-*/;
  
  /**
   * Get the number of groups.
   * @return number of groups.
   */
  public int length() {
    return groups.size();
  }

  /**
   * Get an iterator, DO NOT REMOVE!
   * @return an iterator...
   */
  @Override
  public Iterator<String> iterator() {
    return groups.iterator();
  }
}
