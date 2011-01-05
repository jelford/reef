package uk.ac.imperial.vazels.reef.client.actors;

import uk.ac.imperial.vazels.reef.client.actors.ActorOverlay;

//represents an actor as represented on the server
public class Actor {
  private final String name;
  private String type;  

  /* create the actor representation
   * @param name Either the workload name, or a json string representing the workload.
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
  
  protected native ActorOverlay parseJSON(String json) /*-{
  try {
    return JSON.parse(json);
  }
  catch(e) {
    return null;
  }
}-*/;
}