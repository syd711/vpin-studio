package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.popper.Playlist;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;

import java.util.List;

public class InstallConfirmationMenuState extends MenuState {
  private final MenuState parentState;
  private final MenuController menuController;

  public InstallConfirmationMenuState(MenuState parentState,MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterTableInstallConfirmation();
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
    List<Playlist> playlists = Menu.client.getPlaylistsService().getStaticPlaylists();
    if(!playlists.isEmpty()) {
      return new PlaylistSelectionMenuState(this, menuController);
    }
    return new InstallingMenuState(this, menuController, null);
  }

  @Override
  MenuState back() {
    this.menuController.leaveConfirmation();
    return parentState;
  }
}
