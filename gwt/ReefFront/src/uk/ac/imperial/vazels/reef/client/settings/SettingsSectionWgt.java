package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.EasyRequest;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class SettingsSectionWgt extends FlexTable
{
	private final String section;
	protected Map<String, SingleSettingWgt> settingWgts;
	protected DisclosurePanel errMsg;
	
	public SettingsSectionWgt(String section)
  {
		this.section = section;
		errMsg = new DisclosurePanel("Could not check settings...");
		errMsg.setVisible(false);
		
		getFlexCellFormatter().setColSpan(0, 0, 2);
		setWidget(0,0,errMsg);
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
						SettingGroup settings = new SettingGroup(content);
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
		removeAllRows();
		errMsg.setVisible(false);
		
		getFlexCellFormatter().setColSpan(0, 0, 2);
    setWidget(0,0,errMsg);
    
		HashMap<String, SingleSettingWgt> newSettings = new HashMap<String, SingleSettingWgt>();
		
		for(String key : setGrp.keys())
		{
			SingleSettingWgt wgt = null;
			
			if(settingWgts.containsKey(key))
				wgt = settingWgts.get(key);
			else
				wgt = new SingleSettingWgt(key);
			
			newSettings.put(key, wgt);
			wgt.currentValue(setGrp.get(key));
			int row = getRowCount();
			setText(row, 0, key);
			setWidget(row, 1, wgt);
		}
		
		settingWgts = newSettings;
	}
}
