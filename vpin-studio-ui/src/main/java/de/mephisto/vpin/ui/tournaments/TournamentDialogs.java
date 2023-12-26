package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.preferences.ManiaAccountDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentManiaDialogController;
import de.mephisto.vpin.ui.vps.VPSTableSelectorDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.vps.VpsSelection;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.stage.Stage;

public class TournamentDialogs {

  public static void openManiaAccountDialog(String title, ManiaAccountRepresentation accountRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(ManiaAccountDialogController.class, "preference-account-dialog.fxml", title);
    ManiaAccountDialogController controller = (ManiaAccountDialogController) stage.getUserData();
    controller.setAccount(accountRepresentation);
    stage.showAndWait();
  }

  public static ManiaTournamentRepresentation openTournamentDialog(@NonNull String title, @NonNull ManiaTournamentRepresentation tournamentRepresentation) {
    Stage stage = Dialogs.createStudioDialogStage(TournamentManiaDialogController.class, "dialog-tournament-edit.fxml", title);
    TournamentManiaDialogController controller = (TournamentManiaDialogController) stage.getUserData();
    controller.setTournament(tournamentRepresentation);
    stage.showAndWait();

    return controller.getTournament();
  }

  public static VpsSelection openTableSelectionDialog(Stage parent) {
    Stage stage = Dialogs.createStudioDialogStage(parent, VPSTableSelectorDialogController.class, "dialog-vps-table-selector.fxml", "Virtual Pinball Spreadsheet - Table Selection");
    VPSTableSelectorDialogController controller = (VPSTableSelectorDialogController) stage.getUserData();
    stage.showAndWait();

    return controller.getSelection();
  }

}
