package de.mephisto.vpin.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class SplashScreenController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(SplashScreenController.class);

  public final static int FACT_COUNT = 62;

  @FXML
  private ImageView splashImage;

  @FXML
  private Label versionLabel;

  @FXML
  private Label factLabel;

  @FXML
  private Label statusLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    versionLabel.setText("Version " + Studio.getVersion());
    factLabel.setText("");
    try {
      int i = ThreadLocalRandom.current().nextInt(1, FACT_COUNT + 1);
      String fact = resources.getString("studio.splash.fact." + i);
      factLabel.setText("\"" + fact + "\"");
    }
    catch (Exception e) {
      LOG.error("Splash screen init failed: {}", e.getMessage(), e);
    }
  }

  public void setImage(Image image) {
    splashImage.setImage(image);
  }

  public void setStatus(String status) {
    Platform.runLater(() -> {
      statusLabel.setText(status);
    });
  }
}
