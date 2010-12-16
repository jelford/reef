package uk.ac.imperial.vazels.reef.client;


import uk.ac.imperial.vazels.reef.client.ui.MainReefPanel;
import uk.ac.imperial.vazels.reef.client.ui.SetupPhasePanel;

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
    MainReefPanel main = new MainReefPanel();
    main.setContent("Experiment Setup", new SetupPhasePanel(main));
    RootPanel.get("tabPanel").add(main);
	}
}
