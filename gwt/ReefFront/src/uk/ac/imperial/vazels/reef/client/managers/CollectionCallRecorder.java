package uk.ac.imperial.vazels.reef.client.managers;

import java.util.Set;

/**
 * Makes a number of method calls to various objects and 
 */
public abstract class CollectionCallRecorder {
  private final Set<IManager> toCall;
  private final PushCallback callback;
  private boolean failed;
  
  /**
   * Create a call recorder that will call methods on all of the set given
   * and return after all responses have been received.
   * @param toCall A set of all the managers to call. This will be modified.
   * @param callback A callback to call with the response.
   */
  public CollectionCallRecorder(Set<IManager> toCall, PushCallback callback) {
    this.toCall = toCall;
    this.callback = callback;
    this.failed = false;
  }
  
  /**
   * Fire off all of the requests, only call callback once all have returned
   */
  public void start() throws MissingRequesterException {
    if(toCall.isEmpty()) {
      cb();
      return;
    }
    
    for(final IManager man : toCall) {
      call(man, new PushCallback() {
        @Override
        public void got() {
          checkOff(man);
        }
        
        @Override
        public void failed() {
          failed = true;
          checkOff(man);
        }
      });
    }
  }
  
  /**
   * Called to check off a manager from the list and process if we're done
   * @param man Manager to check off
   */
  private void checkOff(IManager man) {
    toCall.remove(man);
    if(toCall.isEmpty()) {
      cb();
    }
  }
  
  private void cb() {
    if(callback != null) {
      if(failed) {
        callback.failed();
      }
      else {
        callback.got();
      }
    }
  }
  
  /**
   * Call the required method.
   * @param man The manager to call the method on.
   * @param generatedCb The callback to hand to the function.
   */
  protected abstract void call(IManager man, PushCallback generatedCb) throws MissingRequesterException;
}