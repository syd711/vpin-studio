package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class MaintenanceController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private MediaView mediaView;

  // Add a public no-args constructor
  public MaintenanceController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    MonitorInfo screen = ServerFX.client.getScreenInfo(overlaySettings.getOverlayScreenId());

    String maintenanceUrl = ServerFX.client.getURL("assets/maintenance");
    Media media = new Media(maintenanceUrl);
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaView.setMediaPlayer(mediaPlayer);

    mediaPlayer.setOnReady(() -> {
      mediaPlayer.setAutoPlay(true);
      mediaPlayer.setCycleCount(-1);
      mediaPlayer.setMute(true);

      mediaView.setPreserveRatio(false);
      mediaView.setFitWidth(screen.getHeight());
      mediaView.setFitHeight(screen.getWidth());
      mediaView.setVisible(false);
      mediaView.setVisible(true);
    });
  }
}