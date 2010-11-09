package uk.ac.imperial.vazels.reef.client.servercontrol;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.MultipleRequester.Converter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class ServerControl extends Composite {
  private Button btnStopServer;
  private Button btnStartServer;
  
  /**
   * URIs to start and stop the server.
   */
  private static final String SERVER_START_URI="/control/start";
  private static final String SERVER_STOP_URI="/control/stop";

  public ServerControl() {

    
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    initWidget(horizontalPanel);
    horizontalPanel.setSize("176px", "22px");
    
    btnStartServer = new Button("Start Server");
    horizontalPanel.add(btnStartServer);
    
    btnStopServer = new Button("Stop Server");
    btnStopServer.setEnabled(false);
    horizontalPanel.add(btnStopServer);
    
    
    /*
     * Add ClickHandlers for the buttons
     */
    btnStartServer.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        btnStartServer.setEnabled(false);
        new SetServerRunningRequest(true).go(new RequestHandler<Void>(){

          @Override
          public void handle(Void reply, boolean success, String message) {
            if(!success) {
              // Serverside changes haven't happened
              setRunningStateUI(false);
            } else {
              setRunningStateUI(true);
            }
          }
          
        });
      }
    });
    
    btnStopServer.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        btnStopServer.setEnabled(false);
        new SetServerRunningRequest(false).go(new RequestHandler<Void>(){

          @Override
          public void handle(Void reply, boolean success, String message) {
            if(!success) {
              setRunningStateUI(true);
            } else {
              setRunningStateUI(false);
            }
          }
        });
      }
    });
  }
  
  /**
   * Set the UI elements (buttons, ...) to reflect the new running state
   * of the control centre.
   * @param running
   */
  private void setRunningStateUI(boolean running) {
    btnStartServer.setEnabled(!running);
    btnStopServer.setEnabled(running);
  }
  
  /**
   * Basic request class taking a boolean argument to the constructor to tell
   * it whether to run the server or not.
   * @author james
   *
   */
  private class ServerRunningRequest {
    private MultipleRequester<Void> mStartRequest;
    private MultipleRequester<Void> mStopRequest;
    private MultipleRequester<Void> mStatusRequest;
        
    public ServerRunningRequest() {
      mStartRequest = new MultipleRequester<Void>(RequestBuilder.POST, "/control/start", null);
      mStopRequest = new MultipleRequester<Void>(RequestBuilder.POST, "/control/stop", null);
      mStatusRequest = new MultipleRequester<Void>(RequestBuilder.GET, "/control/", null);
    }

    public void startServer(){
      
    }
    
    private Converter<ServerStatus> mServerStatusConverter;
    
    
  }
  
  /**
   * Get the running/stopped status of the server
   */
}
