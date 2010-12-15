package uk.ac.imperial.vazels.reef.client;


import com.google.gwt.core.client.EntryPoint;

import com.google.gwt.user.client.ui.RootPanel;

import uk.ac.imperial.vazels.reef.client.ReefTabPanel;

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
    RootPanel.get("tabPanel").add(new ReefTabPanel());
	}
}
