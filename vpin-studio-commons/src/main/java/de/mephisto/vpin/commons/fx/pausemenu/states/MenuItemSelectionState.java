package de.mephisto.vpin.commons.fx.pausemenu.states;


import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.UIDefaults;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemTypes;
import javafx.application.Platform;

public class MenuItemSelectionState extends MenuState {
  private final MenuController menuController;

  public MenuItemSelectionState(MenuController menuController) {
    this.menuController = menuController;
  }

  @Override
  MenuState left() {
    menuController.scrollGameBarLeft();
    checkAutoPlay();
    return this;
  }

  @Override
  MenuState right() {
    menuController.scrollGameBarRight();
    checkAutoPlay();
    return this;
  }

  @Override
  MenuState enter() {
    PauseMenuItem item = menuController.getSelection();
    if (item.getItemType().equals(PauseMenuItemTypes.exit)) {
      PauseMenu.exitPauseMenu();
    }
    else if (item.getYouTubeUrl() != null) {
      menuController.showYouTubeVideo(item);
    }
    return this;
  }

  @Override
  MenuState back() {
    PauseMenu.exitPauseMenu();
    return null;
  }

  private void checkAutoPlay() {
    if (menuController.getPauseMenuSettings().isAutoplay()) {
      PauseMenuItem item = menuController.getSelection();
      if (item.getYouTubeUrl() != null) {
        new Thread(() -> {
          try {
            Thread.sleep(UIDefaults.SELECTION_SCALE_DURATION * 2);
          } catch (InterruptedException e) {
            //ignore
          }
          Platform.runLater(() -> {
            menuController.showYouTubeVideo(item);
          });
        }).start();
      }
    }
  }
}
