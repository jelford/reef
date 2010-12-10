package uk.ac.imperial.vazels.reef.client;

import uk.ac.imperial.vazels.reef.client.actors.UploadActorWidget;
import uk.ac.imperial.vazels.reef.client.groups.AllocateGroups;
import uk.ac.imperial.vazels.reef.client.servercontrol.ServerControl;
import uk.ac.imperial.vazels.reef.client.workloads.WorkloadWidget;

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
	
  public void onModuleLoad()
	{
	  //RootPanel.get("settingstest").add(new SettingsWgt());
	 // RootPanel.get("specify-groups").add(new AllocateGroups());
	 // RootPanel.get("server-control").add(new ServerControl());
	 // RootPanel.get("workloads").add(new WorkloadWidget());
	 // RootPanel.get("actors").add(new UploadActorWidget());
	  

     RootPanel.get("tabPanel").add(new ReefTabPanel());


	}
}
