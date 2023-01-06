package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class UpdateDialogController implements Initializable, DialogController {

  @FXML
  private Label clientLabel;

  @FXML
  private Label serverLabel;

  @FXML
  private ProgressBar clientProgress;

  @FXML
  private ProgressBar serverProgress;

  private Service serverService;
  private Service clientService;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    String newVersion = "1.2.7"; //Updater.checkForUpdate(Studio.getVersion());
    clientLabel.setText("Downloading " + String.format(Updater.BASE_URL, newVersion) + Updater.UI_ZIP);
    serverLabel.setText("Downloading " + String.format(Updater.BASE_URL, newVersion) + Updater.SERVER_ZIP);

    serverService = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            client.startServerUpdate(newVersion);
            while (true) {
              int progress = client.getServerUpdateProgress();
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p =Double.valueOf(progress)/100.0;

                System.out.println(p);
                serverProgress.setProgress(p);
//                titleLabel.setText(model.getTitle() + " (" + uiIndex + "/" + model.getMax() + ")");
//                progressBarLabel.setText("Processing: " + result);
              });

              if(progress == 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              serverLabel.setText("Restarting Server");
              serverProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            client.installServerUpdate();
            Thread.sleep(1000);

            while (true) {
              Thread.sleep(1000);
              if(client.version() != null) {
                break;
              }
            }

            serverLabel.setText("Update Finished");
            serverProgress.setProgress(1f);

            //finished
            return null;
          }
        };
      }
    };
    serverService.start();
  }

  @Override
  public void onDialogCancel() {
    serverService.cancel();
  }
}
