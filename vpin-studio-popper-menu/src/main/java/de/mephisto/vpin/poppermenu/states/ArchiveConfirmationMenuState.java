package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;

public class ArchiveConfirmationMenuState extends MenuState {
  private final MenuController menuController;

  public ArchiveConfirmationMenuState(MenuController menuController) {
    this.menuController = menuController;
    this.menuController.enterArchiveInstallConfirmation();
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
    return new ArchiveSelectionMenuState(menuController);
  }
}
