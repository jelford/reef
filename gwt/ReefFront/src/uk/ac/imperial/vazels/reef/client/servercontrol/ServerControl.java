package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.user.client.Window;

/**
 * A wrapper class containing Requesters to talk to the server. Really, it's a
 * package. Except it's not; it's a class. Why have I done this? Well, I thought
 * we had quite enough packages, and also I wanted them to share bunch of resources.
 * 
 * Don't inherit from this; it will end badly.
 * @author james
 *
 */
public final class ServerControl {
  /**
   * It doesn't make sense to instantiate this. This makes it act like a
   * package. WHAT????? THEN USE A FUCKING PACKAGE!
   */
  private ServerControl() {}
  
  /**
   * The big red button to stop doing stuff if things go wrong.
   */
  public static void fail() {
    // Oh no! This is very bad indeed!
    Window.alert("It's all gone very wrong!");
    ServerStatusManager.getManager().setAutoRefresh(false);
  }
}
