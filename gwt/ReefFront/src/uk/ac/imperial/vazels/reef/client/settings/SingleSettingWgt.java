package uk.ac.imperial.vazels.reef.client.settings;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;

import com.google.gwt.user.client.ui.TextBox;

public class SingleSettingWgt extends TextBox {
  private final String name;
  private Setting.SettingType type;

  public SingleSettingWgt(String name) {
    this.name = name;
    this.type = null;
  }

  public String getSetting() {
    return name;
  }
  
  public SettingsManager.PendingChange getChange(){
    try{
      switch(type){
      case DOUBLE:
        return new SettingsManager.PendingDouble(name, Double.parseDouble(getValue()));
      case INTEGER:
        return new SettingsManager.PendingInteger(name, Integer.parseInt(getValue()));
      case STRING:
        return new SettingsManager.PendingString(name, getValue());
      }
    }
    catch(Exception e){}
    
    return null;
  }

  // Called to inform the widget of the current setting on the server
  public void updateValue(String section) {
    // We assume the cached values are fine if they're less than 1 min old
    // Usually the values will have been taken by the Section widget

    final SingleSettingWgt widget = this;

    SettingsManager.getManager().getSetting(section, name,
        new RequestHandler<Setting>() {
          @Override
          public void handle(Setting reply, boolean success, String reason) {
            if (success && reply != null) {
              type = reply.getType();

              widget.setValue((reply == null) ? "" : reply.toString());
            }
          }
        });
  }
}
