package uk.ac.imperial.vazels.reef.client.settings.overlay;


import com.google.gwt.core.client.JsArrayString;

public class SettingGroup {
  private JsSettingGroup settings;

  public SettingGroup(String content) {
    settings = parse(content);
  }

  private native JsSettingGroup parse(String content)
  /*-{
    return JSON.parse(content);
  }-*/;

  public String[] keys() {
    JsArrayString keys = settings.getKeys();
    String[] sKeys = new String[keys.length()];
    for (int i = 0; i < keys.length(); i++)
      sKeys[i] = keys.get(i);
    return sKeys;
  }

  public boolean contains(String key) {
    return settings.hasSetting(key);
  }

  public Setting.SettingType getType(String key) {
    String type = settings.getType(key);

    if ("string".equals(type)) {
      return Setting.SettingType.STRING;
    } else if ("number".equals(type)) {
      Double d = settings.getDoubleSetting(key);

      if (Math.round(d) == d) {
        return Setting.SettingType.INTEGER;
      } else {
        return Setting.SettingType.DOUBLE;
      }
    } else {
      return null;
    }
  }

  public Setting get(String key) {
    Setting.SettingType type = getType(key);

    switch (type) {
    case STRING:
      return new Setting(settings.getStringSetting(key), type);
    case INTEGER:
      return new Setting(settings.getIntSetting(key), type);
    case DOUBLE:
      return new Setting(settings.getDoubleSetting(key), type);
    default:
      return null;
    }
  }
}
