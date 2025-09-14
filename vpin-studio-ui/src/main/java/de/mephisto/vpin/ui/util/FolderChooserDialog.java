package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.restclient.system.FolderRepresentation;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class FolderChooserDialog {
  private static String lastFolderSelection;

  public static FolderRepresentation open(@Nullable Stage stage, @Nullable String title, @Nullable String path, boolean forceLocal) {
    if (path == null && lastFolderSelection != null) {
      path = lastFolderSelection;
    }

    if (title == null) {
      title = "Choose Folder";
    }

    if (stage == null) {
      stage = Studio.stage;
    }

    if (forceLocal || client.getSystemService().isLocal()) {
      return openLocalFolderChooser(title, path);
    }
    else {
      Stage dialogStage = Dialogs.createStudioDialogStage(stage, FolderChooserDialogController.class, "dialog-folder-chooser.fxml", "Choose Folder", "folderChooser");
      FolderChooserDialogController controller = (FolderChooserDialogController) dialogStage.getUserData();
      controller.setPath(path);

      FXResizeHelper.install(dialogStage, 30, 6);
      dialogStage.setMinWidth(600);
      dialogStage.setMinHeight(550);

      dialogStage.showAndWait();

      FolderRepresentation selection = controller.getSelection();
      if (selection != null) {
        FolderChooserDialog.lastFolderSelection = selection.getPath();
      }
      return selection;
    }
  }

  public static FolderRepresentation open(@Nullable String path) {
    return open(Studio.stage, null, path, false);
  }

  @Nullable
  private static FolderRepresentation openLocalFolderChooser(String title, @Nullable String path) {
    DirectoryChooser chooser = new DirectoryChooser();
    if (path == null && FolderChooserDialog.lastFolderSelection != null) {
      path = FolderChooserDialog.lastFolderSelection;
    }

    if (path != null) {
      chooser.setInitialDirectory(new File(path));
    }

    chooser.setTitle(title);
    File targetFolder = chooser.showDialog(stage);
    if (targetFolder != null) {
      FolderRepresentation selection = new FolderRepresentation(targetFolder);
      FolderChooserDialog.lastFolderSelection = targetFolder.getAbsolutePath();
      return selection;
    }

    return null;
  }
}
