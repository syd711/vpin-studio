package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentBrowserDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentEditDialogController;
import de.mephisto.vpin.ui.tournaments.dialogs.TournamentTableSelectorDialogController;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

public class TournamentDialogs {

  public static TournamentCreationModel openTournamentDialog(@NonNull String title, @NonNull Tournament tournament, boolean joinMode) {
    Stage stage = Dialogs.createStudioDialogStage(TournamentEditDialogController.class, "dialog-tournament-edit.fxml", title);
    TournamentEditDialogController controller = (TournamentEditDialogController) stage.getUserData();
    controller.setTournament(stage, tournament, joinMode);
    stage.showAndWait();

    return controller.getTournamentData();
  }

  public static Tournament openTournamentBrowserDialog() {
    Stage stage = Dialogs.createStudioDialogStage(TournamentBrowserDialogController.class, "dialog-tournament-browser.fxml", "Tournament Browser");
    TournamentBrowserDialogController controller = (TournamentBrowserDialogController) stage.getUserData();
    stage.showAndWait();

    return controller.getTournament();
  }

  public static TournamentTable openTableSelectionDialog(Stage parent, Tournament tournament, TournamentTable tournamentTable) {
    Stage stage = Dialogs.createStudioDialogStage(parent, TournamentTableSelectorDialogController.class, "dialog-tournament-table-selector.fxml", "Tournament - Table Selection", null);
    TournamentTableSelectorDialogController controller = (TournamentTableSelectorDialogController) stage.getUserData();
    controller.setTournamentTable(stage, tournament, tournamentTable);

    stage.showAndWait();

    return controller.getTournamentTable();
  }

}
