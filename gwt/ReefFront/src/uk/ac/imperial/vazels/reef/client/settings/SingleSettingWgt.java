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
  public void updateValue(String section) {
    // We assume the cached values are fine if they're less than 1 min old
    // Usually the values will have been taken by the Section widget

    final SingleSettingWgt widget = this;

    SettingsManager.getManager().getSetting(section, name,
        new SettingsManager.RequestHandler<Setting>() {
          @Override
          public void handle(Setting reply, boolean success, String reason) {
            if (success && reply != null) {
              switch (reply.getType()) {
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
                type = "";
              }

              widget.setName(name + type);
              widget.setValue((reply == null) ? "" : reply.toString());
            }
          }
        }, 1);
  }
}
