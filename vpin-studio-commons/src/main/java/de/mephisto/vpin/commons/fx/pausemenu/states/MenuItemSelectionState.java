package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemTypes;

public class MenuItemSelectionState extends MenuState {
  private final MenuController menuController;

  public MenuItemSelectionState(MenuController menuController) {
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
    PauseMenuItem item = menuController.getSelection();
    if (item.getItemType().equals(PauseMenuItemTypes.exit)) {
      PauseMenu.getInstance().exitPauseMenu();
    }
    else {
      menuController.enter();
    }
    return this;
  }

  @Override
  MenuState back() {
    PauseMenu.getInstance().exitPauseMenu();
    return null;
  }
}
