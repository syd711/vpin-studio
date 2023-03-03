package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;

public class InstallConfirmationMenuState extends MenuState {
  private final MenuController menuController;

  public InstallConfirmationMenuState(MenuController menuController) {
    this.menuController = menuController;
    this.menuController.showTableInstallConfirmation();
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
    return new InstallSelectionMenuState(menuController);
  }
}
