package uk.ac.imperial.vazels.reef.client;

//import uk.ac.imperial.vazels.reef.client.ReefTabPanel.ReefTabPanelUiBinder;

import uk.ac.imperial.vazels.reef.client.output.DisplayOutput;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;


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

    // Start the server
    new MultipleRequester<Void>(RequestBuilder.GET, "/control/startexperiment", null).go(null);

    RootPanel.get("tabPanel").clear();
    RootPanel.get("tabPanel").add(new DisplayOutput());

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

