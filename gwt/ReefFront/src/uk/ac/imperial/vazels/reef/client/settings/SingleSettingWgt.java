package uk.ac.imperial.vazels.reef.client.settings;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingChange;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingDouble;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingInteger;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingString;
import uk.ac.imperial.vazels.reef.client.settings.SettingsManager.PendingDeletion;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting.SettingType;

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
  
  public SingleSettingWgt(String name, SettingType type){
    this(name);
    
    switch(type){
    case DOUBLE:
      setDouble(0.0);
      break;
    case INTEGER:
      setInteger(0);
      break;
    case STRING:
      setString("");
      break;
    }
  }
  
  public String getKey(){
    return name;
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
                setDouble((Double)reply.getSetting());
                break;
              case INTEGER:
                setInteger((Integer)reply.getSetting());
                break;
              case STRING:
                setString((String)reply.getSetting());
                break;
              }
              
              if(type == null)
                boxArea.clear();
            }
          }
        });
  }
  
  public void setDouble(Double val){
    if (val != null) {
      type = SettingType.DOUBLE;
      
      DoubleBox inputD = new DoubleBox();
      inputD.setValue(val);
      input = inputD;
      
      boxArea.setWidget(input);
    }
    else{
      type = null;
      boxArea.clear();
    }
  }
  
  public void setInteger(Integer val){
    if (val != null) {
      type = SettingType.INTEGER;
      
      IntegerBox inputI = new IntegerBox();
      inputI.setValue(val);
      input = inputI;
      
      boxArea.setWidget(input);
    }
    else{
      type = null;
      boxArea.clear();
    }
  }
  
  public void setString(String val){
    
    if (val != null) {
      type = SettingType.STRING;
      
      TextBox inputS = new TextBox();
      inputS.setValue(val);
      input = inputS;
      
      boxArea.setWidget(input);
    }
    else{
      type = null;
      boxArea.clear();
    }
  }
  
  public void erase(){
    this.type = null;
  }

  public PendingChange getChange(){
    if(type == null)
      return new PendingDeletion(name);
    try{
      switch(type){
      case DOUBLE:
        return new PendingDouble(name, ((DoubleBox)input).getValueOrThrow());
      case INTEGER:
        return new PendingInteger(name, ((IntegerBox)input).getValueOrThrow());
      case STRING:
        return new PendingString(name, ((TextBox)input).getValue());
      }
    }
    catch(Exception e){}
    
    return null;
  }
}
