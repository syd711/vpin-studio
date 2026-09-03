package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.ui.monitor.panels.ScreenPanelController;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Streaming View: two-column layout where the PlayField screen is shown in
 * portrait orientation on the left, and all remaining screens are stacked
 * vertically in the right column. Both columns scale with the window size.
 */
public class StreamingView implements IMonitoringView {
  private final static Logger LOG = LoggerFactory.getLogger(StreamingView.class);

  private final Stage stage;
  private final ScrollPane scrollPane;
  private final HBox root;

  /** Controller for the PlayField panel (left column) */
  private ScreenPanelController playFieldController;
  /** Controllers for all other screens (right column) */
  private final Map<VPinScreen, ScreenPanelController> otherControllers = new HashMap<>();

  /** Resize listener so we can remove it on dispose */
  private ChangeListener<Number> widthListener;
  private ChangeListener<Number> heightListener;

  public StreamingView(Stage stage, CabMonitorController recorderController, ScrollPane scrollPane) {
    this.stage = stage;
    this.scrollPane = scrollPane;

    MonitoringSettings settings = client.getPreferenceService().getJsonPreference(
        PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);

    List<FrontendPlayerDisplay> recordingScreens = client.getRecorderService().getRecordingScreens();

    // --- Left column: PlayField ---
    VBox leftColumn = new VBox();
    leftColumn.setStyle("-fx-padding: 6;");

    // --- Right column: all other screens ---
    VBox rightColumn = new VBox(6);
    rightColumn.setStyle("-fx-padding: 6;");

    for (FrontendPlayerDisplay display : recordingScreens) {
      try {
        FXMLLoader loader = new FXMLLoader(ScreenPanelController.class.getResource("screen-monitor-panel.fxml"));
        loader.setResources(Messages.getBundle());
        Parent panelRoot = loader.load();
        ScreenPanelController ctrl = loader.getController();
        ctrl.setZoom(settings.getScaling());
        ctrl.setData(stage, recorderController, display);

        if (display.getScreen() == VPinScreen.PlayField) {
          playFieldController = ctrl;
          VBox.setVgrow(panelRoot, Priority.ALWAYS);
          leftColumn.getChildren().add(panelRoot);
        }
        else {
          otherControllers.put(display.getScreen(), ctrl);
          rightColumn.getChildren().add(panelRoot);
        }
      }
      catch (IOException e) {
        LOG.error("Failed to load streaming view panel: " + e.getMessage(), e);
      }
    }

    // Build two-column HBox
    root = new HBox(6);
    HBox.setHgrow(leftColumn, Priority.ALWAYS);
    HBox.setHgrow(rightColumn, Priority.ALWAYS);
    leftColumn.setMaxWidth(Double.MAX_VALUE);
    rightColumn.setMaxWidth(Double.MAX_VALUE);
    root.getChildren().addAll(leftColumn, rightColumn);

    // Bind the root width/height to the scroll pane so it fills the viewport
    root.prefWidthProperty().bind(scrollPane.widthProperty().subtract(20));
    root.prefHeightProperty().bind(scrollPane.heightProperty().subtract(20));

    scrollPane.setContent(root);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    // Trigger a refresh whenever the window resizes
    widthListener = (obs, o, n) -> refresh();
    heightListener = (obs, o, n) -> refresh();
    stage.widthProperty().addListener(widthListener);
    stage.heightProperty().addListener(heightListener);
  }

  @Override
  public void updateScreens(List<VPinScreen> disabledScreens) {
    if (playFieldController != null) {
      playFieldController.setVisible(!disabledScreens.contains(VPinScreen.PlayField));
    }
    for (Map.Entry<VPinScreen, ScreenPanelController> entry : otherControllers.entrySet()) {
      entry.getValue().setVisible(!disabledScreens.contains(entry.getKey()));
    }
  }

  @Override
  public void setZoom(double zoom) {
    if (playFieldController != null) {
      playFieldController.setZoom(zoom);
    }
    for (ScreenPanelController ctrl : otherControllers.values()) {
      ctrl.setZoom(zoom);
    }
  }

  @Override
  public void refresh() {
    double totalW = stage.getWidth() - 30;
    double totalH = stage.getHeight() - 100;

    // Left column: portrait playfield takes ~40% of width, full height
    double leftW = totalW * 0.40;
    double leftH = totalH;

    // Right column: remaining width, screens share height equally
    double rightW = totalW * 0.60;
    int otherCount = (int) otherControllers.values().stream()
        .filter(c -> c.isCurrentlyVisible())
        .count();
    double rightH = otherCount > 0 ? totalH / otherCount : totalH;

    if (playFieldController != null && playFieldController.isCurrentlyVisible()) {
      playFieldController.refreshWithSize(leftW, leftH, true);
    }
    for (ScreenPanelController ctrl : otherControllers.values()) {
      if (ctrl.isCurrentlyVisible()) {
        ctrl.refreshWithSize(rightW, rightH, false);
      }
    }
  }

  @Override
  public void dispose() {
    stage.widthProperty().removeListener(widthListener);
    stage.heightProperty().removeListener(heightListener);
    root.getChildren().clear();
    scrollPane.setFitToWidth(false);
    scrollPane.setFitToHeight(false);
  }
}
