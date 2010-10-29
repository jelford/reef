package uk.ac.imperial.vazels.reef.client.settings;

import com.google.gwt.core.client.JavaScriptObject;

public class SectionList extends JavaScriptObject
{
	protected SectionList(){}

	public final native String sectionAt(int index)
	/*-{
		return this[index];
	}-*/;
	
	public final native int length()
	/*-{
		return this.length;
	}-*/;
}
