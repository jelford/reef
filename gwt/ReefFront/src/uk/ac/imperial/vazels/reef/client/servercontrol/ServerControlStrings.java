package uk.ac.imperial.vazels.reef.client.servercontrol;

import com.google.gwt.i18n.client.Constants;

/**
 * Interface to allow Strings to be localized using ServerControlStrings.properties.
 * @author james
 *
 */
public interface ServerControlStrings extends Constants {
  String startControlCentre();
  String goBackToSetup();
  String downloadProbe();
  String controlCentreTimeout();
  String experimentRunning();
  String unknownServerState();
  String setupInstructions();
  String setupCheckBox();
  String startExperiment();
  String startExperimentFailed();
  
  String isRunningExperiment();
  String isFinished();
  String isReady();
  String isRunning();
  String isStarting();
  String hasTimedOut();
  String hasUnknownState();
  String theVazelsSystemStatus();
}
