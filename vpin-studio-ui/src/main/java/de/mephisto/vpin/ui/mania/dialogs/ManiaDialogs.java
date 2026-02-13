package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ManiaDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaDialogs.class);

  public static ManiaRegistration openRegistrationDialog() {
    Stage stage = Dialogs.createStudioDialogStage(ManiaRegistrationDialogController.class, "dialog-mania-registration.fxml", "VPin Mania Registration");
    ManiaRegistrationDialogController controller = (ManiaRegistrationDialogController) stage.getUserData();
    stage.showAndWait();
    return controller.getManiaRegistration();
  }

  public static void openTableSyncResult(List<ManiaTableSyncResult> results) {
    Stage stage = Dialogs.createStudioDialogStage(ManiaTableSynchronizationDialogController.class, "dialog-mania-table-sync.fxml", "VPin Mania Synchronization Results");
    ManiaTableSynchronizationDialogController controller = (ManiaTableSynchronizationDialogController) stage.getUserData();
    controller.setSynchronizationResult(results);
    stage.showAndWait();
  }
}
