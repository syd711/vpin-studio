package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.alx.dialogs.AlxDeleteStatsDialogController;
import de.mephisto.vpin.ui.tables.alx.dialogs.AlxUpdateStatsDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class AlxDialogs {
  public static void openDeleteAlxDialog() {
    Stage stage = Dialogs.createStudioDialogStage(AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", Messages.get("dialog.delete_table_statistics"));
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(null);
    stage.showAndWait();
  }

  public static void openDeleteAlxDialog(Stage parentStage, GameRepresentation gameRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parentStage, AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", Messages.get("dialog.delete_table_statistic"), null);
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }

  public static void openUpdateTableAlxDialog(Stage parentStage, GameRepresentation gameRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parentStage, AlxUpdateStatsDialogController.class, "dialog-alx-update.fxml", Messages.get("dialog.update_table_statistic"), null);
    AlxUpdateStatsDialogController controller = (AlxUpdateStatsDialogController) stage.getUserData();
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }
}
