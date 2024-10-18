package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class BackglassManagerDialogs {

  public static void openResGenerator(DirectB2S selection) {
    Stage stage = Dialogs.createStudioDialogStage(Studio.stage, ResGeneratorDialogController.class, "dialog-res-generator.fxml", ".res File Generator");
    ResGeneratorDialogController controller = (ResGeneratorDialogController) stage.getUserData();
    controller.setData(stage, selection);
    stage.showAndWait();
  }
}
