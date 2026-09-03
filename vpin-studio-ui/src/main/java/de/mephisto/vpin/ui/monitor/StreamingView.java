package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.monitor.PlayfieldRotation;
import de.mephisto.vpin.ui.monitor.panels.ScreenPanelController;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

  /** Gap between the left (PlayField) and right (other screens) columns. */
  private static final double COLUMN_GAP = 4;
  /** Padding inside each column, on every side. */
  private static final double COLUMN_PADDING = 4;
  /** Vertical gap between the stacked panels in the right column. */
  private static final double ROW_GAP = 4;
  /** Fraction of the available width given to the left (PlayField) column. */
  private static final double LEFT_COLUMN_RATIO = 0.40;

  private final Stage stage;
  private final ScrollPane scrollPane;
  private final HBox root;

  /** Controller for the PlayField panel (left column) */
  private ScreenPanelController playFieldController;
  private PlayfieldRotation playfieldRotation = PlayfieldRotation.ROTATE_0;
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
    this.playfieldRotation = settings.getPlayfieldRotation();

    List<FrontendPlayerDisplay> recordingScreens = client.getRecorderService().getRecordingScreens();

    // --- Left column: PlayField ---
    VBox leftColumn = new VBox();
    leftColumn.setStyle("-fx-padding: " + COLUMN_PADDING + ";");

    // --- Right column: all other screens ---
    VBox rightColumn = new VBox(ROW_GAP);
    rightColumn.setStyle("-fx-padding: " + COLUMN_PADDING + ";");

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

    // Build two-column HBox, columns fixed to an exact 40/60 split of the available
    // width so the width StreamingView hands down to each panel (see refresh()) always
    // matches what the columns actually render - avoids drift that causes horizontal scrolling.
    root = new HBox(COLUMN_GAP);
    leftColumn.prefWidthProperty().bind(availableContentWidth().multiply(LEFT_COLUMN_RATIO));
    rightColumn.prefWidthProperty().bind(availableContentWidth().multiply(1 - LEFT_COLUMN_RATIO));
    leftColumn.setMaxWidth(Region.USE_PREF_SIZE);
    rightColumn.setMaxWidth(Region.USE_PREF_SIZE);
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
  public void setPlayfieldRotation(PlayfieldRotation rotation) {
    this.playfieldRotation = rotation;
    refresh();
  }

  /**
   * Width remaining for the two columns once the scroll pane's own reserve and the
   * gap between the columns are subtracted. Used both to bind the columns' actual
   * width and to compute the width handed down to each panel, so the two can never
   * drift apart and cause horizontal scrolling.
   */
  private DoubleBinding availableContentWidth() {
    return scrollPane.widthProperty().subtract(20).subtract(COLUMN_GAP);
  }

  @Override
  public void refresh() {
    double contentW = Math.max(0, scrollPane.getWidth() - 20 - COLUMN_GAP);

    // Left column: portrait playfield; right column: remaining screens, stacked
    double leftColumnW = contentW * LEFT_COLUMN_RATIO;
    double rightColumnW = contentW * (1 - LEFT_COLUMN_RATIO);

    // Width actually available to the panel once the column's own padding is subtracted
    double leftPanelW = Math.max(0, leftColumnW - 2 * COLUMN_PADDING);
    double rightPanelW = Math.max(0, rightColumnW - 2 * COLUMN_PADDING);

    if (playFieldController != null && playFieldController.isCurrentlyVisible()) {
      playFieldController.refreshWithSize(leftPanelW, playfieldRotation);
    }
    for (ScreenPanelController ctrl : otherControllers.values()) {
      if (ctrl.isCurrentlyVisible()) {
        ctrl.refreshWithSize(rightPanelW, PlayfieldRotation.ROTATE_0);
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
