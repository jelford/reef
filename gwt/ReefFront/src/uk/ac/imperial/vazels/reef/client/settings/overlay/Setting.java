package uk.ac.imperial.vazels.reef.client.settings.overlay;

public class Setting
{
	private Object setting;
	private SettingType type;
	
	public Setting(Object setting, SettingType type)
	{
		this.setting = setting;
		this.type = type;
	}
	
	public SettingType getType()
	{
	  return type;
	}
	
	public Object getSetting()
	{
	  return setting;
	}
	
	public String toString()
	{
		return setting.toString();
	}
	
	public static enum SettingType {
    STRING, INTEGER, DOUBLE,
  }
}
