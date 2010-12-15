package uk.ac.imperial.vazels.reef.client.output;


import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DisplayOutput extends Composite {
  
  private final Button refreshButton = new Button("Reload Data");
  private OutputView view = new OutputView();
  
  public DisplayOutput() {

    DecoratorPanel decor = new DecoratorPanel();
    VerticalPanel mainPanel = new VerticalPanel();
    
    initWidget(decor);
    mainPanel.setSize("700px", "300px");
    
    mainPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    
    Panel temp = new HorizontalPanel();
    temp.add(refreshButton);
    
    decor.add(mainPanel);
    mainPanel.add(temp);
    
    refreshButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        refresh();
      }
    });
    
    
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
