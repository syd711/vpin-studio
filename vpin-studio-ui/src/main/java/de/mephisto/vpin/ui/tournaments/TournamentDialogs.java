package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentBrowserDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentEditDialogController;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.vps.dialogs.VPSTableSelectorDialogController;
import de.mephisto.vpin.ui.vps.containers.VpsSelection;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public class TournamentDialogs {

  public static TreeItem<TournamentTreeModel> openTournamentDialog(@NonNull String title, @NonNull Tournament tournament) {
    Stage stage = Dialogs.createStudioDialogStage(TournamentEditDialogController.class, "dialog-tournament-edit.fxml", title);
    TournamentEditDialogController controller = (TournamentEditDialogController) stage.getUserData();
    controller.setTournament(tournament);
    stage.showAndWait();

    return controller.getTournament();
  }

  public static Tournament openTournamentBrowserDialog() {
    Stage stage = Dialogs.createStudioDialogStage(TournamentBrowserDialogController.class, "dialog-tournament-browser.fxml", "Tournament Browser");
    TournamentBrowserDialogController controller = (TournamentBrowserDialogController) stage.getUserData();
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
