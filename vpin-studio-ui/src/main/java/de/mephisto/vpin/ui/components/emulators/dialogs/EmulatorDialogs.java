package de.mephisto.vpin.ui.components.emulators.dialogs;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class EmulatorDialogs {
  public static GameEmulatorRepresentation openNewEmulatorDialog() {
    Stage stage = Dialogs.createStudioDialogStage(DialogNewEmulatorController.class, "dialog-new-emulator.fxml", "New Emulator");
    DialogNewEmulatorController controller = (DialogNewEmulatorController) stage.getUserData();
    stage.showAndWait();

    return controller.getValidatedEmulator();
  }
}
