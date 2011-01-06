package uk.ac.imperial.vazels.reef.client.sue;

import uk.ac.imperial.vazels.reef.client.managers.Manager;

public class SingleSueComponentManager extends Manager<SueComponent, Void> {
  private SueComponent sueComponent;
  
  SingleSueComponentManager(String name) {
    super(false);
    setPuller(null);
    this.sueComponent = new SueComponent(name);
  }
  
  public String getName() {
    return sueComponent.getName();
  }

  @Override
  protected boolean receivePullData(SueComponent pulled) {
    // unused
    return false;
  }

  @Override
  protected boolean receivePushData(Void pushed) {
    // unused
    return false;
  }
}
