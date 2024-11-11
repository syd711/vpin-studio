package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.dropins.DropInManager;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.monitor.CabMonitorController;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ToolbarController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ToolbarController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  private boolean monitorOpen = false;

  @FXML
  private Button updateBtn;

  @FXML
  private Button frontendMenuBtn;

  @FXML
  private Button monitorBtn;

  @FXML
  private MenuButton jobBtn;

  @FXML
  private MenuButton dropInsBtn;

  @FXML
  private MenuItem dofSyncEntry;

  @FXML
  private MenuItem muteSystemEntry;

  @FXML
  private MenuItem frontendMenuItem;

  @FXML
  private MenuItem shutdownMenuItem;

  @FXML
  private ToggleButton maintenanceBtn;

  @FXML
  private HBox toolbarHBox;

  @FXML
  private Label breadcrumb;

  @FXML
  private SplitMenuButton preferencesBtn;

  @FXML
  private ProgressIndicator jobProgress;

  public static String newVersion;
  public boolean muted = false;

  public static ToolbarController INSTANCE;
  private Stage monitorStage;

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
  private void onShutdown() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Shutdown Remote System?", "Shutdown the remote system and exit Studio?", null, "Shutdown and exit");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().systemShutdown();
      System.exit(0);
    }
  }

  @FXML
  private void onMute() {
    client.getSystemService().mute(!muted);
    muted = !muted;

    if (muted) {
      muteSystemEntry.setText("Unmute System");
      muteSystemEntry.setGraphic(WidgetFactory.createIcon("mdi2v-volume-high"));
    }
    else {
      muteSystemEntry.setText("Mute System");
      muteSystemEntry.setGraphic(WidgetFactory.createIcon("mdi2v-volume-mute"));
    }
  }


  @FXML
  private void onFrontend() {
    client.getFrontendService().restartFrontend();
  }

  @FXML
  private void onFrontendMenu() {
    try {
      Frontend frontend = client.getFrontendService().getFrontendCached();
      File file = new File(frontend.getInstallationDirectory(), frontend.getAdminExe());
      if (!file.exists()) {
        WidgetFactory.showAlert(Studio.stage, "Did not find exe", "The exe file " + file.getAbsolutePath() + " was not found.");
      }
      else {
        Studio.open(file);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open admin frontend: " + e.getMessage(), e);
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
    Studio.loadLauncher(new Stage());
  }

  @FXML
  private void onSettings(ActionEvent event) {
    PreferencesController.open();
  }

  @FXML
  private void toggleMonitor() {
    if (!monitorOpen) {
      monitorOpen = true;
      monitorBtn.getStyleClass().add("toggle-button-selected");
      monitorStage = Dialogs.createStudioDialogStage(null, CabMonitorController.class, "dialog-cab-monitor.fxml", "Cabinet Monitor", "cabMonitor");
      CabMonitorController controller = (CabMonitorController) monitorStage.getUserData();
      controller.setData(monitorStage);
      FXResizeHelper fxResizeHelper = new FXResizeHelper(monitorStage, 30, 6);
      monitorStage.setUserData(fxResizeHelper);
      monitorStage.setMinWidth(600);
      monitorStage.setMinHeight(500);

      monitorStage.show();
    }
    else {
      monitorStage.close();
    }
  }

  public void onMonitorClose() {
    monitorOpen = false;
    monitorBtn.getStyleClass().remove("toggle-button-selected");
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
  public void initialize(URL url, ResourceBundle resourceBundle) {
    INSTANCE = this;

    monitorBtn.managedProperty().bindBidirectional(monitorBtn.visibleProperty());
    maintenanceBtn.managedProperty().bindBidirectional(maintenanceBtn.visibleProperty());
    updateBtn.managedProperty().bindBidirectional(updateBtn.visibleProperty());
    frontendMenuBtn.managedProperty().bindBidirectional(frontendMenuBtn.visibleProperty());
    dropInsBtn.managedProperty().bindBidirectional(dropInsBtn.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();

    frontendMenuBtn.setVisible(frontend.getAdminExe() != null);
    frontendMenuItem.setVisible(frontend.getFrontendExe() != null);
    frontendMenuItem.setText("Restart " + frontend.getName());
    this.jobProgress.setDisable(true);
    this.jobProgress.setProgress(0);

    FrontendUtil.replaceName(frontendMenuBtn.getTooltip(), frontend);

    if (frontend.getIconName() != null) {
      Image image1 = new Image(Studio.class.getResourceAsStream(frontend.getIconName()));
      ImageView view1 = new ImageView(image1);
      view1.setPreserveRatio(true);
      view1.setFitHeight(18);
      frontendMenuItem.setGraphic(view1);

      Image image2 = new Image(Studio.class.getResourceAsStream(frontend.getIconName()));
      ImageView view2 = new ImageView(image2);
      view2.setPreserveRatio(true);
      view2.setFitHeight(18);
      frontendMenuBtn.setGraphic(view2);
    }

    if (frontend.getFrontendExe() == null) {
      preferencesBtn.getItems().remove(frontendMenuItem);
    }

    if (client.getSystemService().isLocal()) {
      preferencesBtn.getItems().remove(shutdownMenuItem);
    }

//    this.monitorBtn.setVisible(!client.getSystemService().isLocal() && Features.RECORDER && !client.getFrontendService().getFrontendCached().getSupportedRecodingScreens().isEmpty());
    this.monitorBtn.setVisible(Features.RECORDER && !client.getRecorderService().getRecordingScreens().isEmpty());
    this.maintenanceBtn.setVisible(!client.getSystemService().isLocal());

    EventManager.getInstance().addListener(this);

    JobPoller.destroy();
    JobPoller.create(this.jobBtn, this.jobProgress);

    runUpdateCheck();

    JobPoller.getInstance().setPolling();

    preferencesChanged(PreferenceType.serverSettings);


    Platform.runLater(() -> {
      DropInManager.getInstance().init(dropInsBtn);
    });

    Studio.stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        debouncer.debounce("breadcrumb", () -> {
          Platform.runLater(() -> {
            double maxWidth = newValue.intValue() - 800;
            breadcrumb.setMaxWidth(maxWidth);
          });
        }, DEBOUNCE_MS);
      }
    });

    client.getPreferenceService().addListener(this);
    preferencesChanged(PreferenceNames.DOF_SETTINGS, null);

    Platform.runLater(() -> {
      MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
      if (settings.isOpen()) {
        toggleMonitor();
      }
    });
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.DOF_SETTINGS)) {
      DOFSettings settings = client.getDofService().getSettings();
      boolean valid = settings.isValidDOFFolder() && !StringUtils.isEmpty(settings.getApiKey());
      dofSyncEntry.setDisable(!valid);
    }
  }
}