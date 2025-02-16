package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;

import java.util.Optional;

public interface IEmulatorScriptPanel {

  void setData(Optional<GameEmulatorScript> script);

  void applyValues();
}
