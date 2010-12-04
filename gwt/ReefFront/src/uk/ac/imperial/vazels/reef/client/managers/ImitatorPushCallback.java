package uk.ac.imperial.vazels.reef.client.managers;

public class ImitatorPushCallback implements PushCallback {
  private final PullCallback callback;
  
  public ImitatorPushCallback(PullCallback callback) {
    this.callback = callback;
  }
  
  @Override
  public void got() {
    if(callback != null) {
      callback.got();
    }
  }

  @Override
  public void failed() {
  }
}
