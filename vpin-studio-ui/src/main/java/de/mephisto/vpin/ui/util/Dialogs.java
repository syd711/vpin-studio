package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Iterator;

public class Dialogs {

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
    return stage;
  }

  public static PlayerRepresentation openPlayerDialog(PlayerRepresentation selection) {
    Parent root = null;
    FXMLLoader fxmlLoader = new FXMLLoader(PlayerDialogController.class.getResource("dialog-player-edit.fxml"));
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    PlayerDialogController controller = fxmlLoader.getController();
    controller.setPlayer(selection);

    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    if (selection == null) {
      stage.setTitle("Add New Player");
    }
    else {
      stage.setTitle("Edit Player");
    }

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setResizable(false);
    stage.showAndWait();

    return controller.getPlayer();
  }

  public static CompetitionRepresentation openCompetitionDialog(CompetitionRepresentation selection) {
    Parent root = null;
    FXMLLoader fxmlLoader = new FXMLLoader(CompetitionDialogController.class.getResource("dialog-competition-edit.fxml"));
    try {
      root = fxmlLoader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    CompetitionDialogController controller = fxmlLoader.getController();
    controller.setCompetition(selection);

    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setResizable(false);
    if (selection == null) {
      stage.setTitle("Add New Competition");
    }
    else {
      stage.setTitle("Edit Competition");
    }

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
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
    stage.showAndWait();

    return controller.uploadFinished();
  }

  public static void openMediaDialog(GameRepresentation game, GameMediaItemRepresentation item) {
    Parent root = null;
    try {
      root = FXMLLoader.load(Studio.class.getResource("dialog-media.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage owner = Studio.stage;
    BorderPane mediaView = (BorderPane) root.lookup("#mediaView");
    WidgetFactory.addMediaItemToBorderPane(item, mediaView);
    final Stage stage = createStage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle(game.getGameDisplayName() + " - " + item.getScreen() + " Screen");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.showAndWait();
  }

  public static void openBotTutorial() {
    Parent root = null;
    try {
      root = FXMLLoader.load(Studio.class.getResource("dialog-bot-tutorial.fxml"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage owner = Studio.stage;
    final Stage stage = createStage();
    stage.initOwner(owner);
    stage.initModality(Modality.WINDOW_MODAL);
    stage.setTitle("Discord Bot Instructions");

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
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
