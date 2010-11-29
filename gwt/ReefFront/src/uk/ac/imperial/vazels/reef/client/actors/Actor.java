package uk.ac.imperial.vazels.reef.client.actors;

import uk.ac.imperial.vazels.reef.client.actors.ActorOverlay;

/**
 * Java version of the {@link ActorOverlay}.
 */
public class Actor {
  private final String name;
  private String type;  

  /** 
   * Create the actor representation.
   * @param name Either the actor name, or a json string representing the actor.
   */
  public Actor(String name) {
    ActorOverlay actor = parseJSON(name);
    if(actor == null) {
      this.name = name;
    }
    else {
      this.name = actor.getName();
      this.type = actor.getType();
    }
  }

  /**
   * Get the name of the actor.
   * @return Name of the actor.
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Get the programming language/type of the actor.
   * @return Type of the actor.
   */
  public String getType() {
    return this.type;
  }
  
  /**
   * Try to parse a string as json.
   * @param json The json string.
   * @return An {@link ActorOverlay} if the parse was successful or {@code null}.
   */
  protected native ActorOverlay parseJSON(String json) /*-{
    try {
      return JSON.parse(json);
    }
    catch(e) {
      return null;
    }
  }-*/;
}