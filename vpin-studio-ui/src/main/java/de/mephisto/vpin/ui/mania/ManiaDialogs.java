package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.DeniedScore;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.mania.dialogs.AccountSearchDialogController;
import de.mephisto.vpin.ui.mania.dialogs.VPSTableSearchDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;
import static de.mephisto.vpin.ui.Studio.stage;

public class ManiaDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaDialogs.class);

  public static VpsTable openVPSTableSearchDialog() {
    Stage stage = Dialogs.createStudioDialogStage(VPSTableSearchDialogController.class, "dialog-vps-table-search.fxml", "Virtual Pinball Spreadsheet Search");
    VPSTableSearchDialogController controller = (VPSTableSearchDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
    return controller.getSelection();
  }

  public static Account openAccountSearchDialog() {
    Stage stage = Dialogs.createStudioDialogStage(AccountSearchDialogController.class, "dialog-account-search.fxml", "VPin Mania Player Search");
    AccountSearchDialogController controller = (AccountSearchDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
    return controller.getSelection();
  }

  public static boolean openDenyListDialog(List<DeniedScore> deniedScore) {
    if (deniedScore != null && ManiaPermissions.getAccount() != null) {
      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithCheckbox(stage, "Deny List", null,
          "Add To Deny List", "Add the selected score(s) to the deny list?",
          "Adding the highscore to the deny list will also prohibit any future submission of the score for all users with the given initials and value.",
          "Understood, add the highscore to the deny list",
          true);
      if (confirmationResult.isOkClicked()) {
        try {
          for (DeniedScore score : deniedScore) {
            maniaClient.getHighscoreClient().addToDenyList(score);
          }
          return true;
        }
        catch (Exception e) {
          LOG.error("Failed to add score to deny list: {}", e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, "Error", "Failed to add score to deny list: " + e.getMessage());
          });
        }
      }
    }
    return false;
  }
}
