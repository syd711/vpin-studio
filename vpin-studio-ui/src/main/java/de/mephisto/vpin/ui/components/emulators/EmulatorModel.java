package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

import static de.mephisto.vpin.ui.Studio.client;

public class EmulatorModel extends BaseLoadingModel<GameEmulatorRepresentation, EmulatorModel> {

  public EmulatorModel(GameEmulatorRepresentation emulatorRepresentation) {
    super(emulatorRepresentation);
  }

  @Override
  public boolean sameBean(GameEmulatorRepresentation object) {
    return object.equals(getBean());
  }

  @Override
  public void load() {

  }

  @Override
  public String getName() {
    return getBean().getName();
  }

  public String getDescription() {
    return getBean().getDescriptions();
  }

  public boolean isEnabled() {
    return getBean().isEnabled();
  }

  public void setEnabled(boolean b) {
    getBean().setEnabled(b);
  }

  public void save() {

  }
}
