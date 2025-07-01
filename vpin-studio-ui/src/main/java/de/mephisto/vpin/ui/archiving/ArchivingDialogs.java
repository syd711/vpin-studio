package de.mephisto.vpin.ui.archiving;

import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.ui.archiving.dialogs.*;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

public class ArchivingDialogs {

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
    Stage stage = Dialogs.createStudioDialogStage(ArchiveUploadController.class, "dialog-archive-upload.fxml", "Upload");
    ArchiveUploadController controller = (ArchiveUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openArchiveDownloadDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(ArchiveDownloadDialogController.class, "dialog-archive-download.fxml", "Archive Download");
    ArchiveDownloadDialogController controller = (ArchiveDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openVpaArchiveBundleDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(VpaArchiveBundleDialogController.class, "dialog-vpa-bundle-download.fxml", "Archive Bundle");
    VpaArchiveBundleDialogController controller = (VpaArchiveBundleDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

  public static void openCopyArchiveToRepositoryDialog(ObservableList<ArchiveDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(CopyArchiveToRepositoryDialogController.class, "dialog-copy-archive-to-repository.fxml", "Copy To Repository");
    CopyArchiveToRepositoryDialogController controller = (CopyArchiveToRepositoryDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }
}
