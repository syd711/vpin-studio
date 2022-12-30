package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.ProgressDialogController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.CompetitionDialogController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.dialogs.DirectB2SUploadController;
import de.mephisto.vpin.ui.tables.dialogs.ROMUploadController;
import de.mephisto.vpin.ui.tables.dialogs.TableDeleteController;
import de.mephisto.vpin.ui.tables.dialogs.TableUploadController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Dialogs {

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection) {
    String title = "Add New Player";
    if (selection != null) {
      title = "Edit Player";
    }

    FXMLLoader fxmlLoader = new FXMLLoader(PlayerDialogController.class.getResource("dialog-player-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    PlayerDialogController controller = (PlayerDialogController) stage.getUserData();
    controller.setPlayer(selection);
    stage.showAndWait();

    return controller.getPlayer();
  }

  public static CompetitionRepresentation openCompetitionDialog(CompetitionRepresentation selection) {
    String title = "Add New Competition";
    if (selection != null) {
      title = "Edit Competition";
    }


    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionDialogController.class.getResource("dialog-competition-edit.fxml"));
    Stage stage = WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
    CompetitionDialogController controller = (CompetitionDialogController) stage.getUserData();
    controller.setCompetition(selection);
    stage.showAndWait();

    return controller.getCompetition();
  }


  public static boolean openDirectB2SUploadDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage("dialog-directb2s-upload.fxml", "DirectB2S File Upload");
    DirectB2SUploadController controller = (DirectB2SUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableUploadDialog() {
    Stage stage = createStudioDialogStage("dialog-table-upload.fxml", "VPX Table Upload");
    TableUploadController controller = (TableUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableDeleteDialog(GameRepresentation game) {
    Stage stage = createStudioDialogStage("dialog-table-delete.fxml", "Delete Table");
    TableDeleteController controller = (TableDeleteController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.tableDeleted();
  }

  public static boolean openRomUploadDialog() {
    Stage stage = createStudioDialogStage("dialog-rom-upload.fxml", "Rom Upload");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openBotServerIdTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-server-id-tutorial.fxml", "Server ID Instructions");
    stage.showAndWait();
  }

  public static void openBotTokenTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-token-tutorial.fxml", "Bot Token Instructions");
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Stage stage = createStudioDialogStage("dialog-bot-tutorial.fxml", "Discord Bot Instructions");
    stage.showAndWait();
  }

  public static void createProgressDialog(ProgressModel model) {
    Stage stage = createStudioDialogStage("dialog-progress.fxml", model.getTitle());
    ProgressDialogController controller = (ProgressDialogController) stage.getUserData();
    controller.setProgressModel(stage, model);
    stage.showAndWait();
  }

  public static void openMediaDialog(VPinStudioClient client, GameRepresentation game, GameMediaItemRepresentation item) {
    Parent root = null;
    try {
      root = FXMLLoader.load(Studio.class.getResource("dialog-media.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage owner = Studio.stage;
    BorderPane mediaView = (BorderPane) root.lookup("#mediaView");
    WidgetFactory.addMediaItemToBorderPane(client, item, mediaView);
    final Stage stage = WidgetFactory.createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(game.getGameDisplayName() + " - " + item.getScreen() + " Screen");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.showAndWait();
  }

  private static Stage createStudioDialogStage(String fxml, String title) {
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource(fxml));
    return WidgetFactory.createDialogStage(fxmlLoader, Studio.stage, title);
  }

  public static void openPopperRunningWarning(Stage stage) {
    WidgetFactory.showAlert(stage, "PinUP Popper is running.", "PinUP Popper is running. To perform this operation, you have to close it.");
  }
}
