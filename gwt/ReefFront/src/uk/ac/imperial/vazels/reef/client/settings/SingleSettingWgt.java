package uk.ac.imperial.vazels.reef.client.settings;

import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;

import com.google.gwt.user.client.ui.TextBox;

public class SingleSettingWgt extends TextBox {
  private final String name;
  private String type;

  public SingleSettingWgt(String name) {
    this.name = name;

    this.setName(name);
    this.type = "";
  }

  public String getSetting() {
    return name;
  }

  // Called to inform the widget of the current setting on the server
  public void currentValue(Setting value) {
    switch (value.getType()) {
    case STRING:
      type = "_i";
      break;
    case DOUBLE:
      type = "_d";
      break;
    case INTEGER:
      type = "_i";
      break;
    default:
      type="";
    }

    this.setName(name + type);

    this.setValue((value == null) ? "" : value.toString());
  }
}
