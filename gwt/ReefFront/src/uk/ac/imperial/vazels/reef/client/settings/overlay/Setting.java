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
	
	public String toString()
	{
		return setting.toString();
	}
	
	public enum SettingType {
    STRING, INTEGER, DOUBLE,
  }
}
