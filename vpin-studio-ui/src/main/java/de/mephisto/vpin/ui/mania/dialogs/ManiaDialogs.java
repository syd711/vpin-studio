package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ManiaDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaDialogs.class);

  public static ManiaRegistration openRegistrationDialog(String apiKey) {
    Stage stage = Dialogs.createStudioDialogStage(ManiaRegistrationDialogController.class, "dialog-mania-registration.fxml", Messages.get("mania.mania_registration.vpin_mania_registration"));
    ManiaRegistrationDialogController controller = (ManiaRegistrationDialogController) stage.getUserData();
    controller.setApiKey(apiKey);
    stage.showAndWait();
    return controller.getManiaRegistration();
  }

  public static void openTableSyncResult(List<ManiaTableSyncResult> results) {
    Stage stage = Dialogs.createStudioDialogStage(ManiaTableSynchronizationDialogController.class, "dialog-mania-table-sync.fxml", Messages.get("dialog.mania_sync_results"));
    ManiaTableSynchronizationDialogController controller = (ManiaTableSynchronizationDialogController) stage.getUserData();
    controller.setSynchronizationResult(results);
    stage.showAndWait();
  }
}
