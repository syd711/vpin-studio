package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.MenuController;

public class InstallSelectionMenuState extends MenuState{
  private final MenuController menuController;

  public InstallSelectionMenuState(MenuController menuController) {
    this.menuController = menuController;
    menuController.enterInstall();
  }

  @Override
  MenuState left() {
    menuController.scrollGameBarLeft();
    return this;
  }

  @Override
  MenuState right() {
    menuController.scrollGameBarRight();
    return this;
  }

  @Override
  MenuState enter() {
    return new InstallConfirmationMenuState(this, menuController);
  }

  @Override
  MenuState back() {
    menuController.enterMainMenu();
    return new MainMenuState(menuController);
  }
}
