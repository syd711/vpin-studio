package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.dialogs.*;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;

public class CompetitionDialogs {

  public static void openIScoredInfoDialog(Stage s, GameRoom gameRoom) {
    FXMLLoader fxmlLoader = new FXMLLoader(IScoredInfoDialogController.class.getResource("dialog-iscored-info.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, "Game Room Info");
    IScoredInfoDialogController controller = (IScoredInfoDialogController) stage.getUserData();
    controller.setData(s, gameRoom);
    stage.showAndWait();
  }

  public static CompetitionRepresentation openDiscordJoinCompetitionDialog() {
    String title = "Join Competition";
    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionOfflineDialogController.class.getResource("dialog-discord-competition-join.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
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
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionDiscordDialogController controller = (CompetitionDiscordDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openSubscriptionDialog(List<CompetitionRepresentation> all, @Nullable CompetitionRepresentation selection) {
    String title = "Add Subscription";
    FXMLLoader fxmlLoader = new FXMLLoader(SubscriptionDialogController.class.getResource("dialog-subscription-add.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    SubscriptionDialogController controller = (SubscriptionDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static CompetitionRepresentation openJoinSubscriptionDialog(List<CompetitionRepresentation> all) {
    String title = "Join Subscription";
    FXMLLoader fxmlLoader = new FXMLLoader(JoinSubscriptionDialogController.class.getResource("dialog-subscription-join.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    JoinSubscriptionDialogController controller = (JoinSubscriptionDialogController) stage.getUserData();
    controller.setCompetition(all);
    stage.showAndWait();

    return controller.getCompetition();
  }

  public static List<CompetitionRepresentation> openIScoredSubscriptionDialog(List<CompetitionRepresentation> existingCompetitions) {
    String title = "Game Room Subscriptions";
    FXMLLoader fxmlLoader = new FXMLLoader(IScoredSubscriptionDialogController.class.getResource("dialog-iscored-subscription.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    IScoredSubscriptionDialogController controller = (IScoredSubscriptionDialogController) stage.getUserData();
    controller.setData(existingCompetitions);
    stage.showAndWait();

    return controller.getTableList();
  }

  public static CompetitionRepresentation openOfflineCompetitionDialog(List<CompetitionRepresentation> all, CompetitionRepresentation selection) {
    String title = "Edit Competition";
    if (selection == null) {
      title = "Add Competition";
    }
    else if (selection.getId() == null) {
      title = "Duplicate Competition";
    }


    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionOfflineDialogController.class.getResource("dialog-offline-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionOfflineDialogController controller = (CompetitionOfflineDialogController) stage.getUserData();
    controller.setCompetition(all, selection);
    stage.showAndWait();

    return controller.getCompetition();
  }
}
