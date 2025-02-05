package de.mephisto.vpin.ui.components.screens;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ManagedScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManagedScreenController.class);
  public static final int OFFSET = 600;
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Pane previewCanvas;

  @FXML
  private Label titleLabel;

  private MonitorInfo monitorInfo;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setData(MonitorInfo monitorInfo) {
    this.monitorInfo = monitorInfo;
    this.previewCanvas.setPrefWidth(Studio.stage.getWidth() - OFFSET);

    Studio.stage.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        previewCanvas.setPrefWidth(newValue.intValue() - OFFSET);
        refreshPreview();
      }
    });

    Platform.runLater(() -> {
      refreshPreview();
    });
  }

  private void refreshPreview() {
    titleLabel.setText(monitorInfo.getName());

    double width = monitorInfo.getWidth();
    double height = monitorInfo.getHeight();

    if (height > width) {
      width = monitorInfo.getHeight();
      height = monitorInfo.getWidth();
    }

    double targetWidth = (previewCanvas.getWidth() - 12);
    double targetHeight = (previewCanvas.getHeight() - 12);

    double facWidth = width / height;

    while (width > targetWidth || height > targetHeight) {
      height = height - 1;
      width = height * facWidth;
    }

    Rectangle rect = new Rectangle(6, 6, (int) width, (int) height);
    rect.setStroke(Color.GREEN);
    rect.setStrokeWidth(2);

    previewCanvas.getChildren().removeAll(previewCanvas.getChildren());
    previewCanvas.getChildren().add(rect);
    previewCanvas.requestLayout();
  }
}
