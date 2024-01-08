package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.restclient.games.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ArchivingMenuState extends MenuState {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  private final MenuState parentState;
  private int mode;
  private final MenuController menuController;

  public ArchivingMenuState(MenuState parentState, int mode, MenuController menuController) {
    this.parentState = parentState;
    this.mode = mode;
    this.menuController = menuController;
    this.menuController.enterArchiving();
    this.menuController.setNameLabelText("Creating Backup, please wait...");

    StateMananger.getInstance().setInputBlocked(true);
    executeArchiving();
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

  private void executeArchiving() {
    Platform.runLater(() -> {
      this.menuController.showProgressbar();
    });
    new Thread(() -> {
      GameRepresentation game = this.menuController.getGameSelection();

      BackupDescriptor descriptor = new BackupDescriptor();
      descriptor.getGameIds().add(game.getId());
      descriptor.setRemoveFromPlaylists(mode == 0);
      try {
        Menu.client.getArchiveService().backupTable(descriptor);
      } catch (Exception e) {
        LOG.error("Failed to executing archiving: " + e.getMessage(), e);
      }

      StateMananger.getInstance().waitForJobAndGoBack();
    }).start();

  }
}
