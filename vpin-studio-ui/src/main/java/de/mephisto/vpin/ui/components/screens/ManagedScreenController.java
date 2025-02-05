package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.commons.fx.Debouncer;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ManagedScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManagedScreenController.class);
  public static final int OFFSET = 760;
  private final Debouncer debouncer = new Debouncer();

  private static final String COLOR_MONITOR = "#CCCCCC";
  private static final String COLOR_FRONTEND_SCREEN = "#3333FF";
  private static final String COLOR_SCREENRES_SCREEN = "#33FF33";

  @FXML
  private Pane previewCanvas;

  @FXML
  private BorderPane root;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Studio.stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        refreshPreview();
      }
    });

    Platform.runLater(() -> {
      refreshPreview();
    });
  }

  public void reload() {
    refreshPreview();
  }

  private void refreshPreview() {
    this.previewCanvas.setPrefWidth(Studio.stage.getWidth() - OFFSET);

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();
    List<MonitorInfo> monitorInfos = systemSummary.getScreenInfos();
    previewCanvas.getChildren().removeAll(previewCanvas.getChildren());

    int canvasHeightTotal = 0;
    int canvasWidthTotal = 0;
    for (MonitorInfo monitorInfo : monitorInfos) {
      double width = monitorInfo.getWidth();
      double height = monitorInfo.getHeight();

//      if (height > width) {
//        width = monitorInfo.getHeight();
//        height = monitorInfo.getWidth();
//      }

      double targetWidth = (previewCanvas.getWidth());
      double targetHeight = (previewCanvas.getHeight());

      double facWidth = width / height;

      while (width > targetWidth || height > targetHeight) {
        height = height - 1;
        width = height * facWidth;
      }

      drawScreenCanvas(monitorInfo.toString(), 0, canvasHeightTotal, (int) width, (int) height, COLOR_MONITOR);

      canvasHeightTotal += height;
      canvasWidthTotal += width;
    }

    previewCanvas.setPrefHeight(canvasHeightTotal);
    previewCanvas.setMinHeight(canvasHeightTotal);


    FrontendScreenSummary screenSummary = client.getFrontendService().getScreenSummary();
    drawScreens(screenSummary.getFrontendDisplays(), canvasWidthTotal, COLOR_FRONTEND_SCREEN);
    drawScreens(screenSummary.getScreenResDisplays(), canvasWidthTotal, COLOR_SCREENRES_SCREEN);

    root.requestLayout();
  }

  private void drawScreens(List<FrontendPlayerDisplay> frontendDisplays, int canvasWidthTotal, String color) {
    for (FrontendPlayerDisplay frontendDisplay : frontendDisplays) {
      double scalingPercentage = previewCanvas.getWidth() * 100 / canvasWidthTotal;
      double scalingFactor = scalingPercentage / 100;
      int x = (int) (frontendDisplay.getX() * scalingFactor);
      int y = (int) (frontendDisplay.getY() * scalingFactor);
      int w = (int) (frontendDisplay.getWidth() * scalingFactor);
      int h = (int) (frontendDisplay.getHeight() * scalingFactor);

      drawScreenCanvas(frontendDisplay.getName(), x, y, w, h, color);
    }
  }

  private void drawScreenCanvas(String name, int x, int y, int width, int height, String color) {
    Rectangle rect = new Rectangle(x, y, width, height);
    rect.setStroke(Color.valueOf(color));
    rect.setFill(Color.TRANSPARENT);
    rect.setStrokeWidth(1);

    Text text = new Text(x + 6, y + 18, name);
    Font defaultFont = Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 14);
    text.setFont(defaultFont);
    text.setStroke(Color.valueOf(color));
    text.setFill(Color.valueOf(color));

    previewCanvas.getChildren().add(rect);
    previewCanvas.getChildren().add(text);
  }
}
