package uk.ac.imperial.vazels.reef.client.settings.overlay;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsSettingGroup extends JavaScriptObject
{
	protected JsSettingGroup(){}
	
	public final native JsArrayString getKeys()
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
		return !(this[name] === undefined);
	}-*/;
	
	public final native String getStringSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native double getDoubleSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native int getIntSetting(String name)
	/*-{
		return this[name];
	}-*/;
	
	public final native String getType(String name)
	/*-{
		return typeof this[name];
	}-*/;
}
