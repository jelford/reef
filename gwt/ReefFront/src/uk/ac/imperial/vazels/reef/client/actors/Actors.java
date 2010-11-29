package uk.ac.imperial.vazels.reef.client.actors;

import java.util.LinkedList;

public class Actors {
  static LinkedList <String> actors = null;

  private Actors() {
  }

  static void add (String actor) {
    if(actors == null)
      actors = new LinkedList<String>();    
    actors.add(actor);
  }

  static LinkedList <String> returnActors() {
    if(actors == null)
      actors = new LinkedList<String>();
    return actors;
  }

  //not implemented with server yet
  static void remove(String actor) {
    actors.remove(actor);
  }
}