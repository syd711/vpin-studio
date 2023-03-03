package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;

public class InstallSelectionMenuState extends MenuState{
  private final MenuController menuController;

  public InstallSelectionMenuState(MenuController menuController) {
    this.menuController = menuController;
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
    return null;
  }

  @Override
  MenuState back() {
    menuController.enterMainWithInstall();
    return new MainMenuState(menuController);
  }
}
