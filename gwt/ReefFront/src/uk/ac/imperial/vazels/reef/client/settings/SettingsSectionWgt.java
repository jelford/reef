package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.EasyRequest;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SettingsSectionWgt extends VerticalPanel
{
	private final String section;
	protected Map<String, SingleSettingWgt> settingWgts;
	protected DisclosurePanel errMsg;
	
	public SettingsSectionWgt(String section)
  {
		this.section = section;
		errMsg = new DisclosurePanel("Could not check settings...");
		errMsg.setVisible(false);
		
		add(errMsg);
		settingWgts = new HashMap<String, SingleSettingWgt>();
  }
	
	public String getSection()
	{
		return section;
	}
	
	public void refreshFields()
	{
		EasyRequest request = new EasyRequest()
		{
			@Override
			protected void requested(Integer code, String reason, String content)
			{
				// Constant first otherwise JS translation breaks everything
				if(code != null && code == 200)
				{
						SettingGroup settings = getSettings(content);
						refreshFields(settings);
				}
				else
				{
					errMsg.clear();
					errMsg.add(new Label(code + " " + reason));
					errMsg.setVisible(true);
				}
			}
		};
		
		request.request(RequestBuilder.GET, "/settings/"+section, null);
	}
	
	private void refreshFields(SettingGroup setGrp)
	{
		clear();
		errMsg.setVisible(false);
		add(errMsg);
		HashMap<String, SingleSettingWgt> newSettings = new HashMap<String, SingleSettingWgt>();
		
		for(String key : setGrp.getKeys())
		{
			SingleSettingWgt wgt = null;
			System.out.println(key);
			
			if(settingWgts.containsKey(key))
				wgt = settingWgts.get(key);
			else
				wgt = new SingleSettingWgt(key);
			
			newSettings.put(key, wgt);
			wgt.currentValue(setGrp.getSetting(key));
			add(wgt);
		}
		
		settingWgts = newSettings;
	}
	
	private final native SettingGroup getSettings(String settings)
	/*-{
		return eval("("+settings+")");
	}-*/;
}
