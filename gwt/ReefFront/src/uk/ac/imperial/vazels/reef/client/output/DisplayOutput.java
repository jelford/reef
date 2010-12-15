package uk.ac.imperial.vazels.reef.client.output;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DisplayOutput extends Composite {
  
  private final Label testLabel = new Label("Hello World");
  private final Button refreshButton = new Button("Refresh");
  private OutputView view = new OutputView();
  
  public DisplayOutput() {
    
    VerticalPanel mainPanel = new VerticalPanel();
    
    initWidget(mainPanel);
    mainPanel.setSize("521px", "100px");
    
    Panel temp = new HorizontalPanel();
    temp.setSize("521px", "22px");
    temp.add(testLabel);
    temp.add(refreshButton);
    
    refreshButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          refresh();
        }
    });
    
    mainPanel.add(temp);
    mainPanel.add(view); //placeholder for data to be put into
  }
  
  private void refresh() {
    new OutputDataRequest().go(new RequestHandler<OutputData>(){

      @Override
      public void handle(OutputData reply, boolean success, String message) {
        if (success) {
          view.useData(reply);
        }
      }
    });
  }
  
  private class OutputDataRequest extends MultipleRequester<OutputData>{
    OutputDataRequest() {
      super(RequestBuilder.GET, "/output/", 
          new Converter<OutputData>() {

        @Override
        public OutputData convert(String original) {
            return new OutputData(original);
        }
      });
    }
  }

}
