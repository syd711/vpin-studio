package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.DialogController;
import de.mephisto.vpin.ui.DialogHeaderController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.competitions.CompetitionDialogController;
import de.mephisto.vpin.ui.players.PlayerDialogController;
import de.mephisto.vpin.ui.tables.dialogs.DirectB2SUploadController;
import de.mephisto.vpin.ui.tables.dialogs.ROMUploadController;
import de.mephisto.vpin.ui.tables.dialogs.TableUploadController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;
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

    Object controller = fxmlLoader.getController();

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
        ((DialogController)controller).onDialogCancel();
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
    Parent root = null;
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource("dialog-directb2s-upload.fxml"));
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    DirectB2SUploadController controller = fxmlLoader.getController();
    controller.setGame(game);

    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setResizable(false);
    stage.setTitle("DirectB2S File Upload");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openTableUploadDialog() {
    Parent root = null;
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource("dialog-table-upload.fxml"));
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    TableUploadController controller = fxmlLoader.getController();
    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setResizable(false);
    stage.setTitle("Table Upload");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static boolean openRomUploadDialog() {
    Parent root = null;
    FXMLLoader fxmlLoader = new FXMLLoader(Studio.class.getResource("dialog-rom-upload.fxml"));
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    ROMUploadController controller = fxmlLoader.getController();
    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setResizable(false);
    stage.setTitle("Rom Upload");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.showAndWait();

    return controller.uploadFinished();
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
    Parent root = null;
    try {
      root = FXMLLoader.load(Studio.class.getResource("dialog-progress.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage owner = Studio.stage;
    final Label titleLabel = (Label) root.lookup("#titleLabel");
    final Label progressBarLabel = (Label) root.lookup("#progressBarLabel");
    final ToolBar toolBar = (ToolBar) root.lookup("#bottomToolbar");
    final Button cancelButton = (Button) toolBar.getItems().get(0);
    titleLabel.setText(model.getTitle());

    final ProgressResultModel progressResultModel = new ProgressResultModel();
    final Service service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            int index = 0;
            while (model.hasNext() && !this.isCancelled()) {
              String result = model.processNext(progressResultModel);
              long percent = index * 100 / model.getMax();
              updateProgress(percent, 100);
              final int uiIndex = index;
              Platform.runLater(() -> {
                titleLabel.setText(model.getTitle() + " (" + uiIndex + "/" + model.getMax() + ")");
                progressBarLabel.setText("Processing: " + result);
              });
              index++;
            }
            return null;
          }
        };
      }
    };

    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setResizable(false);
    stage.initOwner(owner);

    final ProgressBar progressBar = (ProgressBar) root.lookup("#progressBar");
    progressBar.progressProperty().bind(service.progressProperty());
    service.stateProperty().addListener((ChangeListener<Worker.State>) (observable, oldValue, newValue) -> {
      if (newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED
          || newValue == Worker.State.SUCCEEDED) {
        stage.hide();

        Platform.runLater(() -> {
          String msg = model.getTitle() + " finished.\n\nProcessed " + progressResultModel.getProcessed() + " of " + model.getMax() + " elements.";
          WidgetFactory.showAlert(msg);
        });
      }
    });

    cancelButton.setOnAction(event -> service.cancel());
    stage.onHidingProperty().addListener((observableValue, windowEventEventHandler, t1) -> service.cancel());

    Scene scene = new Scene(root);
    stage.setScene(scene);
    service.start();

    stage.showAndWait();
  }
}
