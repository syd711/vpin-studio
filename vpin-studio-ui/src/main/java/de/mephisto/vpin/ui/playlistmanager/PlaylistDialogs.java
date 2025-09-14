package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class PlaylistDialogs {

  public static void openPlaylistManager(TableOverviewController tableOverviewController) {
    openPlaylistManager(tableOverviewController, null);
  }

  public static void openPlaylistManager(TableOverviewController tableOverviewController, PlaylistRepresentation selectedPlaylist) {
    Stage stage = Dialogs.createStudioDialogStage(PlaylistManagerController.class, "dialog-playlist-manager.fxml", "Playlist Manager", "playlistManager");
    PlaylistManagerController controller = (PlaylistManagerController) stage.getUserData();

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(1024);
    stage.setMinHeight(700);

    controller.setData(stage, tableOverviewController, selectedPlaylist);
    stage.showAndWait();
  }

  public static void openCreatePlaylistDialog(PlaylistManagerController playlistManagerController) {
    Stage stage = Dialogs.createStudioDialogStage(NewPlaylistController.class, "dialog-new-playlist.fxml", "New Playlist");
    NewPlaylistController controller = (NewPlaylistController) stage.getUserData();
    controller.setData(playlistManagerController);
    stage.showAndWait();
  }

  public static void openPlaylistTemplateDialog(PlaylistTableController playlistTableController, PlaylistRepresentation playlist) {
    Stage stage = Dialogs.createStudioDialogStage(PlaylistTemplatesController.class, "dialog-playlist-templates.fxml", "Playlist Templates");
    PlaylistTemplatesController controller = (PlaylistTemplatesController) stage.getUserData();
    controller.setData(playlistTableController, stage, playlist);
    stage.showAndWait();
  }
}
