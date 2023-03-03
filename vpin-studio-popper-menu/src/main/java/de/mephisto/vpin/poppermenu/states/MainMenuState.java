package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;

public class MainMenuState extends MenuState {

  private final MenuController menuController;

  public MainMenuState(MenuController menuController) {
    this.menuController = menuController;
  }

  @Override
  MenuState left() {
    menuController.toggleInstall();
    return this;
  }

  @Override
  MenuState right() {
    menuController.toggleInstall();
    return this;
  }

  @Override
  MenuState enter() {
    boolean install = menuController.isInstallSelected();
    if (install) {
      menuController.resetGameRow();
      return new InstallSelectionMenuState(menuController);
    }
    menuController.resetGameRow();
    return new ArchiveSelectionMenuState(menuController);
  }

  @Override
  MenuState back() {
    System.exit(0);
    return null;
  }
}
