package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class SettingsSectionWgt extends FlexTable {
  private final String section;
  protected Map<String, SingleSettingWgt> settingWgts;
  protected DisclosurePanel errMsg;

  public SettingsSectionWgt(String section) {
    this.section = section;
    errMsg = new DisclosurePanel("Could not check settings...");
    errMsg.setVisible(false);

    getFlexCellFormatter().setColSpan(0, 0, 2);
    setWidget(0, 0, errMsg);
    settingWgts = new HashMap<String, SingleSettingWgt>();
  }

  public String getSection() {
    return section;
  }

  public void refreshFields() {
    SettingsManager.getManager().getSettingsNow(section,
        new RequestHandler<SettingGroup>() {
          @Override
          public void handle(SettingGroup reply, boolean success, String reason) {
            if (success) {
              refreshFields(reply);
            } else {
              errMsg.clear();
              errMsg.add(new Label(reason));
              errMsg.setVisible(true);
            }
          }
        });
  }

  private void refreshFields(SettingGroup setGrp) {
    removeAllRows();
    errMsg.setVisible(false);

    getFlexCellFormatter().setColSpan(0, 0, 2);
    setWidget(0, 0, errMsg);

    HashMap<String, SingleSettingWgt> newSettings = new HashMap<String, SingleSettingWgt>();

    for (String key : setGrp.keys()) {
      SingleSettingWgt wgt = null;

      if (settingWgts.containsKey(key))
        wgt = settingWgts.get(key);
      else
        wgt = new SingleSettingWgt(key);

      newSettings.put(key, wgt);
      wgt.updateValue(section);
      int row = getRowCount();
      setText(row, 0, key);
      setWidget(row, 1, wgt);
    }

    settingWgts = newSettings;
  }
  
  public void addChanges(){
    SettingsManager manager = SettingsManager.getManager();
    
    for(SingleSettingWgt wgt : settingWgts.values()){
      manager.addChange(section, wgt.getChange());
    }
  }
}
