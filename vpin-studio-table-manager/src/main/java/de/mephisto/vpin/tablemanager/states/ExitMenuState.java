package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.MenuController;

public class ExitMenuState extends MenuState {
  private final MenuState parentState;
  private final MenuController menuController;

  public ExitMenuState(MenuState parentState, MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterExitConfirmation();
  }

  @Override
  MenuState left() {
    return this;
  }

  @Override
  MenuState right() {
    return this;
  }

  @Override
  MenuState enter() {
    System.exit(0);
    return null;
  }

  @Override
  MenuState back() {
    menuController.enterMainMenu();
    return parentState;
  }
}
