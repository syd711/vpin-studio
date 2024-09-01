package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.alx.dialogs.AlxDeleteStatsDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class AlxDialogs {
  public static void openTableDeleteDialog(AlxController alxController) {
    Stage stage = Dialogs.createStudioDialogStage(AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", "Delete Table Statistics");
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(alxController, null);
    stage.showAndWait();
  }

  public static void openTableDeleteDialog(Stage parentStage, GameRepresentation gameRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parentStage, AlxDeleteStatsDialogController.class, "dialog-alx-delete.fxml", "Delete Table Statistic");
    AlxDeleteStatsDialogController controller = (AlxDeleteStatsDialogController) stage.getUserData();
    controller.setData(null, gameRepresentation);
    stage.showAndWait();
  }
}
