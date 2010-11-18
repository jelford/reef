package uk.ac.imperial.vazels.reef.client.servercontrol;


public class ServerStatus {
  private ServerStatusOverlay data;
  private ServerState mServerState;

  public ServerStatus(String original) {
    data = parseJSON(original);
    String statusString = data.getStatusString();
    if ("ready".equals(statusString)) {
      mServerState = ServerState.READY;
    } else if ("running".equals(statusString)) {
      mServerState = ServerState.RUNNING;
    } else if ("starting".equals(statusString)) {
      mServerState = ServerState.STARTING;
    } else if ("timeout".equals(statusString)) {
      mServerState = ServerState.TIMEOUT;
    }
  }

  protected enum ServerState {
    RUNNING,
    READY,
    STARTING,
    TIMEOUT;
  }
  
  public ServerState getState(){
    return mServerState;
  }
  
  private native final ServerStatusOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;

}
