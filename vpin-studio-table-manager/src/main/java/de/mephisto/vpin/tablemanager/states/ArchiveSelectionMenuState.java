package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.MenuController;

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
    menuController.enterMainWithArchive();
    return new MainMenuState(menuController);
  }
}
