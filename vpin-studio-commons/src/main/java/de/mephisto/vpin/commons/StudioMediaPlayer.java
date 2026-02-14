package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * vlc.exe "video.mp4" ^
 * --video-filter=rotate ^
 * --rotate-angle=90 ^
 * --video-x=100 ^
 * --video-y=100 ^
 * --width=800 ^
 * --height=600 ^
 * --no-video-title-show ^
 * --qt-minimal-view
 */
public class StudioMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MediaPlayer jfxMediaPlayer;
  private SystemCommandExecutor executor;
  private Process vlcProcess;

  public Node render(FrontendPlayerDisplay display, @NonNull FrontendScreenAsset asset) {
    if (!renderVlcMediaPlayer(display, asset)) {
      return renderJfxMediaPlayer(asset.getUrl().toExternalForm());
    }
    return null;
  }

  private boolean renderVlcMediaPlayer(@NonNull FrontendPlayerDisplay display, @NonNull FrontendScreenAsset asset) {
    try {
      File propertiesFile = new File(SystemInfo.RESOURCES + "system.properties");
      PropertiesStore store = PropertiesStore.create(propertiesFile);
      File folder = null;
      if (store.containsKey("pinupSystem.installationDir")) {
        folder = new File(store.get("pinupSystem.installationDir"), "VLC/");
      }
      else if (store.containsKey("vlc.installationDir")) {
        folder = new File(store.get("vlc.installationDir"));
      }

      if (folder != null && folder.exists()) {
        File vlcExe = new File(folder, "vlc.exe");
        if (vlcExe.exists()) {
          double screenStageX = display.getX() + asset.getOffsetX();
          double screenStageY = display.getY() + asset.getOffsetY();
          double width = display.getWidth();
          double height = display.getHeight();

          double rotation = asset.getRotation();

          if (asset.getRotation() == 90 || asset.getRotation() == 270) {
            screenStageX = screenStageX + ((double) display.getWidth() / 2) - ((double) display.getHeight() / 2);
            screenStageY = screenStageY - ((double) display.getHeight() / 2) + ((double) display.getHeight() / 2);
            width = display.getHeight();
            height = display.getWidth();
          }
          if (asset.getRotation() == 180) {
            screenStageY = screenStageY + display.getHeight();
          }

          List<String> cmds = new ArrayList<>(List.of("vlc.exe",
              "\"" + asset.getUrl().toExternalForm() + "\"",
              "--mouse-hide-timeout=0",
              "--dummy-quiet",
              "--intf", "rc",
              "--rc-quiet",
              "--no-qt-privacy-ask",
              "--reset-plugins-cache",
              "--ignore-config",
              "--no-stats",
              "--no-media-library",
              "--no-video-deco",
              "--no-video-title-show",
              "--qt-minimal-view",
              "--no-qt-name-in-title",
              "--no-qt-fs-controller",
              "--video-x=" + screenStageX,
              "--video-y=" + screenStageY,
              "--width=" + width,
              "--height=" + height
          ));

          if (rotation != 0) {
            cmds.add("--video-filter=transform");
            cmds.add("--transform-type=" + rotation);
          }
          else {
            cmds.add("--aspect-ratio=");
//            cmds.add("--no-video-orientation");
          }

//          executor = new SystemCommandExecutor(cmds);
//          executor.executeCommandAsync();
          ProcessBuilder pb = new ProcessBuilder(cmds);
          vlcProcess = pb.start();
          LOG.info("Launch VLC player {}.", vlcExe.getAbsolutePath());
          return true;
        }
      }
    }
    catch (Exception e) {
      //ignore
    }
    return false;
  }

  private Node renderJfxMediaPlayer(String mediaUrl) {
    MediaView mediaView = new MediaView();
    renderJfxMediaPlayerWithAttempts(mediaView, mediaUrl, 0);
    return mediaView;
  }

  private void renderJfxMediaPlayerWithAttempts(MediaView mediaView, String mediaUrl, int attempt) {
    if (attempt == 100) {
      return;
    }
    int attemptCount = attempt + 1;
    Media media = new Media(mediaUrl);
    jfxMediaPlayer = new MediaPlayer(media);
    jfxMediaPlayer.setCycleCount(-1);
    jfxMediaPlayer.setMute(false);
    jfxMediaPlayer.setOnError(() -> {
      LOG.warn("Pause Menu Media player error:{}/{}, URL: {}", jfxMediaPlayer.getError(), attemptCount, jfxMediaPlayer.getMedia().getSource());
      jfxMediaPlayer.stop();
      jfxMediaPlayer.dispose();
      mediaView.setMediaPlayer(null);
      renderJfxMediaPlayerWithAttempts(mediaView, mediaUrl, attemptCount);
    });
    mediaView.setMediaPlayer(jfxMediaPlayer);

    Platform.runLater(() -> {
      jfxMediaPlayer.play();
    });
  }

  public void dispose() {
    if (vlcProcess != null) {
      vlcProcess.destroy();
      vlcProcess.destroyForcibly();
    }

    if (executor != null) {
      executor.killProcess();
    }

    if (jfxMediaPlayer != null) {
      jfxMediaPlayer.stop();
      jfxMediaPlayer.dispose();
    }
  }

  public static void main(String[] args) {

  }
}
