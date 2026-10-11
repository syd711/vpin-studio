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

  public static void openBulkImport(int emulatorId) {
    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, BackglassBulkImportDialogController.class, "dialog-backglass-bulk-import.fxml", Messages.get("backglass.bulk_import"), null);
    BackglassBulkImportDialogController controller = (BackglassBulkImportDialogController) stage.getUserData();
    controller.setData(stage, emulatorId);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(600);
    stage.setMinHeight(400);
    stage.showAndWait();
  }
}
