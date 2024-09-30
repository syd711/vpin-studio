package de.mephisto.vpin.ui.tables.actions;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

abstract public class Action {
  private final KeyCombination keyCombination;
  private final String description;

  public Action(KeyCombination keyCombination, String description) {
    this.keyCombination = keyCombination;
    this.description = description;
  }

  public boolean matches(KeyEvent event) {
    return keyCombination.match(event);
  }

  public String getDescription() {
    return description;
  }

  abstract public void execute(GameRepresentation game) throws Exception;
}
