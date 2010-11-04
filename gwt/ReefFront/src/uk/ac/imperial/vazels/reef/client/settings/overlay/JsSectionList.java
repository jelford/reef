package uk.ac.imperial.vazels.reef.client.settings.overlay;

import com.google.gwt.core.client.JavaScriptObject;

public class JsSectionList extends JavaScriptObject
{
	protected JsSectionList(){}

	public final native String sectionAt(int index)
	/*-{
		return this[index];
	}-*/;
	
	public final native int length()
	/*-{
		return this.length;
	}-*/;
}
