package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringMode;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.ToolbarController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class CabMonitorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CabMonitorController.class);
  public static final double MIN_ZOOM = 0.2;
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;
  public static String MODAL_STATE_ID = "cabinetMonitorController";

  private double scaling = 1;

  @FXML
  private ScrollPane scrollPane;

  @FXML
  private MenuButton screenMenuButton;

  @FXML
  private ComboBox<MonitoringMode> monitoringModeCombo;

  @FXML
  private Spinner<Integer> refreshInterval;

  private Thread screenRefresher;
  private Stage stage;
  private IMonitoringView monitoringView;
  private boolean refreshEnabled = true;

  private File folder;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onScreenshot() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");

    if (folder != null) {
      chooser.setInitialDirectory(folder);
    }
    File targetFolder = chooser.showDialog(stage);

    if (targetFolder != null && targetFolder.exists()) {
      this.folder = targetFolder;
      ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new ScreenshotsDownloadProgressModel("Download Screenshots", targetFolder));
      if (!resultModel.getResults().isEmpty()) {
        File target = (File) resultModel.getResults().get(0);
        WidgetFactory.showInformation(stage, "Screenshots Generated", "Downloaded \"" + target.getAbsolutePath() + "\".");
      }
    }
  }

  @FXML
  private void zoomOut() {
    scaling = scaling - 0.1;
    if (scaling < MIN_ZOOM) {
      scaling = MIN_ZOOM;
    }
    monitoringView.setZoom(scaling);

    Platform.runLater(() -> {
      refreshPreview();
      saveScaling();
    });
  }

  @FXML
  private void zoomIn() {
    scaling = scaling + 0.1;
    if (scaling > 1) {
      scaling = 1;
    }
    monitoringView.setZoom(scaling);

    Platform.runLater(() -> {
      refreshPreview();
      saveScaling();
    });
  }

  @FXML
  private void onClearCache() {
    client.getFrontendService().clearCache();
  }

  private void saveScaling() {
    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    settings.setScaling(scaling);
    client.getPreferenceService().setJsonPreference(settings);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    settings.setOpen(true);
    scaling = settings.getScaling();
    client.getPreferenceService().setJsonPreference(settings);

    monitoringModeCombo.setItems(FXCollections.observableList(Arrays.asList(MonitoringMode.values())));
    MonitoringMode monitoringMode = settings.getMonitoringMode();
    if (monitoringMode == null) {
      monitoringMode = MonitoringMode.frontendScreens;
    }
    monitoringModeCombo.setValue(monitoringMode);
    screenMenuButton.setDisable(monitoringMode.equals(MonitoringMode.monitors));
    monitoringModeCombo.valueProperty().addListener(new ChangeListener<MonitoringMode>() {
      @Override
      public void changed(ObservableValue<? extends MonitoringMode> observable, MonitoringMode oldValue, MonitoringMode newValue) {
        Platform.runLater(() -> {
          screenMenuButton.setDisable(newValue.equals(MonitoringMode.monitors));
          settings.setMonitoringMode(newValue);
          client.getPreferenceService().setJsonPreference(settings);
          updateMonitoringMode(newValue);
        });
      }
    });
  }

  public void setData(Stage stage) {
    this.stage = stage;

    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, settings.getRefreshInterval());
    refreshInterval.setValueFactory(factory);
    refreshInterval.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("refresh", () -> {
        refreshPreview();
        settings.setRefreshInterval(newValue.intValue());
        MonitoringManager.getInstance().setMonitoringRefreshIntervalSec(refreshInterval.getValue());
        client.getPreferenceService().setJsonPreference(settings);
      }, 300);
    });

    List<FrontendPlayerDisplay> recordingScreens = client.getRecorderService().getRecordingScreens();
    for (FrontendPlayerDisplay recordingScreen : recordingScreens) {
      VPinScreen screen = recordingScreen.getScreen();
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox();
      checkBox.setText(recordingScreen.getName());
      checkBox.getStyleClass().add("default-text");
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(!settings.getDisabledScreens().contains(screen));
      item.setContent(checkBox);
      item.setGraphic(WidgetFactory.createIcon("mdi2m-monitor"));
      item.setOnAction(actionEvent -> {
        MonitoringSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
        if (!checkBox.isSelected()) {
          if (!s.getDisabledScreens().contains(screen)) {
            s.getDisabledScreens().add(screen);
          }
        }
        else {
          s.getDisabledScreens().remove(screen);
        }
        client.getPreferenceService().setJsonPreference(s);
        refreshPreviewPanelVisibilities();
      });
      screenMenuButton.getItems().add(item);
    }


    stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        refreshPreview();
      }
    });

    stage.heightProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        refreshPreview();
      }
    });

    stage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        refreshEnabled = false;
        MonitoringManager.getInstance().setMonitoringRefreshIntervalSec(Integer.MAX_VALUE);

        MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
        settings.setOpen(false);
        client.getPreferenceService().setJsonPreference(settings);

        ToolbarController.INSTANCE.onMonitorClose();
      }
    });

    screenRefresher = new Thread(() -> {
      try {
        LOG.info("Cabinet Monitor Refresh");
        while (refreshEnabled) {
          Platform.runLater(() -> {
            refreshPreview();
          });

          Thread.sleep(500);
        }
        LOG.info("Cab monitoring thread exited.");
      }
      catch (Exception e) {
        LOG.error("Error in monitor refresh thread: " + e.getMessage(), e);
      }
      finally {
        LOG.info("Exited monitor refresh thread.");
      }
    });
    screenRefresher.start();

    updateMonitoringMode(monitoringModeCombo.getValue());
    refreshPreviewPanelVisibilities();
    MonitoringManager.getInstance().setMonitoringRefreshIntervalSec(refreshInterval.getValue());
  }

  private void updateMonitoringMode(MonitoringMode value) {
    if (monitoringView != null) {
      monitoringView.dispose();
    }

    switch (value) {
      case monitors: {
        monitoringView = new MonitorsView(Studio.stage, this, scrollPane);
        break;
      }
      case frontendScreens: {
        monitoringView = new ScreensView(stage, this, scrollPane);
        break;
      }
    }
    refreshPreviewPanelVisibilities();
  }

  private void refreshPreviewPanelVisibilities() {
    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    List<VPinScreen> disabledScreens = settings.getDisabledScreens();
    monitoringView.updateScreens(disabledScreens);
  }

  private void refreshPreview() {
    debouncer.debounce("resizeMonitors", () -> {
      Platform.runLater(() -> {
        monitoringView.refresh();
      });
    }, DEBOUNCE_MS);
  }

  @Override
  public void onDialogCancel() {

  }
}
