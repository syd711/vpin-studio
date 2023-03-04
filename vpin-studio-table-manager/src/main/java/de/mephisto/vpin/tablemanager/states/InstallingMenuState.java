package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class InstallingMenuState extends MenuState {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private final MenuState parentState;
  private final MenuController menuController;

  public InstallingMenuState(MenuState parentState, MenuController menuController) {
    this.parentState = parentState;
    this.menuController = menuController;
    this.menuController.enterInstalling();

    StateMananger.getInstance().setInputBlocked(true);
    executeInstallation();
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
    menuController.leaveConfirmation();
    return parentState.back();
  }

  private void executeInstallation() {
    new Thread(() -> {
      VpaDescriptorRepresentation game = this.menuController.getVpaSelection();
      VpaManifest manifest = game.getManifest();

      ImportDescriptor descriptor = new ImportDescriptor();
      descriptor.setImportRom(true);
      descriptor.setImportPupPack(true);
      descriptor.setImportPopperMedia(true);
      descriptor.setImportHighscores(true);
      descriptor.setUuid(manifest.getUuid());

      //TODO provide playlist selector
      List<PlaylistRepresentation> playlists = Menu.client.getPlaylists();
      if(!playlists.isEmpty()) {
        descriptor.setPlaylistId(playlists.get(0).getId());
      }
      try {
        Menu.client.importVpa(descriptor);
      } catch (Exception e) {
        LOG.error("Failed to executing installation: " + e.getMessage(), e);
      }

      StateMananger.getInstance().waitForJobAndGoBack();
    }).start();

  }
}
