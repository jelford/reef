package uk.ac.imperial.vazels.reef.client.managers;

/**
 * Raised when trying to push or pull without the requesters.
 */
public class MissingRequesterException extends Exception {
  private static final long serialVersionUID = 1L;
}