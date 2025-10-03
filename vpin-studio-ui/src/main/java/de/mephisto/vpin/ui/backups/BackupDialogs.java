package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.backups.dialogs.*;
import de.mephisto.vpin.ui.backups.dialogs.TablesBackupDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class BackupDialogs {

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesBackupDialogController.class, "dialog-tables-backup.fxml", "Table Backup");
    TablesBackupDialogController controller = (TablesBackupDialogController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static BackupSourceRepresentation openArchiveSourceHttpDialog(BackupSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(BackupSourceHttpDialogController.class, "dialog-backup-source-http.fxml", "HTTP Repository");
    BackupSourceHttpDialogController controller = (BackupSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static BackupSourceRepresentation openArchiveSourceFolderDialog(BackupSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(BackupSourceFolderDialogController.class, "dialog-backup-source-folder.fxml", "Backup Folder");
    BackupSourceFolderDialogController controller = (BackupSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static boolean openArchiveUploadDialog() {
    return openArchiveUploadDialog(null);
  }

  public static boolean openArchiveUploadDialog(@Nullable File file) {
    Stage stage = Dialogs.createStudioDialogStage(BackupUploadController.class, "dialog-backup-upload.fxml", "Upload");
    BackupUploadController controller = (BackupUploadController) stage.getUserData();
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveRestoreDialog(List<BackupDescriptorRepresentation> archives) {
    Stage stage = Dialogs.createStudioDialogStage(BackupRestoreDialogController.class, "dialog-backup-restore.fxml", "Restore");
    BackupRestoreDialogController controller = (BackupRestoreDialogController) stage.getUserData();
    controller.setData(archives);
    stage.showAndWait();
  }

  public static void openArchiveDownloadDialog(List<BackupDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(BackupDownloadDialogController.class, "dialog-backup-download.fxml", "Archive Download");
    BackupDownloadDialogController controller = (BackupDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

}
