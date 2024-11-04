package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.ui.ToolbarController;
import de.mephisto.vpin.ui.monitor.panels.ScreenMonitorPanelController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CabMonitorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CabMonitorController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  private double scaling = 1;

  private final Map<VPinScreen, ScreenMonitorPanelController> controllers = new HashMap<>();

  @FXML
  private Pane previewArea;

  @FXML
  private MenuButton screenMenuButton;

  @FXML
  private Spinner<Integer> refreshInterval;
  private Thread screenRefresher;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void zoomOut() {
    scaling = scaling - 0.1;
    if (scaling < 0.4) {
      scaling = 0.4;
    }
    for (ScreenMonitorPanelController controller : controllers.values()) {
      controller.zoom(scaling);
    }

    Platform.runLater(() -> {
      refreshPreview();
    });
  }

  @FXML
  private void zoomIn() {
    scaling = scaling + 0.1;
    if (scaling > 1) {
      scaling = 1;
    }
    for (ScreenMonitorPanelController controller : controllers.values()) {
      controller.zoom(scaling);
    }

    Platform.runLater(() -> {
      refreshPreview();
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setData(Stage stage) {
    List<RecordingScreen> recordingScreens = client.getRecorderService().getRecordingScreens();
    for (RecordingScreen recordingScreen : recordingScreens) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenMonitorPanelController.class.getResource("screen-monitor-panel.fxml"));
        Parent panelRoot = loader.load();
        ScreenMonitorPanelController screenPanelController = loader.getController();
        controllers.put(recordingScreen.getScreen(), screenPanelController);
        screenPanelController.setData(stage, this, recordingScreen);
        previewArea.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load monitoring panel: " + e.getMessage(), e);
      }
    }


    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, settings.getRefreshInterval());
    refreshInterval.setValueFactory(factory);
    refreshInterval.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("refresh", () -> {
        refreshPreview();
        settings.setRefreshInterval(newValue.intValue());
        client.getPreferenceService().setJsonPreference(PreferenceNames.MONITORING_SETTINGS, settings);
      }, 300);
    });

    List<VPinScreen> supportedRecodingScreens = client.getFrontendService().getFrontendCached().getSupportedRecodingScreens();
    for (VPinScreen screen : supportedRecodingScreens) {
      CustomMenuItem item = new CustomMenuItem();
      CheckBox checkBox = new CheckBox();
      checkBox.setText(screen.getSegment());
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
        client.getPreferenceService().setJsonPreference(PreferenceNames.MONITORING_SETTINGS, s);
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
        ToolbarController.INSTANCE.onMonitorClose();
      }
    });

    screenRefresher = new Thread(() -> {
      try {
        LOG.info("Cabinet Monitor Refresh");
        while (true) {
          Platform.runLater(() -> {
            refreshPreview();
          });

          MonitoringSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
          Thread.sleep(s.getRefreshInterval() * 1000);
        }
      }
      catch (Exception e) {
        LOG.error("Error in monitor refresh thread: " + e.getMessage(), e);
      }
      finally {
        LOG.info("Exited monitor refresh thread.");
      }
    });
    screenRefresher.start();

    refreshPreviewPanelVisibilities();
  }

  private void refreshPreviewPanelVisibilities() {
    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    List<VPinScreen> disabledScreens = settings.getDisabledScreens();

    for (ScreenMonitorPanelController controller : controllers.values()) {
      controller.setVisible(!disabledScreens.contains(controller.getScreen()));
    }
  }

  private void refreshPreview() {
    debouncer.debounce("resizeMonitors", () -> {
      Platform.runLater(() -> {
        for (ScreenMonitorPanelController controller : controllers.values()) {
          controller.refresh();
        }
      });
    }, DEBOUNCE_MS);
  }

  @Override
  public void onDialogCancel() {

  }
}
