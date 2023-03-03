package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;

public class InstallSelectionMenuState extends MenuState{
  private final MenuController menuController;

  public InstallSelectionMenuState(MenuController menuController) {
    this.menuController = menuController;
  }

  @Override
  MenuState left() {
    return null;
  }

  @Override
  MenuState right() {
    return null;
  }

  @Override
  MenuState enter() {
    return null;
  }

  @Override
  MenuState back() {
    return null;
  }
}
