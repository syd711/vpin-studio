package de.mephisto.vpin.ui.monitor.panels;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.system.ScreenInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.monitor.CabMonitorController;
import de.mephisto.vpin.ui.monitor.MonitoringManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ScreenPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenPanelController.class);

  @FXML
  Pane root;

  @FXML
  ImageView imageView;

  @FXML
  Label screenName;

  private RecordingScreen recordingScreen;

  private Stage stage;
  private CabMonitorController recorderController;
  private ScreenInfo screenInfo;
  private double scaling = 1;

  public VPinScreen getScreen() {
    return recordingScreen.getScreen();
  }

  public void setData(Stage stage, CabMonitorController recorderController, RecordingScreen recordingScreen) {
    this.stage = stage;
    this.recorderController = recorderController;
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(960));

    this.recordingScreen = recordingScreen;
    screenName.setText(recordingScreen.getScreen().name());
    if (recordingScreen.getScreen().name().equalsIgnoreCase("Menu")) {
      screenName.setText(recordingScreen.getScreen().name() + "/FullDMD");
    }
    screenName.setText(screenName.getText() + " (" + recordingScreen.getDisplay().getWidth() + " x " + recordingScreen.getDisplay().getHeight() + ")");
    refresh();
  }

  public void setData(Stage stage, CabMonitorController recorderController, ScreenInfo screenInfo) {
    this.stage = stage;
    this.recorderController = recorderController;
    this.screenInfo = screenInfo;
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(960));
    screenName.setText("Monitor #" + screenInfo.getId() + " (" + screenInfo.getOriginalWidth() + " x " + screenInfo.getOriginalHeight() + ")");
    refresh();
  }

  public void setZoom(double scaling) {
    this.scaling = scaling;
  }

  public void refresh() {
    if (root.isVisible()) {
      double width = stage.getWidth() - 72;
      double height = width * 9 / 16;
      if (height + 100 > stage.getHeight()) {
        height = stage.getHeight() - 100;
      }

      imageView.setPreserveRatio(true);
      imageView.setFitWidth(width * (scaling * 100) / 100);
      imageView.setFitHeight(height * (scaling * 100) / 100);

      if (root.isVisible()) {
        if (recordingScreen != null) {
          Image image = MonitoringManager.getInstance().getRecordableScreenImage(recordingScreen);
          imageView.setImage(image);
        }
        else if (screenInfo != null) {
          Image image = MonitoringManager.getInstance().getMonitorImage(screenInfo);
          imageView.setImage(image);
        }
      }
    }
  }

  public void setVisible(boolean b) {
    root.setVisible(b);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
    imageView.managedProperty().bindBidirectional(imageView.visibleProperty());
  }

}
