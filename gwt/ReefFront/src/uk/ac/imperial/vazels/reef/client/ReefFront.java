package uk.ac.imperial.vazels.reef.client;


import com.google.gwt.core.client.EntryPoint;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

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
    SimplePanel top = new SimplePanel();
    top.add(new SetupPhasePanel(top));
    RootPanel.get("tabPanel").add(top);
	}
}
