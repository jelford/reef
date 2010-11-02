package uk.ac.imperial.vazels.reef.client.servercontrol;


public class ServerStatus {
  ServerStatusOverlay data;
  ServerState mServerState;

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
  
  private native final ServerStatusOverlay parseJSON(String json) /*-{
    return JSON.parse(json);
  }-*/;

}
