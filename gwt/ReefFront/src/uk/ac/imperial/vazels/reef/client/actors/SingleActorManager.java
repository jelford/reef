package uk.ac.imperial.vazels.reef.client.actors;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.AddressResolution;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.Manager;

/**
 * Manages an actor object, deals with syncing to the server.
 */
public class SingleActorManager extends Manager<Actor, Void> {
  private Actor actor;

  /**
   * This should only ever be used inside actor manager.
   * We do not ever want to manually create an instance of this class.
   * <p>
   * This cannot edit an actor, that needs to be done with forms.
   * @param name Actor that this manager controls.
   */
  SingleActorManager(String name) {
    super(false);
    setPuller(new ActorRequest(name));
    this.actor = new Actor(name);
  }

  /**
   * Get the name of this actor.
   * @return actor name
   */
  public String getName() {
    return actor.getName();
  }

  /**
   * Get the type of this actor.
   * @return actor language
   */
  public String getType() {
    return actor.getType();
  }
  
  /**
   * Get the URL to download the actor file from.
   * @return A URL pointing to the file to download.
   */
  public String getDownloadURL() {
    return new AddressResolution().resolve("/actors/"+ actor.getName() + ".tar.gz");
  }
  
  // Data processing
  
  @Override
  protected boolean receivePullData(Actor data) {
    actor = data;
    return true;
  }

  @Override
  protected boolean receivePushData(Void data) {
    // Unused
    return false;
  }
  
  // Requests
  
  protected class ActorRequest extends MultipleRequester<Actor> {
    public ActorRequest(String ext) {
      super(RequestBuilder.GET, "/actors/"+ext, new Converter<Actor>() {
        @Override
        public Actor convert(String original) {
          return new Actor(original);
        }
      });
    }
  }
}