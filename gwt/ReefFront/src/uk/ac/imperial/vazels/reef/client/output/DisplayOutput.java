package uk.ac.imperial.vazels.reef.client.output;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.groups.GroupManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class DisplayOutput extends Composite {
  
  private static final boolean DEBUG_MODE = true;
  private static DisplayOutputUiBinder uiBinder = GWT
  .create(DisplayOutputUiBinder.class);

  interface DisplayOutputUiBinder extends UiBinder<Widget, DisplayOutput> {
  }
  
  @UiField OutputView view = new OutputView();
  
  public DisplayOutput() {
    initWidget(uiBinder.createAndBindUi(this));
    // Make sure this is up to date incase there was a page refresh!
    GroupManager.getManager().serverChange();
    refresh();
  }
  
  @UiHandler("refresher")
  void onClickRefresher(ClickEvent event) {
    refresh();
  }
  
  @UiHandler("downloadData")
  void onClickDownloadData(ClickEvent event) {
    Window.open(new AddressResolution().resolve("/downloaddata/data.tar.gz"), "", "");
  }
  
  
  /**
   * Sends request to get all the vazels to send any output they have.
   * If it succeeds then update the page with the new data, does nothing 
   * otherwise. Not sure if nested requests is entirely a good idea though...?
   */
  private void refresh() {
    new FetchDataRequest().go(new RequestHandler<String>() {
      @Override
      public void handle(String reply, Integer responseCode, String message) {
        if (this.isSuccessful(responseCode)  || DEBUG_MODE) {
          new OutputDataRequest().go(new RequestHandler<OutputData>(){

            @Override
            public void handle(OutputData reply,final Integer responseCode, String message) {
              if (this.isSuccessful(responseCode)) {
                view.useData(reply);
              }
            }
          });
        }
      }
    });
    
    
  }
  
  
  /**
   * 
   * Class used to handle the Request for data from the central 
   * server
   *
   */
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
  
  /**
   * 
   * Class used to dispatch a request to get data from
   * all the little vazels.
   *
   */
  private class FetchDataRequest extends MultipleRequester<String> {
    FetchDataRequest(){
      super(RequestBuilder.GET, "/control/getalloutput/",
          new Converter<String>() {
            @Override
            /*
             * Don't really have any need for anything to be
             * returned, it almost doesn't matter if it doesn't 
             * succeed. But it would be nice...
             * 
             */
            public String convert(String original) {
                return original;
            }
          }
      );
    }
  }

}
