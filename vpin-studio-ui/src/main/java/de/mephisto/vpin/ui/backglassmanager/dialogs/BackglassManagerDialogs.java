package de.mephisto.vpin.ui.backglassmanager.dialogs;

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
}
