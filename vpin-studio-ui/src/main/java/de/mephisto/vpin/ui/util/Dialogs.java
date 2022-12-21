package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.DialogController;
import de.mephisto.vpin.ui.DialogHeaderController;
import de.mephisto.vpin.ui.ProgressDialogController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.CompetitionDialogController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.dialogs.DirectB2SUploadController;
import de.mephisto.vpin.ui.tables.dialogs.ROMUploadController;
import de.mephisto.vpin.ui.tables.dialogs.TableUploadController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Dialogs {

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
    return stage;
  }

  public static Stage createDialogStage(String title, String fxml) {
    return createDialogStage(Studio.class, title, fxml);
  }

  public static Stage createDialogStage(Class clazz, String title, String fxml) {
    Parent root = null;

    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    try {
      root = fxmlLoader.load();

    } catch (IOException e) {
      e.printStackTrace();
    }

    DialogController controller = fxmlLoader.getController();

    Node header = root.lookup("#header");
    DialogHeaderController dialogHeaderController = (DialogHeaderController) header.getUserData();
    dialogHeaderController.setTitle(title);

    Stage owner = Studio.stage;
    final Stage stage = createStage();
    dialogHeaderController.setStage(stage);
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setTitle(title);
    stage.setUserData(controller);

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.getRoot().setStyle("-fx-border-width: 1;-fx-border-color: #605E5E;");
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        if(controller != null) {
          controller.onDialogCancel();
        }
        stage.close();
      }
    });
    return stage;
  }

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection) {
    String title = "Add New Player";
    if (selection != null) {
      title = "Edit Player";
    }

    Stage stage = createDialogStage(PlayerDialogController.class, title, "dialog-player-edit.fxml");
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

    Stage stage = createDialogStage(CompetitionDialogController.class, title, "dialog-competition-edit.fxml");
    CompetitionDialogController controller = (CompetitionDialogController) stage.getUserData();
    controller.setCompetition(selection);
    stage.showAndWait();

    return controller.getCompetition();
  }


  public static boolean openDirectB2SUploadDialog(GameRepresentation game) {
    Stage stage = createDialogStage("DirectB2S File Upload", "dialog-directb2s-upload.fxml");
    DirectB2SUploadController controller = (DirectB2SUploadController) stage.getUserData();
    controller.setGame(game);
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableUploadDialog() {
    Stage stage = createDialogStage("VPX Table Upload", "dialog-table-upload.fxml");
    TableUploadController controller = (TableUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openRomUploadDialog() {
    Stage stage = createDialogStage("Rom Upload", "dialog-rom-upload.fxml");
    ROMUploadController controller = (ROMUploadController) stage.getUserData();
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openBotServerIdTutorial() {
    Stage stage = createDialogStage("Server ID Instructions", "dialog-bot-server-id-tutorial.fxml");
    stage.showAndWait();
  }

  public static void openBotTokenTutorial() {
    Stage stage = createDialogStage("Bot Token Instructions", "dialog-bot-token-tutorial.fxml");
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Stage stage = createDialogStage("Discord Bot Instructions", "dialog-bot-tutorial.fxml");
    stage.showAndWait();
  }

  public static void createProgressDialog(ProgressModel model) {
    Stage stage = createDialogStage(model.getTitle(), "dialog-progress.fxml");
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
    final Stage stage = createStage();
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
}
