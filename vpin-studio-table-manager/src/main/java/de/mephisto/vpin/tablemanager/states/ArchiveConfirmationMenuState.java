package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.MenuController;

public class ArchiveConfirmationMenuState extends MenuState {
  private final MenuState parentState;
  private final MenuController menuController;

  public ArchiveConfirmationMenuState(MenuState parentState, MenuController menuController) {
    this.parentState = parentState;
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
    menuController.showProgress();
    return new ArchivingMenuState(this, menuController);
  }

  @Override
  MenuState back() {
    this.menuController.leaveConfirmation();
    return parentState;
  }
}
