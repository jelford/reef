package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.FlexTable;

public class SettingsSectionWgt extends Composite {
  protected Map<String, SingleSettingWgt> settingWgts;
  protected String section;
  private DisclosurePanel errorBox;
  private Label errMsg;
  private FlexTable settingArea;

  public SettingsSectionWgt(String section) {
    
    VerticalPanel mainPanel = new VerticalPanel();
    initWidget(mainPanel);
    
    errorBox = new DisclosurePanel("Problem retrieving settings");
    errorBox.setOpen(false);
    errorBox.setAnimationEnabled(true);
    mainPanel.add(errorBox);
    mainPanel.setCellHorizontalAlignment(errorBox, HasHorizontalAlignment.ALIGN_CENTER);
    
    errMsg = new Label("");
    errorBox.setContent(errMsg);
    errMsg.setSize("5cm", "4cm");
    
    settingArea = new FlexTable();
    mainPanel.add(settingArea);
    mainPanel.setCellHorizontalAlignment(settingArea, HasHorizontalAlignment.ALIGN_CENTER);
    
    this.section = section;
    settingWgts = new HashMap<String, SingleSettingWgt>();
  }

  public void refreshFields(){
    SettingsManager.getManager().getSettingsNow(section,
        new RequestHandler<SettingGroup>() {
          @Override
          public void handle(SettingGroup reply, boolean success, String reason) {
            if (success) {
              refreshFields(reply);
              setError(null);
            } else {
              setError(reason);
            }
          }
        });
  }
  
  void refreshFields(SettingGroup setGrp){
    settingArea.removeAllRows();

    HashMap<String, SingleSettingWgt> newSettings = new HashMap<String, SingleSettingWgt>();

    for (String key : setGrp.keys()) {
      SingleSettingWgt wgt = null;

      if (settingWgts.containsKey(key))
        wgt = settingWgts.get(key);
      else
        wgt = new SingleSettingWgt(key);

      newSettings.put(key, wgt);
      wgt.updateValue(section);
      int row = settingArea.getRowCount();
      settingArea.setText(row, 0, key);
      settingArea.setWidget(row, 1, wgt);
    }

    settingWgts = newSettings;
  }
  
  public void addChanges(){
    SettingsManager manager = SettingsManager.getManager();
    
    for(SingleSettingWgt wgt : settingWgts.values()){
      manager.addChange(section, wgt.getChange());
    }
  }
  
  void setError(String err){
    errorBox.setOpen(false);
    if(err != null)
      errMsg.setText(err);
    errorBox.setVisible(err != null);
  }
}
