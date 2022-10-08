package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.VPinStudioApplication;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Iterator;

public class WidgetFactory {
  public static void createProgressDialog(ProgressModel model,
                                          Stage owner) throws IOException {
    Parent root = FXMLLoader.load(VPinStudioApplication.class.getResource("dialog-progress.fxml"));

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
            Iterator iterator = model.getIterator();
            int index = 1;
            while (iterator.hasNext() && !this.isCancelled()) {
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

    final Stage stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(owner);

    final ProgressBar progressBar = (ProgressBar) root.lookup("#progressBar");
    progressBar.progressProperty().bind(service.progressProperty());
    service.stateProperty().addListener((ChangeListener<Worker.State>) (observable, oldValue, newValue) -> {
      if (newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED
          || newValue == Worker.State.SUCCEEDED) {
        stage.hide();

        String msg = model.getTitle() + " finished.\n\nProcessed "  + progressResultModel.getProcessed() + " of " + model.getMax() + " elements.";
        WidgetFactory.showAlert(msg);
      }
    });

    cancelButton.setOnAction(event -> service.cancel());
    stage.onHidingProperty().addListener((observableValue, windowEventEventHandler, t1) -> service.cancel());

    Scene scene = new Scene(root);
    stage.setScene(scene);
    service.start();
    stage.show();

    scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> service.cancel());
  }


  public static void showAlert(String msg) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.CLOSE);
    alert.getDialogPane().getStylesheets().add(VPinStudioApplication.class.getResource("stylesheet.css").toExternalForm());
    alert.getDialogPane().getStyleClass().add("base-component");
    alert.setHeaderText(null);
    alert.setGraphic(null);
    alert.showAndWait();

  }
}
