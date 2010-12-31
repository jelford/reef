package uk.ac.imperial.vazels.reef.client;

import uk.ac.imperial.vazels.reef.client.servercontrol.StateDisplay;
import uk.ac.imperial.vazels.reef.client.ui.MainReefPanel;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main entry point for the reef application.
 */
public class ReefFront implements EntryPoint
{
	/**
	 * Starting point.
	 */
  public void onModuleLoad()
	{
    RootPanel.get("tabPanel").add(MainReefPanel.getInstance());
    RootPanel.get("stateDisplay").add(new StateDisplay());
	}
}