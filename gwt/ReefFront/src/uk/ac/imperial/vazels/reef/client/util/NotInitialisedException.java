package uk.ac.imperial.vazels.reef.client.util;

/**
 * Use this whenever people try to get something that needs
 * some initialisation code done to it first (e.g. they're making
 * calls in the wrong order). It shouldn't happen, but just in case ;)
 * @author james
 *
 */
public class NotInitialisedException extends Exception {

  /**
   * TODO: Change this on new builds?
   */
  private static final long serialVersionUID = 1L;

  public NotInitialisedException() {
    super();
  }
  
  public NotInitialisedException(String e) {
    super(e);
  }
}
