package uk.ac.imperial.vazels.reef.client.servercontrol;


public class ServerStatus {
  private ServerStatusOverlay data;
  private ServerState mServerState;

  public ServerStatus(String original) {
    data = parseJSON(original);
    if ("ready".equals(data.getStatusString())) {
      mServerState = ServerState.READY;
    } else if ("running".equals(data.getStatusString())) {
      mServerState = ServerState.RUNNING;
    }
  }

  protected enum ServerState {
    RUNNING,
    READY;
  }
  
  public ServerState getState(){
    return mServerState;
  }
  
  private native final ServerStatusOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;

}
