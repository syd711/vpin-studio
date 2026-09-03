package de.mephisto.vpin.ui.monitor.panels;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.monitor.PlayfieldRotation;
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
    screenName.setText(Messages.get("monitor.screen_monitor_panel.monitor_dimensions", monitorInfo.getId(), monitorInfo.getWidth(), monitorInfo.getHeight()));
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

  public boolean isCurrentlyVisible() {
    return root.isVisible();
  }

  /**
   * Combined horizontal space eaten up inside this panel by fixed FXML insets
   * (the root VBox's BorderPane.margin, the media-container's padding, and the
   * ImageView's BorderPane.margin) between the panel's own width and the image.
   * Keep in sync with screen-monitor-panel.fxml.
   */
  private static final double PANEL_INTERNAL_OVERHEAD = 28;

  /**
   * Refreshes the panel using an explicit pixel width rather than deriving it
   * from the stage size. Used by StreamingView to lay out the two-column layout.
   *
   * @param widthPx  available width in pixels for this panel (i.e. the width
   *                 StreamingView's column actually hands to this panel, after
   *                 the column's own padding)
   * @param rotation rotation applied to the captured (landscape) image; 90°/270°
   *                 swap the box to a portrait aspect, 0°/180° keep it landscape
   */
  public void refreshWithSize(double widthPx, PlayfieldRotation rotation) {
    if (!root.isVisible()) {
      return;
    }

    // The default binding sizes this panel for the single-column ScreensView layout;
    // unbind it so the BorderPane can stretch the panel to the width StreamingView
    // actually assigns it instead.
    if (root.prefWidthProperty().isBound()) {
      root.prefWidthProperty().unbind();
    }

    double availableW = widthPx - PANEL_INTERNAL_OVERHEAD;
    int degrees = rotation.getDegrees();
    boolean swapped = degrees == 90 || degrees == 270;

    // Always use the full available width so the preview is as large as possible;
    // the manual zoom slider is ignored here (it only applies to the other views) and
    // the surrounding scroll pane accommodates whatever height that produces.
    // Sizes are computed pre-rotation (assuming a 16:9 source), since setRotate()
    // below swaps the rendered width/height for a 90°/270° turn.
    double fitW;
    double fitH;
    if (swapped) {
      fitH = availableW;
      fitW = fitH * 16.0 / 9.0;
    }
    else {
      fitW = availableW;
      fitH = fitW * 9.0 / 16.0;
    }

    imageView.setPreserveRatio(true);
    imageView.setFitWidth(fitW);
    imageView.setFitHeight(fitH);
    imageView.setRotate(degrees);

    if (recordingScreen != null) {
      Image image = MonitoringManager.getInstance().getRecordableScreenImage(recordingScreen);
      imageView.setImage(image);
    }
    else if (monitorInfo != null) {
      Image image = MonitoringManager.getInstance().getMonitorImage(monitorInfo);
      imageView.setImage(image);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
    imageView.managedProperty().bindBidirectional(imageView.visibleProperty());
  }

}
