package de.mephisto.vpin.ui.monitor.panels;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.system.MonitorInfo;
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

  private FrontendPlayerDisplay recordingScreen;

  private Stage stage;
  private CabMonitorController recorderController;
  private MonitorInfo monitorInfo;
  private double scaling = 1;

  public VPinScreen getScreen() {
    return recordingScreen.getScreen();
  }

  public void setData(Stage stage, CabMonitorController recorderController, FrontendPlayerDisplay recordingScreen) {
    this.stage = stage;
    this.recorderController = recorderController;
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(960));

    this.recordingScreen = recordingScreen;
    screenName.setText(recordingScreen.getScreen().name());
    if (recordingScreen.getScreen().name().equalsIgnoreCase("Menu")) {
      screenName.setText(recordingScreen.getScreen().name() + "/FullDMD");
    }
    screenName.setText(screenName.getText() + " (" + recordingScreen.getWidth() + " x " + recordingScreen.getHeight() + ")");
    refresh();
  }

  public void setData(Stage stage, CabMonitorController recorderController, MonitorInfo monitorInfo) {
    this.stage = stage;
    this.recorderController = recorderController;
    this.monitorInfo = monitorInfo;
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(960));
    screenName.setText("Monitor #" + monitorInfo.getId() + " (" + monitorInfo.getWidth() + " x " + monitorInfo.getHeight() + ")");
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
        else if (monitorInfo != null) {
          Image image = MonitoringManager.getInstance().getMonitorImage(monitorInfo);
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
