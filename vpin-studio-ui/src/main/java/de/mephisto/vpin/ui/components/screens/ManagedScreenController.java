package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.FrontendScreenSummary;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class ManagedScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManagedScreenController.class);
  public static final int OFFSET = 410;

  private static final String COLOR_MONITOR = "#CCCCCC";
  private static final String COLOR_FRONTEND_SCREEN = "#9999FF";
  private static final String COLOR_SCREENRES_SCREEN = "#33FF33";
  private static final String COLOR_VPX_SCREEN = "#AA6611";

  @FXML
  private ScrollPane scrollPane;

  @FXML
  private Pane previewCanvas;

  @FXML
  private BorderPane root;

  @FXML
  private CheckBox showAllFrontendCheckbox;

  @FXML
  private CheckBox showAllScreenResCheckbox;

  @FXML
  private CheckBox showAllVpxCheckbox;

  @FXML
  private VBox frontendPanel;

  @FXML
  private VBox screenResPanel;

  @FXML
  private VBox vpxPanel;

  @FXML
  private FontIcon frontendScreenIcon;

  @FXML
  private FontIcon screenResScreenIcon;

  @FXML
  private FontIcon vpxScreenIcon;

  @FXML
  private VBox validationError;

  private double zoom = 1;

  private final List<CheckBox> frontendScreensCheckboxes = new ArrayList<>();
  private final List<CheckBox> screenResScreensCheckboxes = new ArrayList<>();
  private final List<CheckBox> vpxScreensCheckboxes = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    validationError.setVisible(false);

    vpxPanel.managedProperty().bindBidirectional(vpxPanel.visibleProperty());

    Studio.stage.widthProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
      refreshPreview(false);
    }));
    Studio.stage.heightProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
      refreshPreview(false);
    }));

    frontendScreenIcon.setIconColor(Paint.valueOf(COLOR_FRONTEND_SCREEN));
    screenResScreenIcon.setIconColor(Paint.valueOf(COLOR_SCREENRES_SCREEN));
    vpxScreenIcon.setIconColor(Paint.valueOf(COLOR_VPX_SCREEN));

    showAllFrontendCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        frontendScreensCheckboxes.stream().forEach(c -> c.setSelected(newValue));
        refreshPreview(false);
      }
    });

    showAllScreenResCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        screenResScreensCheckboxes.stream().forEach(c -> c.setSelected(newValue));
        refreshPreview(false);
      }
    });

    showAllVpxCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        vpxScreensCheckboxes.stream().forEach(c -> c.setSelected(newValue));
        refreshPreview(false);
      }
    });

    refreshPreview(true);
  }

  private void renderScreenCheckboxes(List<FrontendPlayerDisplay> frontendDisplays, VBox parent, List<CheckBox> result, CheckBox showAllCheckbox) {

    // If there is only one display, hide the 'showAll' checkbox
    parent.getChildren().get(1).setManaged(frontendDisplays.size() > 1);
    parent.getChildren().get(1).setVisible(frontendDisplays.size() > 1);

    // remove all checkboxes if any
    while (parent.getChildren().size() > 2) {
      parent.getChildren().remove(2);
    }
    result.clear();

    for (FrontendPlayerDisplay frontendDisplay : frontendDisplays) {
      CheckBox screenCheckbox = new CheckBox();
      screenCheckbox.setSelected(showAllCheckbox.isSelected());
      screenCheckbox.setUserData(frontendDisplay);
      screenCheckbox.getStyleClass().add("default-text");
      screenCheckbox.setText(frontendDisplay.getName() + " [" + frontendDisplay.getWidth() + "x" + frontendDisplay.getHeight() + "]");
      screenCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshPreview(false));
      parent.getChildren().add(screenCheckbox);
      result.add(screenCheckbox);
    }
  }

  public void reload() {
    refreshPreview(true);
  }

  /**
   * Reload / redraw the preview
   */
  private void refreshPreview(boolean reloadData) {
    this.previewCanvas.setPrefWidth((Studio.stage.getWidth() - OFFSET));

    SystemSummary systemSummary = client.getSystemService().getSystemSummary(reloadData);
    List<MonitorInfo> monitorInfos = systemSummary.getMonitorInfos();
    previewCanvas.getChildren().removeAll(previewCanvas.getChildren());

    double canvasMinX = Integer.MAX_VALUE, canvasMinY = Integer.MAX_VALUE;
    double canvasMaxX = Integer.MIN_VALUE, canvasMaxY = Integer.MIN_VALUE;
    for (MonitorInfo monitorInfo : monitorInfos) {
      canvasMinX = Math.min(canvasMinX, monitorInfo.getX());
      canvasMinY = Math.min(canvasMinY, monitorInfo.getY());
      canvasMaxX = Math.max(canvasMaxX, monitorInfo.getX() + monitorInfo.getWidth());
      canvasMaxY = Math.max(canvasMaxY, monitorInfo.getY() + monitorInfo.getHeight());
    }
    double canvasTotalWidth = canvasMaxX - canvasMinX;
    double canvasTotalHeight = canvasMaxY - canvasMinY;
    double percentage = Math.min(
        (scrollPane.getWidth() * 100 / canvasTotalWidth) / 100,
        (scrollPane.getHeight() * 100 / canvasTotalHeight) / 100);

    for (MonitorInfo monitorInfo : monitorInfos) {
      double width = monitorInfo.getWidth() * percentage * zoom;
      double height = monitorInfo.getHeight() * percentage * zoom;
      double x = (monitorInfo.getX() - canvasMinX) * percentage * zoom;
      double y = (monitorInfo.getY() - canvasMinY) * percentage * zoom;

      drawScreenCanvas("Monitor \"" + monitorInfo.getFormattedName() + "\"", (int) x, (int) y, (int) width, (int) height, COLOR_MONITOR, 36);
      Text text = new Text(x + 36 - 12, y + 52, monitorInfo.getWidth() + "x" + monitorInfo.getHeight());
      Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 14);
      text.setFont(defaultFont);
      text.setStroke(Color.valueOf(COLOR_MONITOR));
      text.setFill(Color.valueOf(COLOR_MONITOR));
      previewCanvas.getChildren().add(text);
    }

    previewCanvas.setPrefWidth(canvasTotalWidth * percentage * zoom);
    previewCanvas.setMinWidth(canvasTotalWidth * percentage * zoom);

    previewCanvas.setPrefHeight(canvasTotalHeight * percentage * zoom);
    previewCanvas.setMinHeight(canvasTotalHeight * percentage * zoom);

    FrontendScreenSummary screenSummary = client.getFrontendService().getScreenSummary(reloadData);

    vpxPanel.setVisible(!screenSummary.getVpxDisplaysDisplays().isEmpty());
    if (reloadData) {
      renderScreenCheckboxes(screenSummary.getFrontendDisplays(), frontendPanel, frontendScreensCheckboxes, showAllFrontendCheckbox);
      renderScreenCheckboxes(screenSummary.getScreenResDisplays(), screenResPanel, screenResScreensCheckboxes, showAllScreenResCheckbox);
      renderScreenCheckboxes(screenSummary.getVpxDisplaysDisplays(), vpxPanel, vpxScreensCheckboxes, showAllVpxCheckbox);
    }

    drawScreens(screenSummary.getFrontendDisplays(), canvasMinX, canvasMinY, percentage, COLOR_FRONTEND_SCREEN, 16, frontendScreensCheckboxes);
    drawScreens(screenSummary.getScreenResDisplays(), canvasMinX, canvasMinY, percentage, COLOR_SCREENRES_SCREEN, 16, screenResScreensCheckboxes);
    drawScreens(screenSummary.getVpxDisplaysDisplays(), canvasMinX, canvasMinY, percentage, COLOR_VPX_SCREEN, 16, vpxScreensCheckboxes);

    if (reloadData) {
      addErrors(screenSummary.getErrors(), validationError);
    }

    previewCanvas.requestLayout();
    root.requestLayout();
  }

  private void addErrors(List<String> errors, VBox errorsVbox) {
    errorsVbox.getChildren().clear();
    validationError.setVisible(false);
    for (String error : errors) {
      Label l = new Label(error);
      l.getStyleClass().add("error-title");
      errorsVbox.getChildren().add(l);
      validationError.setVisible(true);
    }
  }

  private void drawScreens(List<FrontendPlayerDisplay> frontendDisplays, double canvasMinX, double canvasMinY, double scalingPercentage,
                           String color, int textYOffset, List<CheckBox> checkboxes) {
    for (FrontendPlayerDisplay frontendDisplay : frontendDisplays) {
      Optional<CheckBox> first = checkboxes.stream().filter(c -> c.getUserData().equals(frontendDisplay)).findFirst();
      if (first.isPresent() && !first.get().isSelected()) {
        continue;
      }

      int x = (int) ((frontendDisplay.getX() - canvasMinX) * scalingPercentage * zoom);
      int y = (int) ((frontendDisplay.getY() - canvasMinY) * scalingPercentage * zoom);
      int w = (int) (frontendDisplay.getWidth() * scalingPercentage * zoom);
      int h = (int) (frontendDisplay.getHeight() * scalingPercentage * zoom);

      drawScreenCanvas(frontendDisplay.getName(), x, y, w, h, color, textYOffset);
    }
  }

  private void drawScreenCanvas(String name, int x, int y, int width, int height, String color, int textYOffset) {
    Rectangle rect = new Rectangle(x, y, width, height);
    rect.setStroke(Color.valueOf(color));
    rect.setFill(Color.TRANSPARENT);
    rect.setStrokeWidth(1);

    Text text = new Text(x + textYOffset - 12, y + textYOffset, name);
    Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 14);
    text.setFont(defaultFont);
    text.setStroke(Color.valueOf(color));
    text.setFill(Color.valueOf(color));

    previewCanvas.getChildren().add(rect);
    previewCanvas.getChildren().add(text);
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
    this.reload();
  }
}
