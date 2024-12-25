package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class PlaylistDialogs {

  public static void openPlaylistManager(TableOverviewController tableOverviewController) {
    Stage stage = Dialogs.createStudioDialogStage(PlaylistManagerController.class, "dialog-playlist-manager.fxml", "Playlist Manager", "playlistManager");
    PlaylistManagerController controller = (PlaylistManagerController) stage.getUserData();

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);
    stage.setMinWidth(1024);
    stage.setMinHeight(700);

    controller.setData(stage, tableOverviewController);
    stage.showAndWait();
  }
}
