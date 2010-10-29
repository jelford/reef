package uk.ac.imperial.vazels.reef.client.settings;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class SingleSettingWgt extends HorizontalPanel
{
	private final String name;
	private TextBox input;
	private String type;
	
	public SingleSettingWgt(String name)
	{
		this.name = name;
		
		this.input = new TextBox();
		this.input.setName(name);
		this.type="";
		
		this.add(new Label(name));
		this.add(input);
	}
	
	public String getName()
	{
		return name;
	}
	
	// Called to inform the widget of the current setting on the server
	public void currentValue(Object value)
	{
		if(value instanceof Integer)
			type = "_i";
		else if(value instanceof Double)
			type = "_d";
		else if(value instanceof String)
			type = "_s";
		else
			type = "";
		
		this.input.setName(name+type);
		
		this.input.setValue((value==null)?"":value.toString());
	}
}
