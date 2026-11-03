package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.dialogs.*;
import javafx.stage.Stage;

import java.util.List;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class CompetitionDialogs {

  public static CompetitionRepresentation openOfflineCompetitionDialog(List<CompetitionRepresentation> all, CompetitionRepresentation selection) {
    String title = Messages.get("dialog.edit_competition");
    if (selection == null) {
      title = Messages.get("dialog.add_competition");
    }
    else if (selection.getId() == null) {
      title = Messages.get("dialog.duplicate_competition");
    }

    Stage stage = WidgetFactory.createDialogStage(CompetitionOfflineDialogController.class, Studio.stage, title, "dialog-offline-competition-edit.fxml");
    CompetitionOfflineDialogController controller = (CompetitionOfflineDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }
}
