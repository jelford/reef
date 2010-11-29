package uk.ac.imperial.vazels.reef.client.managers;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.JsArrayString;

import uk.ac.imperial.vazels.reef.client.MultipleRequester.Converter;

/**
 * Converts a Javascript array of strings to a set of strings.
 */
public class JsArrayStringSetConverter implements Converter<Set<String>>{
  @Override
  public Set<String> convert(String original) {
    Set<String> set = new HashSet<String>();
    if(original != null) {
      JsArrayString array = parseJson(original);
      for(int i=0; i<array.length(); i++) {
        set.add(array.get(i));
      }
    }
    return set;
  }
  
  public final native JsArrayString parseJson(String json) /*-{
    return eval(json);
  }-*/;
}
