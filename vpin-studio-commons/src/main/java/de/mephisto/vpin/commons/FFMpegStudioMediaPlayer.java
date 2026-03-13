package de.mephisto.vpin.commons;

import de.mephisto.vpin.commons.fx.pausemenu.model.FrontendScreenAsset;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Media player that uses ffplay.exe (part of the ffmpeg suite) from the resources folder
 * to display videos borderless at a specific screen position and size.
 *
 * Requires ffplay.exe to be present in the resources folder alongside ffmpeg.exe.
 *
 * ffplay options used:
 *   -x <width>        display width
 *   -y <height>       display height
 *   -left <x>         window left position
 *   -top <y>          window top position
 *   -noborder         borderless window (no decorations)
 *   -loop 0           loop video indefinitely
 *   -alwaysontop      keep window on top
 *   -window_title ""  empty window title
 *   -vf transpose=1   rotate 90° clockwise
 *   -vf transpose=2   rotate 90° counter-clockwise (270°)
 *   -vf hflip,vflip   rotate 180°
 */
public class FFMpegStudioMediaPlayer {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MediaPlayer jfxMediaPlayer;
  private Process ffmpegProcess;

  public Node render(FrontendPlayerDisplay display, @NonNull FrontendScreenAsset asset) {
    if (!renderFfplayMediaPlayer(display, asset)) {
      return renderJfxMediaPlayer(asset.getUrl().toExternalForm());
    }
    return null;
  }

  private boolean renderFfplayMediaPlayer(@NonNull FrontendPlayerDisplay display, @NonNull FrontendScreenAsset asset) {
    try {
      File ffplayExe = new File(SystemInfo.RESOURCES, "ffplay.exe");
      if (!ffplayExe.exists()) {
        LOG.warn("ffplay.exe not found at {}, falling back to JFX media player.", ffplayExe.getAbsolutePath());
        return false;
      }

      double screenStageX = display.getX() + asset.getOffsetX();
      double screenStageY = display.getY() + asset.getOffsetY();
      double width = display.getWidth();
      double height = display.getHeight();
      double rotation = asset.getRotation();

      if (rotation == 90 || rotation == 270) {
        screenStageX = screenStageX + ((double) display.getWidth() / 2) - ((double) display.getHeight() / 2);
        screenStageY = screenStageY - ((double) display.getHeight() / 2) + ((double) display.getHeight() / 2);
        width = display.getHeight();
        height = display.getWidth();
      }
      if (rotation == 180) {
        screenStageY = screenStageY + display.getHeight();
      }

      String filePath = Paths.get(asset.getUrl().toURI()).toAbsolutePath().toString();

      List<String> cmds = new ArrayList<>();
      cmds.add(ffplayExe.getAbsolutePath());
      cmds.add("-i");
      cmds.add(filePath);
      cmds.add("-x");
      cmds.add(String.valueOf((int) width));
      cmds.add("-y");
      cmds.add(String.valueOf((int) height));
      cmds.add("-left");
      cmds.add(String.valueOf((int) screenStageX));
      cmds.add("-top");
      cmds.add(String.valueOf((int) screenStageY));
      cmds.add("-noborder");
      cmds.add("-loop");
      cmds.add("0");
      cmds.add("-alwaysontop");
      cmds.add("-window_title");
      cmds.add("");

      if (rotation == 90) {
        cmds.add("-vf");
        cmds.add("transpose=1");
      }
      else if (rotation == 270) {
        cmds.add("-vf");
        cmds.add("transpose=2");
      }
      else if (rotation == 180) {
        cmds.add("-vf");
        cmds.add("hflip,vflip");
      }

      LOG.info("FFplay is playing: {}", filePath);
      LOG.info("FFplay command: {}", String.join(" ", cmds));

      ProcessBuilder pb = new ProcessBuilder(cmds);
      ffmpegProcess = pb.start();
      LOG.info("Launched FFplay player {}.", ffplayExe.getAbsolutePath());
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to launch FFplay player: {}", e.getMessage(), e);
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
    if (ffmpegProcess != null) {
      ffmpegProcess.destroy();
      ffmpegProcess.destroyForcibly();
    }

    if (jfxMediaPlayer != null) {
      jfxMediaPlayer.stop();
      jfxMediaPlayer.dispose();
    }
  }
}
