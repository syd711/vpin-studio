package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ToolbarController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(ToolbarController.class);

  @FXML
  private Button updateBtn;

  @FXML
  private Button popperMenuBtn;

  @FXML
  private MenuButton jobBtn;

  @FXML
  private MenuItem dofSyncEntry;

  @FXML
  private MenuItem popperEntry;

  @FXML
  private ToggleButton maintenanceBtn;

  @FXML
  private MenuButton messagesBtn;

  @FXML
  private HBox toolbarHBox;

  @FXML
  private SplitMenuButton preferencesBtn;

  @FXML
  private ProgressIndicator jobProgress;

  private String newVersion;

  // Add a public no-args constructor
  public ToolbarController() {
  }

  @FXML
  private void onMaintenance() {
    boolean maintenanceMode = EventManager.getInstance().isMaintenanceMode();
    if (!maintenanceMode) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Maintenance Mode", "Switch cabinet to maintenance mode?",
          "The maintenance mode will be automatically turned off on disconnect or exit.", "Enable Maintenance Mode");
      if (!result.isPresent() || !result.get().equals(ButtonType.OK)) {
        return;
      }
    }
    EventManager.getInstance().notifyMaintenanceMode(!maintenanceMode);
  }

  @Override
  public void maintenanceEnabled(boolean b) {
    if (maintenanceBtn.isVisible()) {
      if (b) {
        maintenanceBtn.getStyleClass().add("action-selected");
      }
      else {
        maintenanceBtn.getStyleClass().remove("action-selected");
      }
    }
  }

  @FXML
  private void onUpdate() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Update " + newVersion, "A new update has been found. Download and install update for server and client?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Dialogs.openUpdateDialog();
      Platform.runLater(() -> {
        runUpdateCheck();
      });
    }
  }


  @FXML
  private void onPopper() {
    client.getPinUPPopperService().restartPopper();
  }

  @FXML
  private void onPopperMenu() {
      try {
        String pinupSystemDirectory = client.getSystemService().getSystemSummary().getPinupSystemDirectory();
        File file = new File(pinupSystemDirectory, "PinUpMenuSetup.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find PinUpMenuSetup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          Studio.open(file);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open PinUpMenuSetup.exe: " + e.getMessage(), e);
      }
  }

  @FXML
  private void onDisconnect() {
    try {
      client.getSystemService().setMaintenanceMode(false);
    }
    catch (Exception e) {
      LOG.error("Exception ignored, Cannot set maintenance mode, system may be done", e);
    }
    Studio.stage.close();
    NavigationController.refreshControllerCache();
    NavigationController.refreshViewCache();
//    NavigationController.refreshAvatar();
    Studio.loadLauncher(new Stage());
  }

  @FXML
  private void onSettings(ActionEvent event) {
    PreferencesController.open();
  }


  @FXML
  private void onClearCache() {
    client.clearCache();
  }

  @FXML
  private void onDOFSyn() {
    Studio.client.getDofService().sync(false);
    JobPoller.getInstance().setPolling();
  }

  private void runUpdateCheck() {
    try {
      updateBtn.setVisible(false);
      new Thread(() -> {
        String serverVersion = client.getSystemService().getVersion();
        String clientVersion = Studio.getVersion();

        String updateServerVersion = Updater.checkForUpdate(serverVersion);
        String updateClientVersion = Updater.checkForUpdate(clientVersion);

        if (updateClientVersion != null) {
          Platform.runLater(() -> {
            newVersion = updateClientVersion;
            updateBtn.setText("Version " + updateClientVersion + " available");
            updateBtn.setVisible(!StringUtils.isEmpty(updateClientVersion));
          });
        }
        else if (updateServerVersion != null) {
          Platform.runLater(() -> {
            newVersion = updateServerVersion;
            updateBtn.setText("Version " + updateServerVersion + " available");
            updateBtn.setVisible(!StringUtils.isEmpty(updateServerVersion));
          });
        }
      }).start();
    }
    catch (Exception e) {
      LOG.error("Failed to run update check: " + e.getMessage(), e);
    }
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.serverSettings)) {
      DOFSettings settings = client.getDofService().getSettings();
      boolean valid = (settings.isValidDOFFolder() || settings.isValidDOFFolder32()) && !StringUtils.isEmpty(settings.getApiKey());
      dofSyncEntry.setDisable(!valid);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    maintenanceBtn.managedProperty().bindBidirectional(maintenanceBtn.visibleProperty());
    updateBtn.managedProperty().bindBidirectional(updateBtn.visibleProperty());
    messagesBtn.managedProperty().bindBidirectional(messagesBtn.visibleProperty());
    popperMenuBtn.managedProperty().bindBidirectional(popperMenuBtn.visibleProperty());

    popperMenuBtn.setVisible(client.getSystemService().isLocal());

    this.jobBtn.setDisable(true);
    this.jobProgress.setDisable(true);
    this.jobProgress.setProgress(0);

    Image image1 = new Image(Studio.class.getResourceAsStream("popper.png"));
    ImageView view1 = new ImageView(image1);
    view1.setPreserveRatio(true);
    view1.setFitHeight(18);
    popperEntry.setGraphic(view1);

    Image image2 = new Image(Studio.class.getResourceAsStream("popper.png"));
    ImageView view2 = new ImageView(image2);
    view2.setPreserveRatio(true);
    view2.setFitHeight(18);
    popperMenuBtn.setGraphic(view2);

    this.messagesBtn.setVisible(false);
    this.maintenanceBtn.setVisible(!client.getSystemService().isLocal());

    EventManager.getInstance().addListener(this);

    JobPoller.destroy();
    JobPoller.create(this.jobBtn, this.jobProgress, this.messagesBtn);

    runUpdateCheck();

    JobPoller.getInstance().setPolling();

    this.messagesBtn.setVisible(!client.getJobsService().getResults().isEmpty());

    preferencesChanged(PreferenceType.serverSettings);
  }
}