package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.ProgressDialogController;
import javafx.stage.Stage;

public class ProgressDialog {
  public static ProgressResultModel createProgressDialog(ProgressModel model) {
    return createProgressDialog(null, model);
  }

  public static ProgressResultModel createProgressDialog(Stage parentStage, ProgressModel model) {
    Stage stage = null;
    if (parentStage == null) {
      stage = Dialogs.createStudioDialogStage("dialog-progress.fxml", model.getTitle());
    }
    else {
      stage = Dialogs.createStudioDialogStage(parentStage, ProgressDialogController.class, "dialog-progress.fxml", model.getTitle());
    }
    stage.setAlwaysOnTop(true);
    ProgressDialogController controller = (ProgressDialogController) stage.getUserData();
    controller.setProgressModel(stage, model);
    stage.showAndWait();
    ProgressResultModel progressResult = controller.getProgressResult();
    stage.close();
    return progressResult;
  }
}
