package uk.ac.imperial.vazels.reef.client.settings;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class SettingGroup extends JavaScriptObject
{
	protected SettingGroup(){}
	
	public final String[] getKeys()
	{
		JsArrayString keys = getJsKeys();
		String[] sKeys = new String[keys.length()];
		for(int i=0; i<keys.length(); i++)
			sKeys[i] = keys.get(i);
		return sKeys;
	}
	
	private final native JsArrayString getJsKeys()
	/*-{
		var keys = []
		for(key in this)
		{
			keys.push(key);
		}
		return keys;
	}-*/;
	
	public final native boolean hasSetting(String name)
	/*-{
		return (this[name] === undefined);
	}-*/;
	
	public final native String getStringSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native Double getDoubleSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native Integer getIntSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native String getType(String name)
	/*-{
		return typeof this[name];
	}-*/;
	
	public final Object getSetting(String name)
	{
		String type = getType(name);
		if(type == "string")
			return getStringSetting(name);
		else if(type == "number")
		{
			Integer i = getIntSetting(name);
			Double d = getDoubleSetting(name);
			return (Math.round(d) == d) ? i : d;
		}
		else
			return null;
	}
}
