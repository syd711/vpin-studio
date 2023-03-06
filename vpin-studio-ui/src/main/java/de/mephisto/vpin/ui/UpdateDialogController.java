package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.Updater;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.lang3.StringUtils;

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
    String newVersion = Updater.checkForUpdate(Studio.getVersion());
    clientLabel.setText("Downloading " + String.format(Updater.BASE_URL, newVersion) + Updater.UI_ZIP);
    serverLabel.setText("Downloading " + String.format(Updater.BASE_URL, newVersion) + Updater.SERVER_ZIP);


    String existingVersion = client.version();
    if(existingVersion.equals(newVersion)) {
      serverProgress.setDisable(true);
      serverProgress.setProgress(1f);
      serverLabel.setText("The server is already running on version " + newVersion);
      startClientUpdate(newVersion);
    }
    else {
      startServerUpdate(newVersion);
    }
  }

  private void startServerUpdate(String newVersion) {
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
                double p = Double.valueOf(progress) / 100.0;
                serverProgress.setProgress(p);
              });

              if (progress == 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              serverLabel.setText("Restarting Server");
              serverProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            client.installServerUpdate();
            Thread.sleep(5000);

            while (true) {
              Thread.sleep(1000);
              if (client.version() != null) {
                break;
              }
            }

            Platform.runLater(() -> {
              serverLabel.setText("Update successful, server is running on version " + client.version());
              serverProgress.setProgress(1f);
            });

            //finished
            startClientUpdate(newVersion);

            return null;
          }
        };
      }
    };
    serverService.start();
  }

  private void startClientUpdate(String newVersion) {
    clientService = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            Updater.downloadUpdate(newVersion, Updater.UI_ZIP);
            while (true) {
              int progress = Updater.getDownloadProgress(Updater.UI_ZIP, Updater.UI_ZIP_SIZE);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                clientProgress.setProgress(p);
              });

              if (progress == 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              clientLabel.setText("Installing Update");
              clientProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            Thread.sleep(2000);
            Updater.installClientUpdate();
            return null;
          }
        };
      }
    };
    clientService.start();
  }

  @Override
  public void onDialogCancel() {
    serverService.cancel();
  }
}
