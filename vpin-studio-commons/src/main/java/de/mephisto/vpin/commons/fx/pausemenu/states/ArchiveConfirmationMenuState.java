package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;

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
    return new ArchiveOptionsMenuState(this, menuController);
  }

  @Override
  MenuState back() {
    this.menuController.leaveConfirmation();
    return parentState;
  }
}
