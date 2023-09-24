package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.popper.PlaylistRepresentation;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class PlaylistSelectionMenuState extends MenuState {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private final MenuState parentState;
  private final MenuController menuController;
  private final List<PlaylistRepresentation> playlists;
  private int index = 0;

  public PlaylistSelectionMenuState(MenuState parentState, MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterPlaylistSelection();
    playlists = new ArrayList<>(Menu.client.getPlaylistsService().getStaticPlaylists());
    playlists.add(null);

    this.menuController.setNameLabelText(playlists.get(index).getName());
  }

  @Override
  MenuState left() {
    if (index > 0) {
      index--;
    }
    else {
      index = playlists.size() - 1;
    }

    PlaylistRepresentation playlistRepresentation = playlists.get(index);
    if(playlistRepresentation == null) {
      this.menuController.setArrowsVisible(false);
      this.menuController.setNameLabelText("Skip Playlist Selection");
    }
    else {
      this.menuController.setArrowsVisible(true);
      this.menuController.setNameLabelText(playlistRepresentation.getName());
    }
    return this;
  }

  @Override
  MenuState right() {
    if (index == playlists.size() - 1) {
      index = 0;
    }
    else {
      index++;
    }

    PlaylistRepresentation playlistRepresentation = playlists.get(index);
    if(playlistRepresentation == null) {
      this.menuController.setArrowsVisible(false);
      this.menuController.setNameLabelText("Skip Playlist Selection");
    }
    else {
      this.menuController.setArrowsVisible(true);
      this.menuController.setNameLabelText(playlistRepresentation.getName());
    }
    return this;
  }

  @Override
  MenuState enter() {
    return new InstallingMenuState(this, menuController, this.playlists.get(index));
  }

  @Override
  MenuState back() {
    menuController.leaveInstallSubSelection();
    return parentState.back();
  }
}
