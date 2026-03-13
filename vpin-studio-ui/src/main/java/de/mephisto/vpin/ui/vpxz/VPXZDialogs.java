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

  public static void openTablesVpxzDialog(GameRepresentation game) {
    Stage stage = Dialogs.createStudioDialogStage(TablesVPXZDialogController.class, "dialog-tables-vpxz.fxml", "VPXZ Table Packager");
    TablesVPXZDialogController controller = (TablesVPXZDialogController) stage.getUserData();
    controller.setGames(game);
    stage.showAndWait();
  }

//  public static VPXZSourceRepresentation openArchiveSourceHttpDialog(VPXZSourceRepresentation source) {
//    Stage stage = Dialogs.createStudioDialogStage(VPXZSourceHttpDialogController.class, "dialog-vpxmobile-source-http.fxml", "HTTP Repository");
//    VPXZSourceHttpDialogController controller = (VPXZSourceHttpDialogController) stage.getUserData();
//    controller.setSource(source);
//    stage.showAndWait();
//
//    return controller.getArchiveSource();
//  }

  public static VPXZSourceRepresentation openVpxzSourceFolderDialog(VPXZSourceRepresentation source) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZSourceFolderDialogController.class, "dialog-vpxz-source-folder.fxml", "VPXZ Folder");
    VPXZSourceFolderDialogController controller = (VPXZSourceFolderDialogController) stage.getUserData();
    controller.setSource(source);
    stage.showAndWait();
    return controller.getVpxzSource();
  }

  public static boolean openVpxzUploadDialog() {
    return openVpxzUploadDialog(null);
  }

  public static boolean openVpxzUploadDialog(@Nullable File file) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZUploadController.class, "dialog-vpxz-upload.fxml", "Upload");
    VPXZUploadController controller = (VPXZUploadController) stage.getUserData();
    controller.setFile(file);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openVpxzDownloadDialog(List<VPXZDescriptorRepresentation> selectedItems) {
    Stage stage = Dialogs.createStudioDialogStage(VPXZDownloadDialogController.class, "dialog-vpxz-download.fxml", "VPXZ Download");
    VPXZDownloadDialogController controller = (VPXZDownloadDialogController) stage.getUserData();
    controller.setData(selectedItems);
    stage.showAndWait();
  }

}
