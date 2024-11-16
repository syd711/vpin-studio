package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.util.SystemUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MaintenanceController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MaintenanceController.class);

  @FXML
  private ImageView imageView;

  // Add a public no-args constructor
  public MaintenanceController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    Screen screen = SystemUtil.getScreenById(overlaySettings.getOverlayScreenId());
    Rectangle2D bounds = screen.getVisualBounds();
    imageView.setPreserveRatio(false);
    imageView.setFitWidth(bounds.getHeight());
    imageView.setFitHeight(bounds.getWidth());
  }
}