package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.preferences.UISettings;
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
import java.util.ResourceBundle;

//import static de.mephisto.vpin.ui.Studio.client;

public class UpdateDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateDialogController.class);

  @FXML
  private Label clientLabel;

  @FXML
  private Label remoteClientLabel;

  @FXML
  private Label serverLabel;

  @FXML
  private ProgressBar clientProgress;

  @FXML
  private ProgressBar remoteClientProgress;

  @FXML
  private ProgressBar serverProgress;

  @FXML
  private HBox footer;

  @FXML
  private VBox remoteClientUpdate;

  @FXML
  private Button closeBtn;

  private Service<?> serverService;
  private Service<?> clientService;
  private Service<?> remoteClientService;

  private boolean updateServer = false;
  private boolean updateClient = false;

  /**
   * The client that needs to be updated
   */
  private VPinStudioClient client;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    remoteClientUpdate.managedProperty().bindBidirectional(remoteClientUpdate.visibleProperty());
  }

  public void setClient(VPinStudioClient _client) {
    this.client = _client;

    remoteClientUpdate.setVisible(!client.getSystemService().isLocal());

    String latestVersion = Updater.checkForUpdate();

    String clientVersion = Studio.getVersion();
    String serverVersion = client.getSystemService().getVersion();

    updateClient = Updater.isLargerVersionThan(latestVersion, clientVersion);
    updateServer = Updater.isLargerVersionThan(latestVersion, serverVersion);

    //initialize UI
    if (!updateClient) {
      clientProgress.setDisable(true);
      clientProgress.setProgress(1f);
      clientLabel.setText("The client is already running on version " + clientVersion);
    }
    else {
      String os = System.getProperty("os.name");
      boolean winUpdate = os.contains("Windows");
      if (winUpdate) {
        clientLabel.setText("Downloading " + String.format(Updater.BASE_URL, latestVersion) + Updater.UI_ZIP);
      }
      else {
        clientLabel.setText("Downloading " + String.format(Updater.BASE_URL, latestVersion) + Updater.UI_JAR_ZIP);
      }
    }

    if (!updateServer) {
      serverProgress.setDisable(true);
      serverProgress.setProgress(1f);
      remoteClientProgress.setDisable(true);
      remoteClientProgress.setProgress(1f);
      remoteClientLabel.setText("The remote client is already running on version " + serverVersion);
      serverLabel.setText("The server is already running on version " + serverVersion);
    }
    else {
      serverLabel.setText("Downloading " + String.format(Updater.BASE_URL, latestVersion) + Updater.SERVER_ZIP);
      remoteClientLabel.setText("Downloading " + String.format(Updater.BASE_URL, latestVersion) + Updater.UI_ZIP);
    }

    //execute updates
    if (updateServer) {
      startServerUpdate(latestVersion, latestVersion);
    }
    else if (updateClient) {
      startClientUpdate(latestVersion);
    }
  }

  @FXML
  private void onClose(ActionEvent ae) {
    try {
      if (serverService != null && serverService.isRunning()) {
        serverService.cancel();
      }

      if (clientService != null && clientService.isRunning()) {
        clientService.cancel();
      }

      if (remoteClientService != null && remoteClientService.isRunning()) {
        remoteClientService.cancel();
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to cancel update services: " + e.getMessage());
    }

    if (ae != null) {
      Stage stage = (Stage) ((Button) ae.getSource()).getScene().getWindow();
      stage.close();
    }
  }

  private void startServerUpdate(String newServerVersion, String newClientVersion) {
    serverService = new Service<>() {
      @Override
      protected Task<Object> createTask() {
        return new Task<>() {
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

            boolean b = client.getSystemService().installServerUpdate();
            if (!b) {
              Platform.runLater(() -> {
                WidgetFactory.showAlert(Studio.stage, "Error", "Server update failed, restart the server and client and try again.", "In case this fails too, check the github Wiki how to update manually.");
              });
              return null;
            }
            Thread.sleep(5000);

            while (true) {
              Thread.sleep(1000);
              if (client.getSystemService().getVersion() != null) {
                break;
              }
            }

            Platform.runLater(() -> {
              String updatedVersion = client.getSystemService().getVersion();
              serverLabel.setText("Update successful, server is running on version " + updatedVersion);
              serverProgress.setProgress(1f);

              LOG.info("Server updated finished to " + updatedVersion);
            });

            // if we are here and remote, assume the remote client has to be updated as well  
            if (!client.getSystemService().isLocal()) {
              Platform.runLater(() -> {
                startRemoteClientUpdate(newServerVersion, newClientVersion);
              });
            }
            else if (updateClient) {
              Platform.runLater(() -> {
                startClientUpdate(newClientVersion);
              });
            }

            return null;
          }
        };
      }
    };
    serverService.start();
  }

  private void startRemoteClientUpdate(String newServerVersion, String newClientVersion) {
    remoteClientService = new Service<>() {
      @Override
      protected Task<Object> createTask() {
        return new Task<>() {
          @Override
          protected Object call() throws Exception {
            client.getSystemService().startRemoteClientUpdate(newServerVersion);
            while (true) {
              int progress = client.getSystemService().getRemoteClientProgress();
              LOG.info("Server Remote Client Download: " + progress);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                remoteClientProgress.setProgress(p);
              });

              if (progress >= 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              remoteClientLabel.setText("Installing Remote Client Update");
              remoteClientProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            boolean result = client.getSystemService().installRemoteClientUpdate();
            Platform.runLater(() -> {
              if (result) {
                remoteClientLabel.setText("Update successful, the remote client has been updated to " + newServerVersion);
              }
              else {
                remoteClientLabel.setText("Update failed, check server log for details.");
              }

              remoteClientProgress.setProgress(1f);

              LOG.info("Server updated finished to " + client.getSystemService().getVersion());
            });


            Platform.runLater(() -> {
              startClientUpdate(newClientVersion);
            });
            return null;
          }
        };
      }
    };
    remoteClientService.start();
  }

  private void startClientUpdate(String newVersion) {
    resetShowUpdateInfoFlag();

    String clientVersion = Studio.getVersion();
    if (clientVersion != null && clientVersion.equals(newVersion)) {
      try {
        Thread.sleep(2000);
      }
      catch (InterruptedException e) {
        //ignore
      }
      return;
    }

    String os = System.getProperty("os.name");
    LOG.info("Updater resolved OS name '" + os + "'");
    boolean winUpdate = os.contains("Windows");
    clientService = new Service<>() {
      @Override
      protected Task<Object> createTask() {
        return new Task<>() {
          @Override
          protected Object call() throws Exception {
            new Thread(() -> {
              if (winUpdate) {
                Updater.downloadUpdate(newVersion, Updater.UI_ZIP);
              }
              else {
                Updater.downloadUpdate(newVersion, Updater.UI_JAR_ZIP);
              }
            }).start();
            Thread.sleep(1000);
            while (true) {
              String file = Updater.UI_JAR_ZIP;
              if (winUpdate) {
                file = Updater.UI_ZIP;
              }

              int progress = Updater.getDownloadProgress(file, Updater.UI_ZIP_SIZE);
              LOG.info("Client Update Download: " + progress);
              updateProgress(progress, 100);
              Thread.sleep(1000);
              Platform.runLater(() -> {
                double p = Double.valueOf(progress) / 100.0;
                clientProgress.setProgress(p);
              });

              if (progress >= 100) {
                break;
              }
            }
            Platform.runLater(() -> {
              clientLabel.setText("Installing Update");
              clientProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            Thread.sleep(2000);
            Updater.installClientUpdate(clientVersion, newVersion);
            return null;
          }
        };
      }
    };
    clientService.start();
  }

  private void resetShowUpdateInfoFlag() {
    try {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      uiSettings.setHideUpdateInfo(false);
      client.getPreferenceService().setJsonPreference(uiSettings);
    }
    catch (Exception e) {
      LOG.error("Failed to reset update info: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {
    onClose(null);
  }
}
