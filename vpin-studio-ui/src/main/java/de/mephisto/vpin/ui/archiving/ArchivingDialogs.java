package de.mephisto.vpin.ui.archiving;

import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.archiving.dialogs.*;
import de.mephisto.vpin.ui.archiving.dialogs.TablesBackupDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class ArchivingDialogs {

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesBackupDialogController.class, "dialog-tables-backup.fxml", "Table Backup");
    TablesBackupDialogController controller = (TablesBackupDialogController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static ArchiveSourceRepresentation openArchiveSourceHttpDialog(ArchiveSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveSourceHttpDialogController.class, "dialog-archive-source-http.fxml", "HTTP Repository");
    ArchiveSourceHttpDialogController controller = (ArchiveSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static ArchiveSourceRepresentation openArchiveSourceFolderDialog(ArchiveSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveSourceFolderDialogController.class, "dialog-archive-source-folder.fxml", "Folder Repository");
    ArchiveSourceFolderDialogController controller = (ArchiveSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static boolean openArchiveUploadDialog() {
    return openArchiveUploadDialog(null);
  }

  public static boolean openArchiveUploadDialog(@Nullable File file) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveUploadController.class, "dialog-archive-upload.fxml", "Upload");
    ArchiveUploadController controller = (ArchiveUploadController) stage.getUserData();
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveRestoreDialog(List<ArchiveDescriptorRepresentation> archives) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveRestoreDialogController.class, "dialog-archive-restore.fxml", "Restore");
    ArchiveRestoreDialogController controller = (ArchiveRestoreDialogController) stage.getUserData();
    controller.setData(archives);
    stage.showAndWait();
  }

  public static void openArchiveDownloadDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveDownloadDialogController.class, "dialog-archive-download.fxml", "Archive Download");
    ArchiveDownloadDialogController controller = (ArchiveDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

}
