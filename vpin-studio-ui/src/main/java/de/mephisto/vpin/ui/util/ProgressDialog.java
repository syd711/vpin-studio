package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.ProgressDialogController;
import javafx.stage.Stage;

public class ProgressDialog {
  public static <T> ProgressResultModel createProgressDialog(ProgressModel<T> model) {
    return createProgressDialog(null, model);
  }

  public static <T> ProgressResultModel createProgressDialog(Stage parentStage, ProgressModel<T> model) {
    Stage stage = null;
    if (parentStage == null) {
      stage = Dialogs.createStudioDialogStage("dialog-progress.fxml", model.getTitle());
    }
    else {
      stage = Dialogs.createStudioDialogStage(parentStage, ProgressDialogController.class, "dialog-progress.fxml", model.getTitle(), null);
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
