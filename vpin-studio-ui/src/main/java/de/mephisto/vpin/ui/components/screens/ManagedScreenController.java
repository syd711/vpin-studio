package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
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
  public static final int OFFSET = 390;
  private final Debouncer debouncer = new Debouncer();

  private static final String COLOR_MONITOR = "#CCCCCC";
  private static final String COLOR_FRONTEND_SCREEN = "#9999FF";
  private static final String COLOR_SCREENRES_SCREEN = "#33FF33";
  private static final String COLOR_VPX_SCREEN = "#66AA11";

  @FXML
  private Pane previewCanvas;

  @FXML
  private BorderPane root;

  @FXML
  private CheckBox vpxPlayfieldCheckbox;

  @FXML
  private CheckBox showAllFrontendCheckbox;

  @FXML
  private CheckBox showAllScreenResCheckbox;

  @FXML
  private VBox frontendPanel;

  @FXML
  private VBox screenResPanel;

  @FXML
  private FontIcon frontendScreenIcon;

  @FXML
  private FontIcon screenResScreenIcon;

  @FXML
  private FontIcon vpxScreenIcon;

  private double zoom = 1;

  private List<CheckBox> frontendScreensCheckboxes = new ArrayList<>();
  private List<CheckBox> screenResScreensCheckboxes = new ArrayList<>();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Studio.stage.widthProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
      refreshPreview();
    }));
    Studio.stage.heightProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
      refreshPreview();
    }));

    frontendScreenIcon.setIconColor(Paint.valueOf(COLOR_FRONTEND_SCREEN));
    screenResScreenIcon.setIconColor(Paint.valueOf(COLOR_SCREENRES_SCREEN));
    vpxScreenIcon.setIconColor(Paint.valueOf(COLOR_VPX_SCREEN));

    FrontendScreenSummary screenSummary = client.getFrontendService().getScreenSummary(false);
    renderScreenCheckboxes(screenSummary.getFrontendDisplays(), frontendPanel, frontendScreensCheckboxes);
    renderScreenCheckboxes(screenSummary.getScreenResDisplays(), screenResPanel, screenResScreensCheckboxes);

    List<FrontendPlayerDisplay> vpxDisplaysDisplays = screenSummary.getVpxDisplaysDisplays();
    if (!vpxDisplaysDisplays.isEmpty()) {
      FrontendPlayerDisplay frontendPlayerDisplay = vpxDisplaysDisplays.get(0);
      vpxPlayfieldCheckbox.setText(frontendPlayerDisplay.getName() + " [" + frontendPlayerDisplay.getWidth() + "x" + frontendPlayerDisplay.getHeight() + "]");
      if (frontendPlayerDisplay.getWidth() == 0) {
        vpxPlayfieldCheckbox.setSelected(false);
        vpxPlayfieldCheckbox.setDisable(true);
      }
    }

    vpxPlayfieldCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refreshPreview();
      }
    });

    showAllFrontendCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        frontendScreensCheckboxes.stream().forEach(c -> c.setSelected(newValue));
        refreshPreview();
      }
    });

    showAllScreenResCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        screenResScreensCheckboxes.stream().forEach(c -> c.setSelected(newValue));
        refreshPreview();
      }
    });


    try {
      Thread.sleep(200);
      refreshPreview();
    }
    catch (InterruptedException e) {
      //ignore
    }
  }

  private void renderScreenCheckboxes(List<FrontendPlayerDisplay> frontendDisplays, VBox parent, List<CheckBox> result) {
    for (FrontendPlayerDisplay frontendDisplay : frontendDisplays) {
      CheckBox screenCheckbox = new CheckBox();
      screenCheckbox.setSelected(true);
      screenCheckbox.setUserData(frontendDisplay);
      screenCheckbox.getStyleClass().add("default-text");
      screenCheckbox.setText(frontendDisplay.getName() + " [" + frontendDisplay.getWidth() + "x" + frontendDisplay.getHeight() + "]");
      screenCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshPreview());
      parent.getChildren().add(screenCheckbox);
      result.add(screenCheckbox);
    }
  }

  public void reload() {
    refreshPreview();
  }

  private void refreshPreview() {
    this.previewCanvas.setPrefWidth((Studio.stage.getWidth() - OFFSET));

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();
    List<MonitorInfo> monitorInfos = systemSummary.getScreenInfos();
    previewCanvas.getChildren().removeAll(previewCanvas.getChildren());

    int x = 0;
    int canvasMaxHeight = 0;
    int canvasTotalWidth = 0;
    for (MonitorInfo monitorInfo : monitorInfos) {
      canvasTotalWidth += monitorInfo.getWidth();
    }

    double percentage = (this.previewCanvas.getPrefWidth() * 100 / canvasTotalWidth) / 100;
    for (MonitorInfo monitorInfo : monitorInfos) {
      double width = monitorInfo.getWidth() * percentage * zoom;
      double height = monitorInfo.getHeight() * percentage * zoom;
//      if (height > width) {
//        width = monitorInfo.getHeight() * percentage * zoom;
//        height = monitorInfo.getWidth() * percentage * zoom;
//      }

      drawScreenCanvas(monitorInfo.toString(), x, 0, (int) width, (int) height, COLOR_MONITOR, 36);
      if (height > canvasMaxHeight) {
        canvasMaxHeight = (int) height;
      }

      x = (int) (x + (width));
    }

    previewCanvas.setPrefHeight(canvasMaxHeight);
    previewCanvas.setMinHeight(canvasMaxHeight);


    FrontendScreenSummary screenSummary = client.getFrontendService().getScreenSummary(false);
    drawScreens(screenSummary.getFrontendDisplays(), canvasTotalWidth, COLOR_FRONTEND_SCREEN, 16, frontendScreensCheckboxes);
    drawScreens(screenSummary.getScreenResDisplays(), canvasTotalWidth, COLOR_SCREENRES_SCREEN, 16, screenResScreensCheckboxes);

    if (vpxPlayfieldCheckbox.isSelected()) {
      drawScreens(screenSummary.getVpxDisplaysDisplays(), canvasTotalWidth, COLOR_VPX_SCREEN, 16, Collections.emptyList());
    }

    previewCanvas.requestLayout();
    root.requestLayout();
  }

  private void drawScreens(List<FrontendPlayerDisplay> frontendDisplays, int canvasWidthTotal, String color, int textYOffset, List<CheckBox> checkboxes) {
    for (FrontendPlayerDisplay frontendDisplay : frontendDisplays) {
      Optional<CheckBox> first = checkboxes.stream().filter(c -> c.getUserData().equals(frontendDisplay)).findFirst();
      if (first.isPresent() && !first.get().isSelected()) {
        continue;
      }

      double scalingPercentage = (previewCanvas.getPrefWidth() * 100 / canvasWidthTotal) / 100;
      int x = (int) (frontendDisplay.getX() * scalingPercentage * zoom);
      int y = (int) (frontendDisplay.getY() * scalingPercentage * zoom);
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
