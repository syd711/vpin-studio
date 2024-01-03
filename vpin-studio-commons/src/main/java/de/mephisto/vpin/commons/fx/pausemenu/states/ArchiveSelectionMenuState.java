package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;

public class ArchiveSelectionMenuState extends MenuState{
  private final MenuController menuController;

  public ArchiveSelectionMenuState(MenuController menuController) {
    this.menuController = menuController;
    menuController.enterArchive();
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
    return new ArchiveConfirmationMenuState(this, menuController);
  }

  @Override
  MenuState back() {
    menuController.enterMainMenu();
    return new MainMenuState(menuController);
  }
}
