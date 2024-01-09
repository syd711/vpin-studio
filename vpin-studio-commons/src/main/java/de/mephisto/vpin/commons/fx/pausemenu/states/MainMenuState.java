package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;

public class MainMenuState extends MenuState {

  private final MenuController menuController;

  public MainMenuState(MenuController menuController) {
    this.menuController = menuController;
    this.menuController.enterMainMenu();
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
    menuController.resetGameRow();
    return new ArchiveSelectionMenuState(menuController);
  }

  @Override
  MenuState back() {
    return new ExitMenuState(this, menuController);
  }
}
