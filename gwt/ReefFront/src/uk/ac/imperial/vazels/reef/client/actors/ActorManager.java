package uk.ac.imperial.vazels.reef.client.actors;

import java.util.Set;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.managers.JsArrayStringSetConverter;
import uk.ac.imperial.vazels.reef.client.managers.ListedCollectionManager;

public class ActorManager extends ListedCollectionManager<String, SingleActorManager> {
    private static ActorManager manager = null;
    
    private ActorManager() {
      setPuller(new ActorPuller());
    }
    
    /**
     * Get the singleton instance of this class.
     * @return A global actor manager.
     */
    public static ActorManager getManager() {
      if(manager == null) {
        manager = new ActorManager();
      }
      return manager;
    }
    
    /**
     * Get the names of all the actors on the system.
     * @return A set of names.
     */
    public Set<String> getNames() {
      return this.getItems();
    }
    
    /**
     * Get the manager for a particular actor.
     * @param name The name of the actor to grab a manager for.
     * @return A manager for the specified actor.
     */
    public SingleActorManager getActorManager(String name) {
      return getItem(name);
    }

    /**
     * Called to notify the manager that an actor has been uploaded.
     * @param name Of the uploaded actor.
     */
    public void actorUploaded(String name) {
      this.serverChange(name);
    }
    
    @Override
    protected SingleActorManager createManager(String id, boolean nMan) {
      return new SingleActorManager(id);
    }
    
    private class ActorPuller extends MultipleRequester<Set<String>> {
      public ActorPuller() {
        super(RequestBuilder.GET, "/workloads/", new JsArrayStringSetConverter());
      } 
    }
}
