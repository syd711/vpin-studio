package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuStyle;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PauseMenuItemsFactory {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuItemsFactory.class);

  public static List<PauseMenuItem> createPauseMenuItems(@NonNull GameRepresentation game, @NonNull PauseMenuSettings pauseMenuSettings, @Nullable VPinScreen cardScreen) {
    List<PauseMenuItem> pauseMenuItems = new ArrayList<>();
    PauseMenuItem item = new PauseMenuItem(PauseMenuItemTypes.exit, "Continue", "Continue Game", new Image(PauseMenu.class.getResourceAsStream("continue.png")));
    pauseMenuItems.add(item);

    if (pauseMenuSettings.getStyle() == null || pauseMenuSettings.getStyle().equals(PauseMenuStyle.embedded)) {
      item = new PauseMenuItem(PauseMenuItemTypes.highscores, "Highscores", "Highscore Card", new Image(PauseMenu.class.getResourceAsStream("highscores.png")));
      InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), cardScreen);
      if (imageStream != null) {
        Image scoreImage = new Image(imageStream);
        item.setDataImage(scoreImage);
        pauseMenuItems.add(item);
      }

      if (cardScreen == null || !cardScreen.equals(VPinScreen.GameInfo)) {
        loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.info, VPinScreen.GameInfo, "Instructions", "Info Card", "infocard.png", "infovideo.png");
      }
      if (cardScreen == null || !cardScreen.equals(VPinScreen.Other2)) {
        loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.info, VPinScreen.Other2, "Instructions", "Info", "infocard.png", "infovideo.png");
      }
      if (cardScreen == null || !cardScreen.equals(VPinScreen.GameHelp)) {
        loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.help, VPinScreen.GameHelp, "Rules", "Table Rules", "rules.png", "rules.png");
      }
    }

    if (pauseMenuSettings.getStyle() == null || pauseMenuSettings.getStyle().equals(PauseMenuStyle.embedded)) {
      createTutorialEntries(game, pauseMenuSettings, pauseMenuItems);
    }
    return pauseMenuItems;
  }

  private static void createTutorialEntries(GameRepresentation game, PauseMenuSettings pauseMenuSettings, List<PauseMenuItem> pauseMenuItems) {
    PauseMenuItem item;
    List<VpsTutorialUrls> videoTutorials = getVideoTutorials(game, pauseMenuSettings);
    for (VpsTutorialUrls videoTutorial : videoTutorials) {
      item = new PauseMenuItem(PauseMenuItemTypes.help, "Help", "YouTube: " + videoTutorial.getTitle(), new Image(PauseMenu.class.getResourceAsStream("video.png")));
      String ytUrl = createYouTubeUrl(videoTutorial);
      item.setYouTubeUrl(ytUrl);
      LOG.info("\"" + game.getGameDisplayName() + "\": found tutorial video " + ytUrl);
      String url = "https://img.youtube.com/vi/" + videoTutorial.getYoutubeId() + "/0.jpg";
      Image scoreImage = new Image(PauseMenu.client.getCachedUrlImage(url));
      item.setDataImage(scoreImage);
      pauseMenuItems.add(item);
    }
  }

  public static String createYouTubeUrl(VpsTutorialUrls tutorialUrl) {
    return "https://www.youtube.com/embed/" + tutorialUrl.getYoutubeId() + "?autoplay=1&controls=1";
  }

  public static List<VpsTutorialUrls> getVideoTutorials(@NonNull GameRepresentation game, @NonNull PauseMenuSettings pauseMenuSettings) {
    List<VpsTutorialUrls> tutorials = new ArrayList<>();
    String extTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(extTableId)) {
      VpsTable tableById = PauseMenu.client.getVpsService().getTableById(extTableId);
      if (tableById != null) {
        List<VpsTutorialUrls> tutorialFiles = tableById.getTutorialFiles();
        if (tutorialFiles != null && !tutorialFiles.isEmpty()) {
          String authorAllowList = pauseMenuSettings.getAuthorAllowList() == null ? "" : pauseMenuSettings.getAuthorAllowList();
          List<String> authorNames = Collections.emptyList();
          if (!StringUtils.isEmpty(authorAllowList)) {
            authorNames = Arrays.asList(authorAllowList.toLowerCase().split(","));
          }

          for (VpsTutorialUrls tutorialFile : tutorialFiles) {
            boolean excludeTutorial = excludeTutorial(authorNames, tutorialFile);
            if (!excludeTutorial && !StringUtils.isEmpty(tutorialFile.getYoutubeId())) {
              tutorials.add(tutorialFile);
            }
          }
        }
      }
      else {
        LOG.warn("The table \"" + game.getGameDisplayName() + "\" is not mapped against VPS, no additional tutorials links will be loaded.");
      }
    }
    return tutorials;
  }

  private static boolean excludeTutorial(List<String> authorAllowList, VpsTutorialUrls tutorialFile) {
    if (authorAllowList.isEmpty()) {
      return false;
    }

    List<String> authors = tutorialFile.getAuthors();
    if (!authorAllowList.isEmpty()) {
      for (String author : authors) {
        if (authorAllowList.contains(author.toLowerCase())) {
          return false;
        }
      }
    }

    return true;
  }

  private static void loadPopperMedia(GameRepresentation game, List<PauseMenuItem> pauseMenuItems, PauseMenuItemTypes pauseType, VPinScreen screen, String title, String text, String pictureImage, String videoImage) {
    List<GameMediaItemRepresentation> mediaItems = game.getGameMedia().getMediaItems(screen);
    for (GameMediaItemRepresentation mediaItem : mediaItems) {
      String mimeType = mediaItem.getMimeType();
      String baseType = mimeType.split("/")[0];
      if (baseType.equals("image")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream(pictureImage)));
        String url = PauseMenu.client.getURL(mediaItem.getUri() + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset()));
        Image scoreImage = new Image(PauseMenu.client.getCachedUrlImage(url));
        item.setDataImage(scoreImage);
        pauseMenuItems.add(item);
      }
      else if (baseType.equals("video")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream(videoImage)));
        String url = PauseMenu.client.getURL(mediaItem.getUri() + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset()));
        item.setVideoUrl(url);
        pauseMenuItems.add(item);
      }
    }
  }
}
