package uk.ac.imperial.vazels.reef.client;

//import uk.ac.imperial.vazels.reef.client.ReefTabPanel.ReefTabPanelUiBinder;

import uk.ac.imperial.vazels.reef.client.groups.AllocateGroups;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;


public class ReefTabPanel extends Composite {

  /**
   * Initialize this example.
   */
  


  /**
   * Table for holding the information on each group.
   */
  @UiField Button btnStart;
 
  @UiHandler("btnStart")
  void start(ClickEvent event){

  //RootPanel.getBodyElement().setInnerHTML(ReefFront.initialHTML);
  //RootPanel.get().clear();
  RootPanel.get("tabPanel").clear();
  RootPanel.get("tabPanel").add(new AllocateGroups());
   
  }
  @UiField DecoratedTabPanel tabPanel;

  private static ReefTabPanelUiBinder uiBinder = GWT
  .create(ReefTabPanelUiBinder.class);

interface ReefTabPanelUiBinder extends UiBinder<Widget, ReefTabPanel> {
}

@SuppressWarnings("deprecation")
public ReefTabPanel() {
initWidget(uiBinder.createAndBindUi(this));


tabPanel.selectTab(0);
}
}

