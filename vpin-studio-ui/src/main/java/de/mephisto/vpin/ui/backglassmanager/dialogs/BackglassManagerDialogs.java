package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class BackglassManagerDialogs {

  public static void openResGenerator(int emulatorId, String fileName) {
    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, ResGeneratorDialogController.class, "dialog-res-generator.fxml", ".res File Generator", null);
    ResGeneratorDialogController controller = (ResGeneratorDialogController) stage.getUserData();
    controller.setData(stage, emulatorId, fileName);
    stage.showAndWait();
  }

  /**
   * @return true if .res files have been changed
   */
  public static boolean openResBulkEdit(int emulatorId) {
    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, ResBulkEditDialogController.class, "dialog-res-bulk-edit.fxml", Messages.get("backglass.res_bulk.title"), null);
    ResBulkEditDialogController controller = (ResBulkEditDialogController) stage.getUserData();
    controller.setData(stage, emulatorId);
    stage.showAndWait();
    return controller.isApplied();
  }

  public static void openBulkImport(int emulatorId, BackglassBulkImportType type) {
    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, BackglassBulkImportDialogController.class, "dialog-backglass-bulk-import.fxml", Messages.get("backglass.bulk_import"), null);
    BackglassBulkImportDialogController controller = (BackglassBulkImportDialogController) stage.getUserData();
    controller.setData(stage, emulatorId, type);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(600);
    stage.setMinHeight(400);
    stage.showAndWait();
  }
}
