package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.ui.monitor.panels.ScreenMonitorPanelController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CabMonitorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(CabMonitorController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  private double scaling = 1;

  private List<ScreenMonitorPanelController> controllers = new ArrayList<>();

  @FXML
  private Pane previewArea;

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
    for (ScreenMonitorPanelController controller : controllers) {
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
    for (ScreenMonitorPanelController controller : controllers) {
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
        controllers.add(screenPanelController);
        screenPanelController.setData(stage, this, recordingScreen);
        previewArea.getChildren().add(panelRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load monitoring panel: " + e.getMessage(), e);
      }
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
  }

  private void refreshPreview() {
    debouncer.debounce("resizeMonitors", () -> {
      Platform.runLater(() -> {
        for (ScreenMonitorPanelController controller : controllers) {
          controller.refresh();
        }
      });
    }, DEBOUNCE_MS);
  }

  @Override
  public void onDialogCancel() {

  }
}
