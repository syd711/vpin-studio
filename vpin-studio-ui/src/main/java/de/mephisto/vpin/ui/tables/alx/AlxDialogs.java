package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.alx.dialogs.AlxDeleteStatsDialogController;
import de.mephisto.vpin.ui.tables.alx.dialogs.AlxUpdateStatsDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class AlxDialogs {
  public static void openDeleteAlxDialog() {
    Stage stage = Dialogs.createStudioDialogStage(AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", "Delete Table Statistics");
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(null);
    stage.showAndWait();
  }

  public static void openDeleteAlxDialog(Stage parentStage, GameRepresentation gameRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parentStage, AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", "Delete Table Statistic", null);
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }

  public static void openUpdateTableAlxDialog(Stage parentStage, GameRepresentation gameRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parentStage, AlxUpdateStatsDialogController.class, "dialog-alx-update.fxml", "Update Table Statistic", null);
    AlxUpdateStatsDialogController controller = (AlxUpdateStatsDialogController) stage.getUserData();
    controller.setData(gameRepresentation);
    stage.showAndWait();
  }
}
