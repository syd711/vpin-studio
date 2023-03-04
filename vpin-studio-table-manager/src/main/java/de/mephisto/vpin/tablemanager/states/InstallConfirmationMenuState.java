package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.MenuController;

public class InstallConfirmationMenuState extends MenuState {
  private final MenuState parentState;
  private final MenuController menuController;

  public InstallConfirmationMenuState(MenuState parentState,MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterTableInstallConfirmation();
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
    return this;
  }

  @Override
  MenuState back() {
    this.menuController.leaveConfirmation();
    return parentState;
  }
}
