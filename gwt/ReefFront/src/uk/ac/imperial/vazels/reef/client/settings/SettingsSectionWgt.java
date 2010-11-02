package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting.SettingType;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class SettingsSectionWgt extends Composite {
  protected Map<String, SingleSettingWgt> settingWgts;
  protected String section;
  private DisclosurePanel errorBox;
  private Label errMsg;
  private FlexTable settingArea;
  private TextBox addIn;
  private ListBox addType;

  public SettingsSectionWgt(String section) {
    
    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    initWidget(mainPanel);
    
    errorBox = new DisclosurePanel("Problem retrieving settings");
    errorBox.setVisible(false);
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
    
    HorizontalPanel addPanel = new HorizontalPanel();
    addPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    mainPanel.add(addPanel);
    
    addIn = new TextBox();
    addPanel.add(addIn);
    
    addType = new ListBox();
    addType.addItem("String", "s");
    addType.addItem("Integer", "i");
    addType.addItem("Double", "d");
    addType.setSelectedIndex(0);
    addPanel.add(addType);
    
    Button addButton = new Button("Add");
    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        addSetting();
      }
    });
    addPanel.add(addButton);
    addPanel.setCellHorizontalAlignment(addButton, HasHorizontalAlignment.ALIGN_CENTER);
    
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
      addRow(key, wgt);
    }

    settingWgts = newSettings;
  }
  
  public void addChanges(){
    SettingsManager manager = SettingsManager.getManager();
    
    for(SingleSettingWgt wgt : settingWgts.values()){
      manager.addChange(section, wgt.getChange());
    }
  }
  
  public void addSetting(){
    String setting = addIn.getValue();
    addIn.setText("");
    
    if(!settingWgts.containsKey(setting)){
      String sType = addType.getValue(addType.getSelectedIndex());
      
      SettingType type = null;
      if(sType.equals("s"))
        type = SettingType.STRING;
      else if(sType.equals("i"))
        type = SettingType.INTEGER;
      else if(sType.equals("d"))
        type = SettingType.DOUBLE;
      
      SingleSettingWgt wgt = new SingleSettingWgt(setting, type);
      addRow(setting, wgt);
    }
  }
  
  private void addRow(final String name, SingleSettingWgt wgt){
    settingWgts.put(name, wgt);
    int row = settingArea.getRowCount();
    settingArea.setText(row, 0, name);
    settingArea.setWidget(row, 1, wgt);
    
    Button rmButton = new Button("X");
    rmButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        removeRow(name);
      }
    });
    
    settingArea.setWidget(row, 2, rmButton);
  }
  
  private void removeRow(String name){
    if(settingWgts.containsKey(name)){
      SingleSettingWgt wgt = settingWgts.get(name);
      wgt.erase();
      for(int i=0; i<settingArea.getRowCount(); i++){
        if(((SingleSettingWgt)settingArea.getWidget(i, 1)).getKey().equals(name)){
          settingArea.removeRow(i);
          break;
        }
      }
    }
  }
  
  private void setError(String err){
    errorBox.setOpen(false);
    if(err != null)
      errMsg.setText(err);
    errorBox.setVisible(err != null);
  }
}
