package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.dialogs.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;

public class CompetitionDialogs {

  public static CompetitionRepresentation openDiscordJoinCompetitionDialog() {
    String title = "Join Competition";
    Stage stage = WidgetFactory.createDialogStage(CompetitionOfflineDialogController.class, Studio.stage, title, "dialog-discord-competition-join.fxml");
    CompetitionDiscordJoinDialogController controller = (CompetitionDiscordJoinDialogController) stage.getUserData();
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openDiscordCompetitionDialog(List<CompetitionRepresentation> all, @Nullable CompetitionRepresentation selection) {
    String title = "Edit Competition";
    if (selection == null) {
      title = "Add Competition";
    }
    else if (selection.getId() == null) {
      title = "Duplicate Competition";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionDiscordDialogController.class.getResource("dialog-discord-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(CompetitionDiscordDialogController.class, Studio.stage, title, "dialog-discord-competition-edit.fxml");
    CompetitionDiscordDialogController controller = (CompetitionDiscordDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openSubscriptionDialog(List<CompetitionRepresentation> all, @Nullable CompetitionRepresentation selection) {
    String title = "Add Subscription";
    Stage stage = WidgetFactory.createDialogStage(SubscriptionDialogController.class, Studio.stage, title, "dialog-subscription-add.fxml");
    SubscriptionDialogController controller = (SubscriptionDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openJoinSubscriptionDialog(List<CompetitionRepresentation> all) {
    String title = "Join Subscription";
    Stage stage = WidgetFactory.createDialogStage(JoinSubscriptionDialogController.class, Studio.stage, title, "dialog-subscription-join.fxml");
    JoinSubscriptionDialogController controller = (JoinSubscriptionDialogController) stage.getUserData();
    controller.setCompetition(all);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openOfflineCompetitionDialog(List<CompetitionRepresentation> all, CompetitionRepresentation selection) {
    String title = "Edit Competition";
    if (selection == null) {
      title = "Add Competition";
    }
    else if (selection.getId() == null) {
      title = "Duplicate Competition";
    }

    Stage stage = WidgetFactory.createDialogStage(CompetitionOfflineDialogController.class, Studio.stage, title, "dialog-offline-competition-edit.fxml");
    CompetitionOfflineDialogController controller = (CompetitionOfflineDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }
}
