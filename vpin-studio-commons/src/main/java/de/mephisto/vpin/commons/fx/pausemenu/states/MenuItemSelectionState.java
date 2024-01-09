package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;

public class MenuItemSelectionState extends MenuState{
  private final MenuController menuController;

  public MenuItemSelectionState(MenuController menuController) {
    this.menuController = menuController;
    menuController.enterMenuItemSelection();
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
    return this;
  }

  @Override
  MenuState back() {
    PauseMenu.exit();
    return null;
  }
}
