package de.mephisto.vpin.ui.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZDescriptorRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.vpxz.dialogs.*;
import de.mephisto.vpin.ui.vpxz.dialogs.TablesVPXZDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class VPXZDialogs {

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesVPXZDialogController.class, "dialog-tables-vpxmobile.fxml", "Table Backup");
    TablesVPXZDialogController controller = (TablesVPXZDialogController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static VPXZSourceRepresentation openArchiveSourceHttpDialog(VPXZSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZSourceHttpDialogController.class, "dialog-vpxmobile-source-http.fxml", "HTTP Repository");
    VPXZSourceHttpDialogController controller = (VPXZSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static VPXZSourceRepresentation openArchiveSourceFolderDialog(VPXZSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZSourceFolderDialogController.class, "dialog-vpxmobile-source-folder.fxml", "Backup Folder");
    VPXZSourceFolderDialogController controller = (VPXZSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static boolean openArchiveUploadDialog() {
    return openArchiveUploadDialog(null);
  }

  public static boolean openArchiveUploadDialog(@Nullable File file) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZUploadController.class, "dialog-vpxmobile-upload.fxml", "Upload");
    VPXZUploadController controller = (VPXZUploadController) stage.getUserData();
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveDownloadDialog(List<VPXZDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZDownloadDialogController.class, "dialog-vpxmobile-download.fxml", "Archive Download");
    VPXZDownloadDialogController controller = (VPXZDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

}
