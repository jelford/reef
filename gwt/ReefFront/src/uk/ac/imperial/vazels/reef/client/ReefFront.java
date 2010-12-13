package uk.ac.imperial.vazels.reef.client;


import com.google.gwt.core.client.EntryPoint;

import com.google.gwt.user.client.ui.RootPanel;

import uk.ac.imperial.vazels.reef.client.ReefTabPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ReefFront implements EntryPoint
{
	/**
	 * This is the entry point method.
	 */
	
  static String initialHTML;
  
  public void onModuleLoad()
	{
    initialHTML = RootPanel.get().getBodyElement().getInnerHTML();
     
	  //RootPanel.get("settingstest").add(new SettingsWgt());
	 // RootPanel.get("specify-groups").add(new AllocateGroups());
	 // RootPanel.get("server-control").add(new ServerControl());
	 // RootPanel.get("workloads").add(new WorkloadWidget());
	 // RootPanel.get("actors").add(new UploadActorWidget());

     
    
    RootPanel.get("tabPanel").add(new ReefTabPanel());


	}
}
