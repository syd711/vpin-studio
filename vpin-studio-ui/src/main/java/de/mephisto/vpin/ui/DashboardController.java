package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.widgets.LatestScoresWidgetController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(DashboardController.class);

  @FXML
  private BorderPane widget1;
  private Parent root;

  // Add a public no-args constructor
  public DashboardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    try {
      FXMLLoader loader = new FXMLLoader(LatestScoresWidgetController.class.getResource("widget-latest-scores.fxml"));
      root = loader.load();
      widget1.setCenter(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Platform.runLater(() -> {

      try {
        Thread.sleep(1000);
//        root.set(2000);
//        root.setMinHeight(1000);
        WritableImage snapshot = root.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_ARGB);
        File file = new File("E:/temp/test.jpg");
        BufferedImage image = SwingFXUtils.fromFXImage(snapshot, bufferedImage);
        Graphics2D gd = (Graphics2D) image.getGraphics();
        gd.translate(widget1.getWidth(), widget1.getHeight());
        ImageIO.write(image, "png", file);
      } catch (Exception ex) {
        LOG.error("Error generating thump: " + ex, ex);
      }
    });


  }

}