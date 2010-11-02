package uk.ac.imperial.vazels.reef.client.settings;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SectionList;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;

public class SettingsWgt extends Composite {
  private Label errorMsg;
  private DisclosurePanel errorBox;
  private DecoratedTabPanel sectionTabs;
  private Map<String, SettingsSectionWgt> sectionWgts;
  private Label infoTxt;

  public SettingsWgt() {
    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    initWidget(mainPanel);
    
    sectionTabs = new DecoratedTabPanel();
    sectionTabs.setAnimationEnabled(true);
    mainPanel.add(sectionTabs);
    mainPanel.setCellHorizontalAlignment(sectionTabs, HasHorizontalAlignment.ALIGN_CENTER);
    
    infoTxt = new Label("Hello, I'm the settings manager!\n\nPress refresh to grab the settings, then you can click a tab at the top to choose a section to browse. Once you've made some changes remember to hit save to keep them.");
    sectionTabs.add(infoTxt, "Info", false);
    infoTxt.setSize("5cm", "3cm");
    
    errorBox = new DisclosurePanel("A problem occurred...");
    errorBox.setOpen(false);
    errorBox.setVisible(false);
    errorBox.setAnimationEnabled(true);
    mainPanel.add(errorBox);
    mainPanel.setCellHorizontalAlignment(errorBox, HasHorizontalAlignment.ALIGN_CENTER);
    
    errorMsg = new Label("");
    errorBox.setContent(errorMsg);
    errorMsg.setSize("5cm", "4cm");
    
    HorizontalPanel buttons = new HorizontalPanel();
    mainPanel.add(buttons);
    mainPanel.setCellHorizontalAlignment(buttons, HasHorizontalAlignment.ALIGN_CENTER);
    
    Button refreshBtn = new Button("Refresh");
    refreshBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        refreshSettings();
      }
    });
    refreshBtn.setText("Refresh");
    buttons.add(refreshBtn);
    
    Button saveBtn = new Button("Save");
    saveBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        saveSettings();
      }
    });
    buttons.add(saveBtn);
    buttons.setCellHorizontalAlignment(saveBtn, HasHorizontalAlignment.ALIGN_RIGHT);
    
    sectionWgts = new HashMap<String, SettingsSectionWgt>();
    sectionTabs.selectTab(0);
  }

  private void refreshSettings(){
    SettingsManager.getManager().getSectionsNow(
        new RequestHandler<SectionList>() {
          @Override
          public void handle(SectionList reply, boolean success, String reason) {
            if (success) {
              refreshSettings(reply);
              setError(null);
            } else {
              setError(reason);
            }
          }
        });
  }
  
  private void refreshSettings(SectionList sections){
    HashMap<String, SettingsSectionWgt> newSectionWgts = new HashMap<String, SettingsSectionWgt>();

    // Remove all but the first
    while(sectionTabs.getWidgetCount() > 1)
      sectionTabs.remove(1);
    sectionTabs.selectTab(0);
    
    // Create new hashmap of the sections, only remove them if they are no longer used
    for (String section : sections) {
      SettingsSectionWgt wgt = null;

      if (sectionWgts.containsKey(section))
        wgt = sectionWgts.get(section);
      else
        wgt = new SettingsSectionWgt(section);

      newSectionWgts.put(section, wgt);
      wgt.refreshFields();
      sectionTabs.add(wgt, section);
    }

    sectionWgts = newSectionWgts;
  }
  
  private void saveSettings(){
    SettingsManager manager = SettingsManager.getManager();
    // Dangerous!
    // If someone else is using the settings manager their pending changes will vanish.
    manager.clearChanges();
    
    for(SettingsSectionWgt section : sectionWgts.values())
      section.addChanges();
    
    manager.commitChanges(null);
  }
  
  private void setError(String err){
    errorBox.setOpen(false);
    if(err != null)
      errorMsg.setText(err);
    errorBox.setVisible(err != null);
  }
}
