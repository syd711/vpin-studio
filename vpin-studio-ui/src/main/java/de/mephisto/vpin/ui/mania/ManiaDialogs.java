package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.mania.dialogs.VPSTableSearchDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class ManiaDialogs {

  public static VpsTable openVPSTableSearchDialog() {
    Stage stage = Dialogs.createStudioDialogStage(VPSTableSearchDialogController.class, "dialog-vps-table-search.fxml", "Virtual Pinball Spreadsheet Search");
    VPSTableSearchDialogController controller = (VPSTableSearchDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
    return controller.getSelection();
  }
}