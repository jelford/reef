package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SectionList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SettingsWgt extends VerticalPanel {
  private Label statusBar = new Label();
  private StackPanel settingAccordion = new StackPanel();
  private Button refreshBtn = new Button("Refresh");
  private Button saveBtn = new Button("Save");
  private Map<String, SettingsSectionWgt> sectionWgts;

  public SettingsWgt() {
    sectionWgts = new HashMap<String, SettingsSectionWgt>();

    refreshBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        refreshSettings();
      }
    });
    
    saveBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        saveSettings();
      }
    });

    this.add(statusBar);
    this.add(settingAccordion);
    this.add(refreshBtn);
    this.add(saveBtn);
  }

  private void refreshSettings() {
    SettingsManager.getManager().getSectionsNow(
        new RequestHandler<SectionList>() {
          @Override
          public void handle(SectionList reply, boolean success, String reason) {
            if (success) {
              refreshSettings(reply);
              statusBar.setText("");
            } else {
              statusBar.setText("Problem: " + reason);
            }
          }
        });
  }

  private void refreshSettings(SectionList sections) {
    settingAccordion.clear();
    HashMap<String, SettingsSectionWgt> newSectionWgts = new HashMap<String, SettingsSectionWgt>();

    for (String section : sections) {
      SettingsSectionWgt wgt = null;

      if (sectionWgts.containsKey(section))
        wgt = sectionWgts.get(section);
      else
        wgt = new SettingsSectionWgt(section);

      newSectionWgts.put(section, wgt);
      wgt.refreshFields();
      settingAccordion.add(wgt, section);
    }

    sectionWgts = newSectionWgts;
  }
  
  public void saveSettings(){
    SettingsManager manager = SettingsManager.getManager();
    manager.clearChanges();
    
    for(SettingsSectionWgt section : sectionWgts.values())
      section.addChanges();
    
    manager.commitChanges(null);
  }
}