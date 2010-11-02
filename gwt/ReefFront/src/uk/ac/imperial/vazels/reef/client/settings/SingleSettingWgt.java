package uk.ac.imperial.vazels.reef.client.settings;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingChange;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingDouble;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingInteger;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingString;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SingleSettingWgt extends Composite {
  private String name;
  private Setting.SettingType type;
  private SimplePanel boxArea;
  private Widget input;

  public SingleSettingWgt(String name) {
    boxArea = new SimplePanel();
    initWidget(boxArea);
    
    this.name = name;
    this.type = null;
  }
  
  public void updateValue(String section){
    // We assume the cached values are fine if they're less than 1 min old
    // Usually the values will have been taken by the Section widget

    SettingsManager.getManager().getSetting(section, name,
        new RequestHandler<Setting>() {
          @Override
          public void handle(Setting reply, boolean success, String reason) {
            if (success && reply != null) {
              type = reply.getType();
              
              switch(type){
              case DOUBLE:
                input = new DoubleBox();
                ((DoubleBox)input).setValue((Double)reply.getSetting());
                break;
              case INTEGER:
                input = new IntegerBox();
                ((IntegerBox)input).setValue((Integer)reply.getSetting());
                break;
              case STRING:
                input = new TextBox();
                ((TextBox)input).setValue((String)reply.getSetting());
                break;
              default:
                input = null;
              }
              
              if(input == null)
                boxArea.clear();
              else
                boxArea.setWidget(input);
            }
          }
        });
  }

  public PendingChange getChange(){
    try{
      switch(type){
      case DOUBLE:
        return new PendingDouble(name, ((DoubleBox)input).getValueOrThrow());
      case INTEGER:
        return new PendingInteger(name, ((IntegerBox)input).getValueOrThrow());
      case STRING:
        return new PendingString(name, ((TextBox)input).getValueOrThrow());
      }
    }
    catch(Exception e){}
    
    return null;
  }
}
