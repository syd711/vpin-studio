package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.preferences.ManiaAccountDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentManiaDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.VPSTableSelectorDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

import java.util.List;

public class TournamentDialogs {

  public static void openManiaAccountDialog(String title, ManiaAccountRepresentation accountRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(ManiaAccountDialogController.class, "preference-account-dialog.fxml", title);
    ManiaAccountDialogController controller = (ManiaAccountDialogController) stage.getUserData();
    controller.setAccount(accountRepresentation);
    stage.showAndWait();
  }

  public static ManiaTournamentRepresentation openTournamentDialog(String title, ManiaTournamentRepresentation tournamentRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(TournamentManiaDialogController.class, "dialog-tournament-edit.fxml", title);
    TournamentManiaDialogController controller = (TournamentManiaDialogController) stage.getUserData();
    controller.setTournament(tournamentRepresentation);
    stage.showAndWait();

    return controller.getTournament();
  }

  public static List<VpsTableVersion> openTableSelectionDialog(Stage parent, String title, ManiaTournamentRepresentation tournamentRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(parent, VPSTableSelectorDialogController.class, "dialog-vps-table-selector.fxml", title);
    VPSTableSelectorDialogController controller = (VPSTableSelectorDialogController) stage.getUserData();
    controller.setTournament(tournamentRepresentation);
    stage.showAndWait();

    return controller.getSelection();
  }

}
