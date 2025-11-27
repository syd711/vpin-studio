package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static de.mephisto.vpin.commons.fx.ImageUtil.*;

@Service
public class ScreenshotService {
  private final static Logger LOG = LoggerFactory.getLogger(RecorderService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private RecorderService recorderService;

  @Autowired
  private GameService gameService;

  /**
   * The not streamed version
   */
  public void takeScreenshots(int gameId) {
    try {
      Game game = gameId > 0 ? gameService.getGame(gameId) : null;

      File targetFolder = new File(SystemService.RESOURCES, "screenshots/");
      if (!targetFolder.exists() && !targetFolder.mkdirs()) {
        LOG.error("Failed to create screenshot folder: {}", targetFolder.getAbsolutePath());
        return;
      }

      String dateSuffix = DateUtil.formatTimeString(new Date()) + ".zip";
      String name = game != null ? FileUtils.replaceWindowsChars(game.getGameDisplayName()) + dateSuffix : "menu-" + dateSuffix;
      File target = new File(targetFolder, name);
      List<File> files = takeScreenshots();
      zipScreenshots(target, files);
    }
    catch (IOException e) {
      LOG.error("Failed to write internal screenshots: {}", e.getMessage(), e);
    }
  }

  public void takeScreenshots(@NonNull File targetArchive) throws IOException {
    List<File> files = takeScreenshots();
    zipScreenshots(targetArchive, files);
  }

  public void zipScreenshots(@NonNull File targetArchive, @NonNull List<File> screenshotFiles) throws IOException {
    FileOutputStream fos = new FileOutputStream(targetArchive);
    ZipOutputStream zipOut = new ZipOutputStream(fos);

    for (File screenshotFile : screenshotFiles) {
      ZipUtil.zipFile(screenshotFile, screenshotFile.getName(), zipOut);
    }
    zipOut.close();
    fos.close();

    for (File screenshotFile : screenshotFiles) {
      if (!screenshotFile.delete()) {
        LOG.warn("Failed to delete temporary screenshot file " + screenshotFile.getAbsolutePath());
      }
      else {
        LOG.info("Delete temporary screenshot {}", screenshotFile.getAbsolutePath());
      }
    }
  }

  private List<File> takeScreenshots() throws IOException {
    MonitoringSettings monitoringSettings = preferencesService.getJsonPreference(PreferenceNames.MONITORING_SETTINGS, MonitoringSettings.class);
    return takeFrontendScreenshots(monitoringSettings);
  }

  private List<File> takeFrontendScreenshots(MonitoringSettings monitoringSettings) {
    List<File> screenshotFiles = new ArrayList<>();
    List<VPinScreen> disabledScreens = monitoringSettings.getDisabledScreens();
    List<FrontendPlayerDisplay> recordingScreens = recorderService.getRecordingScreens();
    List<BufferedImage> images = new ArrayList<>();
    for (FrontendPlayerDisplay recordingScreen : recordingScreens) {
      try {
        VPinScreen screen = recordingScreen.getScreen();
        if (!disabledScreens.contains(screen)) {
          File file = File.createTempFile("screenshot", ".jpg");
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          recorderService.refreshPreview(out, screen);
          out.close();

          FileOutputStream fileOutputStream = new FileOutputStream(file);
          fileOutputStream.write(out.toByteArray());
          fileOutputStream.close();

          String name = "screenshot-" + screen.name() + ".jpg";
          File target = new File(file.getParentFile(), name);
          if (target.exists() && !target.delete()) {
            throw new Exception("Failed to delete temporary screenshot file " + target.getAbsolutePath());
          }

          images.add(loadImage(file));
          drawTimestamp(file);
          file.renameTo(target);

          screenshotFiles.add(target);
          LOG.info("Written screenshot " + target.getAbsolutePath());
        }
      }
      catch (Exception e) {
        LOG.error("Error writing screenshot: {}", e.getMessage(), e);
      }
    }

    try {
      if (!images.isEmpty()) {
        File summaryImage = writeScreenshotSummary(images);
        if (summaryImage != null) {
          drawTimestamp(summaryImage);
          screenshotFiles.add(summaryImage);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed writing summary timestamp: {}", e.getMessage(), e);
    }

    return screenshotFiles;
  }

  private File writeScreenshotSummary(List<BufferedImage> images) {
    try {
      int width = 0;
      int height = 0;
      for (BufferedImage image : images) {
        width += image.getWidth();
        if (image.getHeight() > height) {
          height = image.getHeight();
        }
      }

      BufferedImage summaryImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      int x = 0;
      Graphics g = summaryImage.getGraphics();
      for (BufferedImage image : images) {
        g.drawImage(image, x, 0, null);
        x += image.getWidth();
      }
      g.dispose();

      File file = File.createTempFile("screenshot", ".jpg");
      write(summaryImage, file);

      String name = "screenshot-summary.jpg";
      File target = new File(file.getParentFile(), name);
      if (target.exists() && !target.delete()) {
        throw new Exception("Failed to delete temporary screenshot file " + target.getAbsolutePath());
      }

      file.renameTo(target);
      LOG.info("Written screenshot summary {} ({})", target.getAbsolutePath(), FileUtils.readableFileSize(target.length()));
      return target;
    }
    catch (Exception e) {
      LOG.error("Failed writing summary image: {}", e.getMessage(), e);
    }
    return null;
  }
}
