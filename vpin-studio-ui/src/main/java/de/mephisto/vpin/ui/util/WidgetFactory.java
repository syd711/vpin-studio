package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.ui.VPinStudioApplication;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class WidgetFactory {
  public static void createProgressDialog(ProgressModel model,
                                           Stage owner) {
//    Parent root = FXMLLoader.load(getClass().getResource("scene-main.fxml"));
    Service service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            for(int i=0; i<100; i++){

              updateProgress(i, 100);
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            return null;
          }
        };
      }
    };

    final Stage stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.initOwner(owner);
    final BorderPane root = new BorderPane();
    root.getStylesheets().add(VPinStudioApplication.class.getResource("stylesheet.css").toExternalForm());
    root.setStyle(".navigation-panel");
    final ProgressBar indicator = new ProgressBar();
    // have the indicator display the progress of the service:
    indicator.progressProperty().bind(service.progressProperty());
    // hide the stage when the service stops running:
    service.stateProperty().addListener(new ChangeListener<Worker.State>() {
      @Override
      public void changed(
          ObservableValue<? extends Worker.State> observable,
          Worker.State oldValue, Worker.State newValue) {
        if (newValue == Worker.State.CANCELLED || newValue == Worker.State.FAILED
            || newValue == Worker.State.SUCCEEDED) {
          stage.hide();
        }
      }
    });
    // A cancel button:
    Button cancel = new Button("Cancel");
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        service.cancel();
      }
    });
    root.setCenter(indicator);
    root.setBottom(cancel);
    Scene scene = new Scene(root, 400, 200);
    stage.setScene(scene);

    service.start();
    stage.show();
  }
}
