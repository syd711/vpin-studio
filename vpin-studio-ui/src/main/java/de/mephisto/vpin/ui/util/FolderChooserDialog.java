package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class FolderChooserDialog {
  private static File lastFolderSelection;

  public static FolderRepresentation open(@Nullable String path) {
    if (path == null && lastFolderSelection != null) {
      path = lastFolderSelection.getAbsolutePath();
    }

    if (!client.getSystemService().isLocal()) {
      DirectoryChooser chooser = new DirectoryChooser();
      if (FolderChooserDialog.lastFolderSelection != null) {
        chooser.setInitialDirectory(FolderChooserDialog.lastFolderSelection);
      }
      chooser.setTitle("Chooser Folder");
      File targetFolder = chooser.showDialog(stage);
      if (targetFolder != null) {
        FolderRepresentation selection = new FolderRepresentation(targetFolder);
        FolderChooserDialog.lastFolderSelection = targetFolder;
        return selection;
      }
    }
    else {
      Stage stage = Dialogs.createStudioDialogStage(FolderChooserDialogController.class, "dialog-folder-chooser.fxml", "Choose Folder", "folderChooser");
      FolderChooserDialogController controller = (FolderChooserDialogController) stage.getUserData();
      controller.setPath(path);

      FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
      stage.setUserData(fxResizeHelper);
      stage.setMinWidth(600);
      stage.setMinHeight(550);

      stage.showAndWait();

      return controller.getSelection();
    }

    return null;
  }
}
