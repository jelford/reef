package uk.ac.imperial.vazels.reef.client.output;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DisplayOutput extends Composite {
  
  private static DisplayOutputUiBinder uiBinder = GWT
  .create(DisplayOutputUiBinder.class);

  interface DisplayOutputUiBinder extends UiBinder<Widget, DisplayOutput> {
  }
  
  @UiField OutputView view = new OutputView();
  
  public DisplayOutput() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  @UiHandler("refresher")
  void onClick(ClickEvent event) {
    refresh();
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
