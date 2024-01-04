package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class UpdateDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateDialogController.class);

  @FXML
  private Label localClientLabel;

  @FXML
  private Label remoteClientLabel;

  @FXML
  private Label serverLabel;

  @FXML
  private ProgressBar localClientProgress;

  @FXML
  private ProgressBar remoteClientProgress;

  @FXML
  private ProgressBar serverProgress;

  @FXML
  private HBox footer;

  @FXML
  private Button closeBtn;

  private Service serverService;
  private Service remoteClientService;
  private Service clientService;

  private boolean updateServer = false;
  private boolean updateClient = false;
  private boolean updateRemoteClient = false;


  @FXML
  private VBox serverClientUpdateRoot;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    serverClientUpdateRoot.managedProperty().bindBidirectional(serverClientUpdateRoot.visibleProperty());
    footer.managedProperty().bindBidirectional(footer.visibleProperty());
    serverClientUpdateRoot.setVisible(false);

    String clientVersion = Studio.getVersion();
    String serverVersion = client.getSystemService().getVersion();

    String newClientVersion = Updater.checkForUpdate(clientVersion);
    updateClient = newClientVersion != null;

    String newServerVersion = Updater.checkForUpdate(serverVersion);
    updateServer = newServerVersion != null;

    if (client.getSystemService().isLocal()) {
      serverClientUpdateRoot.setVisible(false);
    }
    else if (updateServer) {
      //remote server and remote client are always updated together
      updateRemoteClient = true;
      serverClientUpdateRoot.setVisible(true);
    }


    //initialize UI
    if (!updateClient) {
      localClientProgress.setDisable(true);
      localClientProgress.setProgress(1f);
      localClientLabel.setText("The client is already running on version " + clientVersion);
    }
    else {
      localClientLabel.setText("Downloading " + String.format(Updater.BASE_URL, newClientVersion) + Updater.UI_ZIP);
    }

    if (!updateServer) {
      serverProgress.setDisable(true);
      serverProgress.setProgress(1f);
      serverLabel.setText("The server is already running on version " + serverVersion);
    }
    else {
      serverLabel.setText("Downloading " + String.format(Updater.BASE_URL, newServerVersion) + Updater.SERVER_ZIP);
    }

    //execute updates
    if (updateServer) {
      startServerUpdate(newServerVersion, newClientVersion);
    }
    else if (updateClient) {
      startClientUpdate(newClientVersion);
    }
  }

  @FXML
  private void onClose(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  private void startServerUpdate(String newServerVersion, String newClientVersion) {
    serverService = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            client.getSystemService().startServerUpdate(newServerVersion);
            while (true) {
              int progress = client.getSystemService().getServerUpdateProgress();
              LOG.info("Server Update Download: " + progress);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                serverProgress.setProgress(p);
              });

              if (progress >= 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              serverLabel.setText("Restarting Server");
              serverProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            client.getSystemService().installServerUpdate();
            Thread.sleep(5000);

            while (true) {
              Thread.sleep(1000);
              if (client.getSystemService().getVersion() != null) {
                break;
              }
            }

            Platform.runLater(() -> {
              serverLabel.setText("Update successful, server is running on version " + client.getSystemService().getVersion());
              serverProgress.setProgress(1f);

              LOG.info("Server updated finished to " + client.getSystemService().getVersion());
            });

            //finished
            Platform.runLater(() -> {
              if (updateRemoteClient) {
                LOG.info("Starting remote client update.");
                startRemoteClientUpdate(newServerVersion, newClientVersion);
              }
              else {
                LOG.info("Starting local client update.");
                startClientUpdate(newClientVersion);
              }
            });

            return null;
          }
        };
      }
    };
    serverService.start();
  }


  private void startRemoteClientUpdate(String newServerVersion, String newClientVersion) {
    remoteClientService = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            LOG.info("Remote Client Update Service started");
            client.getSystemService().startRemoteClientUpdate(newServerVersion);
            while (true) {
              int progress = client.getSystemService().getRemoteClientProgress();
              LOG.info("Remote Client Update Download: " + progress);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                serverProgress.setProgress(p);
              });

              if (progress >= 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              remoteClientLabel.setText("Installing Remote Client");
              remoteClientProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            client.getSystemService().installRemoteClientUpdate();

            Platform.runLater(() -> {
              remoteClientLabel.setText("Update successful, server is running on version " + client.getSystemService().getVersion());
              remoteClientProgress.setProgress(1f);
            });

            //finished
            if (updateClient) {
              Platform.runLater(() -> {
                startClientUpdate(newClientVersion);
              });
            }

            return null;
          }
        };
      }
    };
    remoteClientService.start();
  }

  private void startClientUpdate(String newVersion) {
    resetDoNotShowAgain();

    String clientVersion = Studio.getVersion();
    if(clientVersion != null && clientVersion.equals(newVersion)) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        //ignore
      }
      return;
    }

    clientService = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            new Thread(() -> {
              Updater.downloadUpdate(newVersion, Updater.UI_ZIP);
            }).start();
            Thread.sleep(1000);
            while (true) {
              int progress = Updater.getDownloadProgress(Updater.UI_ZIP, Updater.UI_ZIP_SIZE);
              LOG.info("Client Update Download: " + progress);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                localClientProgress.setProgress(p);
              });

              if (progress >= 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              localClientLabel.setText("Installing Update");
              localClientProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            Thread.sleep(2000);
            Updater.installFromLocalClientUpdate();
            return null;
          }
        };
      }
    };
    clientService.start();
  }

  private void resetDoNotShowAgain() {
    try {
      PreferenceEntryRepresentation doNotShowAgainPref = client.getPreferenceService().getPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS);
      List<String> csvValue = doNotShowAgainPref.getCSVValue();
      csvValue.remove(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_UPDATE_INFO);
      client.getPreferenceService().setPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS, String.join(",", csvValue));
    } catch (Exception e) {
      LOG.error("Failed to reset update info: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {
    serverService.cancel();
  }
}
