package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.backups.dialogs.*;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

public class BackupDialogs {

  public static void openTablesBackupDialog(List<GameRepresentation> games) {
    Stage stage = Dialogs.createStudioDialogStage(TablesBackupDialogController.class, "dialog-tables-backup.fxml", Messages.get("dialog.table_backup"));
    TablesBackupDialogController controller = (TablesBackupDialogController) stage.getUserData();
    controller.setGames(games);
    stage.showAndWait();
  }

  public static BackupSourceRepresentation openArchiveSourceHttpDialog(BackupSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(BackupSourceHttpDialogController.class, "dialog-backup-source-http.fxml", Messages.get("dialog.http_repository"));
    BackupSourceHttpDialogController controller = (BackupSourceHttpDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();

    return controller.getArchiveSource();
  }

  public static BackupSourceRepresentation openArchiveSourceFolderDialog(BackupSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(BackupSourceFolderDialogController.class, "dialog-backup-source-folder.fxml", Messages.get("dialog.backup_folder"));
    BackupSourceFolderDialogController controller = (BackupSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getArchiveSource();
  }

  public static boolean openArchiveUploadDialog() {
    return openArchiveUploadDialog(null);
  }

  public static boolean openArchiveUploadDialog(@Nullable File file) {
    Stage stage = Dialogs.createStudioDialogStage(BackupUploadController.class, "dialog-backup-upload.fxml", Messages.get("dialog.upload"));
    BackupUploadController controller = (BackupUploadController) stage.getUserData();
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveRestoreDialog(List<BackupDescriptorRepresentation> archives) {
    Stage stage = Dialogs.createStudioDialogStage(BackupRestoreDialogController.class, "dialog-backup-restore.fxml", Messages.get("dialog.restore"));
    BackupRestoreDialogController controller = (BackupRestoreDialogController) stage.getUserData();
    controller.setData(archives);
    stage.showAndWait();
  }

  public static void openArchiveDownloadDialog(List<BackupDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(BackupDownloadDialogController.class, "dialog-backup-download.fxml", Messages.get("dialog.archive_download"));
    BackupDownloadDialogController controller = (BackupDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

}
