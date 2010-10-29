package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.EasyRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SettingsWgt extends VerticalPanel
{
	private final String settingsURL = "/settings/";
	
	private Label statusBar = new Label();
	private StackPanel settingAccordion = new StackPanel();
	private Button refreshBtn = new Button("Refresh");
	private Map<String, SettingsSectionWgt> sectionWgts;
	
	public SettingsWgt()
	{
		sectionWgts = new HashMap<String, SettingsSectionWgt>();
		
		refreshBtn.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				refreshSettings();
			}
		});
		
		this.add(statusBar);
		this.add(settingAccordion);
		this.add(refreshBtn);
	}
	
	private void refreshSettings()
	{
		EasyRequest request = new EasyRequest()
		{
			@Override
			protected void requested(Integer code, String reason, String content)
			{
				if(code == null)
					statusBar.setText("There was an unknown problem with the request...");
				else if(code == 200)
				{
						SectionList sections = getSections(content);
						refreshSettings(sections);
						statusBar.setText("");
				}
				else
				{
					String err = "Problem: "+ code;
					if(reason != null)
						err += " - " + reason;
					statusBar.setText(err);
				}
			}
		};
		
		request.request(RequestBuilder.GET, settingsURL, null);
	}
	
	private void refreshSettings(SectionList sections)
	{
		settingAccordion.clear();
		HashMap<String, SettingsSectionWgt> newSectionWgts = new HashMap<String, SettingsSectionWgt>();
		
		for(int i=0;i<sections.length();i++)
		{
			String section = sections.sectionAt(i);
			SettingsSectionWgt wgt = null;
			
			if(sectionWgts.containsKey(section))
				wgt = sectionWgts.get(section);
			else
				wgt = new SettingsSectionWgt(section);
			
			newSectionWgts.put(section, wgt);
			wgt.refreshFields();
			settingAccordion.add(wgt, section);
		}
		
		sectionWgts = newSectionWgts;
	}
	
	private final native SectionList getSections(String sections)
	/*-{
		return eval(sections);
	}-*/;
}