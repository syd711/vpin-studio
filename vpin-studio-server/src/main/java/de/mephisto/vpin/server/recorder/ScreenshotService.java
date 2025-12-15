package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.zip.ZipOutputStream;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;
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

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private ScreenPreviewService screenPreviewService;

  private String lastScreenShotId = null;

  public InputStream takeScreenshot() {
    try {
      BufferedImage bufferedImage = takeMonitorsScreenshots();
      byte[] bytes = toBytes(bufferedImage);
      return new ByteArrayInputStream(bytes);
    }
    catch (Exception e) {
      LOG.error("Failed to read screenshot image: {}", e.getMessage(), e);
    }
    return new ByteArrayInputStream(new byte[]{});
  }

  public String screenshot() {
    lastScreenShotId = UUID.randomUUID().toString();

    BufferedImage bufferedImage = takeMonitorsScreenshots();
    //return as fast as possible to speed up pause menu show
    new Thread(() -> {
      try {
        Thread.currentThread().setName("Screenshot Writer " + lastScreenShotId);
        writeScore(bufferedImage);
        byte[] bytes = toBytes(bufferedImage);
        File screenshot = getScreenshotFile(lastScreenShotId);
        FileOutputStream out = new FileOutputStream(screenshot);
        IOUtils.write(bytes, out);
        out.close();
      }
      catch (IOException e) {
        LOG.error("Failed to write screenshot: {}", e.getMessage(), e);
      }
    }).start();
    return lastScreenShotId;
  }

  private void writeScore(BufferedImage bufferedImage) {
    Player adminPlayer = playerService.getAdminPlayer();
    if (adminPlayer != null) {
      if (gameStatusService.getStatus() != null) {
        GameStatus status = gameStatusService.getStatus();
        Game game = gameService.getGame(status.getGameId());
        if (game != null) {
          highscoreService.scanScore(game, EventOrigin.COMPETITION_UPDATE);
          ScoreSummary scoreSummary = highscoreService.getScoreSummary(-1, game);
          Optional<Score> score = scoreSummary.getScores().stream().filter(s -> s.getPlayer() != null && s.getPlayer().equals(adminPlayer)).findFirst();
          if (score.isPresent()) {
            Graphics g = bufferedImage.getGraphics();
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Digital Counter 7", Font.BOLD, 48));
            g2d.drawString(score.get().getFormattedScore(), 12, 60);
          }
        }
      }
    }
  }

  public File getScreenshotFile(@Nullable String uuid) {
    File screenshotFolder = new File(RESOURCES, "screenshots");
    if (!screenshotFolder.exists()) {
      screenshotFolder.mkdirs();
    }
    String id = uuid != null ? uuid : lastScreenShotId;
    File screenshot = new File(screenshotFolder, id + ".jpg");
    screenshot.deleteOnExit();
    return screenshot;
  }


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

  private List<File> takeScreenshots() {
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

          BufferedImage bufferedImage = loadImage(file);
          images.add(bufferedImage);
          drawTimestamp(bufferedImage);
          file.renameTo(target);

          screenshotFiles.add(target);
          LOG.info("Written screenshot " + target.getAbsolutePath());
        }
      }
      catch (Exception e) {
        LOG.error("Error writing screenshot: {}", e.getMessage(), e);
      }
    }

    writeSummaryScreenshot(images, screenshotFiles);
    return screenshotFiles;
  }

  private BufferedImage takeMonitorsScreenshots() {
    long start = System.currentTimeMillis();
    PauseMenuSettings pauseMenuSettings = preferencesService.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    List<BufferedImage> images = new ArrayList<>();
    List<MonitorInfo> monitorInfos = new ArrayList<>();
    if (pauseMenuSettings.isDesktopUser()) {
      monitorInfos.add(MonitorInfoUtil.getPrimaryMonitor());
    }
    else {
      monitorInfos.addAll(systemService.getMonitorInfos());
    }

    Collections.sort(monitorInfos, new Comparator<MonitorInfo>() {
      @Override
      public int compare(MonitorInfo o1, MonitorInfo o2) {
        if (o1.isPrimary()) {
          return -1;
        }
        return 1;
      }
    });

    for (MonitorInfo monitorInfo : monitorInfos) {
      try {
        BufferedImage bufferedImage = screenPreviewService.capture(monitorInfo);
        if (monitorInfo.isPrimary() && !pauseMenuSettings.isDesktopUser()) {
          bufferedImage = ImageUtil.rotateRight(bufferedImage);
        }
        images.add(bufferedImage);
        drawTimestamp(bufferedImage);
      }
      catch (Exception e) {
        LOG.error("Error writing monitor screenshot: {}", e.getMessage(), e);
      }
    }

    BufferedImage summaryImage = null;
    if (pauseMenuSettings.isDesktopUser()) {
      summaryImage = images.get(0);
      summaryImage = ImageUtil.resizeImage(summaryImage, 1920, 1080);
    }
    else {
      summaryImage = generateSummaryImage(images);
      int width = summaryImage.getWidth() / 2;
      int height = summaryImage.getHeight() / 2;
      summaryImage = ImageUtil.resizeImage(summaryImage, width, height);
    }

    LOG.info("Screenshot generation took {}ms.", (System.currentTimeMillis() - start));
    return summaryImage;
  }

  private BufferedImage generateSummaryImage(List<BufferedImage> images) {
    try {
      int totalHeight = 0;
      int totalWidth = images.get(0).getWidth();
      int additionalWidth = totalWidth;

      int index = 0;
      for (BufferedImage image : images) {
        if (image.getWidth() > additionalWidth) {
          additionalWidth = image.getWidth();
        }

        if (index > 0) {
          totalHeight = totalHeight + image.getHeight();
        }
        index++;
      }

      totalWidth = totalWidth + additionalWidth;

      if (images.get(0).getHeight() > totalHeight) {
        totalHeight = images.get(0).getHeight();
      }


      BufferedImage summaryImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
      int x = 0;
      int y = 0;
      Graphics g = summaryImage.getGraphics();
      index = 0;
      for (BufferedImage image : images) {
        if (index == 0) {
          g.drawImage(image, x, 0, null);
        }
        else {
          g.drawImage(image, images.get(0).getWidth(), y, null);
          y = y + image.getHeight();
        }
        index++;
      }
      g.dispose();

      return summaryImage;
    }
    catch (Exception e) {
      LOG.error("Failed generating summary image: {}", e.getMessage(), e);
    }
    return null;
  }

  private void writeSummaryScreenshot(List<BufferedImage> images, List<File> screenshotFiles) {
    try {
      if (!images.isEmpty()) {
        BufferedImage summaryImage = generateSummaryImage(images);
        File file = File.createTempFile("screenshot", ".jpg");
        write(summaryImage, file);

        String name = "screenshot-summary.jpg";
        File target = new File(file.getParentFile(), name);
        if (target.exists() && !target.delete()) {
          throw new Exception("Failed to delete temporary screenshot file " + target.getAbsolutePath());
        }

        file.renameTo(target);
        LOG.info("Written screenshot summary {} ({})", target.getAbsolutePath(), FileUtils.readableFileSize(target.length()));
        target.deleteOnExit();
        screenshotFiles.add(target);
      }
    }
    catch (Exception e) {
      LOG.error("Failed writing summary timestamp: {}", e.getMessage(), e);
    }
  }
}
