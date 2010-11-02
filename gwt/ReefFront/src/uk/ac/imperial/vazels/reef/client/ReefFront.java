package uk.ac.imperial.vazels.reef.client;

import uk.ac.imperial.vazels.reef.client.settings.SettingsWgt;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ReefFront implements EntryPoint
{
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad()
	{
	  RootPanel.get("settingstest").add(new SettingsWgt());
	}
}
