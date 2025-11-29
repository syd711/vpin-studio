package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.system.FeaturesInfo;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.commons.fx.ServerFX.client;

public class PauseMenuItemsFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static List<PauseMenuItem> createPauseMenuItems(@NonNull GameRepresentation game, @NonNull PauseMenuSettings pauseMenuSettings, @NonNull WOVPSettings wovpSettings, @Nullable VPinScreen cardScreen, @NonNull FrontendMediaRepresentation frontendMedia) {

    // get application features
    FeaturesInfo Features = ServerFX.client.getFeatures();

    List<PauseMenuItem> pauseMenuItems = new ArrayList<>();
    PauseMenuItem item = new PauseMenuItem(PauseMenuItemTypes.exit, "Continue", "Continue Game", new Image(PauseMenu.class.getResourceAsStream("continue.png")));
    pauseMenuItems.add(item);

    if (wovpSettings.isEnabled() && wovpSettings.isApiKeySet() && wovpSettings.isUseScoreSubmitter()) {
      PauseMenuItem scoreSubmitterItem = new PauseMenuItem(PauseMenuItemTypes.scoreSubmitter, "World Of Virtual Pinball", "Score Submitter for World Of Virtual Pinball", new Image(PauseMenu.class.getResourceAsStream("wovp-wheel.png")));
      pauseMenuItems.add(scoreSubmitterItem);
    }

    if (pauseMenuSettings.isShowIscoredScores() && Features.ISCORED_ENABLED) {
      List<CompetitionRepresentation> competitions = client.getIScoredSubscriptions();
      for (CompetitionRepresentation competition : competitions) {
        if (competition.isActive() && competition.getGameId() == game.getId()) {
          PauseMenuItem iScoredItem = new PauseMenuItem(PauseMenuItemTypes.iScored, "iScored", "iScored Game Room Scores", new Image(PauseMenu.class.getResourceAsStream("iscored.png")));
          iScoredItem.setCompetition(competition);
          pauseMenuItems.add(iScoredItem);
        }
      }
    }

    if (pauseMenuSettings.isShowManiaScores() && Features.MANIA_ENABLED) {
      VpsTableVersion tableVersion = client.getVpsTableVersion(game.getExtTableId(), game.getExtTableVersionId());
      if (tableVersion != null) {
        PauseMenuItem maniaItem = new PauseMenuItem(PauseMenuItemTypes.maniaScores, "VPin Mania", "VPin Mania Scores", new Image(PauseMenu.class.getResourceAsStream("mania-wheel.png")));
        pauseMenuItems.add(maniaItem);

        CompetitionRepresentation competition = new CompetitionRepresentation();
        competition.setType(CompetitionType.MANIA.name());
        competition.setName("VPin Mania Scores");
        competition.setGameId(game.getId());
        maniaItem.setCompetition(competition);
      }
    }

    if (!game.isCardDisabled()) {
      item = new PauseMenuItem(PauseMenuItemTypes.highscores, "Highscores", "Highscore Card", new Image(PauseMenu.class.getResourceAsStream("highscores.png")));
      pauseMenuItems.add(item);
    }

    //only load the media of those screens that are not the highscore card screen or if no highscore card screen is set
    if (cardScreen == null || !cardScreen.equals(VPinScreen.GameInfo)) {
      loadMedia(game, pauseMenuItems, PauseMenuItemTypes.info, VPinScreen.GameInfo, frontendMedia, "Instructions", "Info Card", "infocard.png", "tutorial.png");
    }
    if (cardScreen == null || !cardScreen.equals(VPinScreen.Other2)) {
      loadMedia(game, pauseMenuItems, PauseMenuItemTypes.info, VPinScreen.Other2, frontendMedia, "Instructions", "Info", "infocard.png", "tutorial.png");
    }
    if (cardScreen == null || !cardScreen.equals(VPinScreen.GameHelp)) {
      loadMedia(game, pauseMenuItems, PauseMenuItemTypes.help, VPinScreen.GameHelp, frontendMedia, "Rules", "Table Rules", "rules.png", "rules.png");
    }

    if (pauseMenuSettings.isShowTutorials()) {
      createTutorialEntries(game, pauseMenuSettings, pauseMenuItems);
    }

    return pauseMenuItems;
  }

  private static void createTutorialEntries(GameRepresentation game, PauseMenuSettings pauseMenuSettings, List<PauseMenuItem> pauseMenuItems) {
    PauseMenuItem item;
    List<VpsTutorialUrls> videoTutorials = getVideoTutorials(game, pauseMenuSettings);
    for (VpsTutorialUrls videoTutorial : videoTutorials) {
      item = new PauseMenuItem(PauseMenuItemTypes.help, "Tutorial", "Tutorial: " + videoTutorial.getTitle(), new Image(PauseMenu.class.getResourceAsStream("tutorial.png")));
      String videoUrl = "https://assets.vpin-mania.net/tutorials/kongedam/" + game.getExtTableId() + ".mp4";
      item.setVideoUrl(videoUrl);
      LOG.info("\"" + game.getGameDisplayName() + "\": found tutorial video " + videoUrl);
      String url = "https://img.youtube.com/vi/" + videoTutorial.getYoutubeId() + "/0.jpg";
      item.setDataImageUrl(url);
      pauseMenuItems.add(item);
    }
  }

  public static List<VpsTutorialUrls> getVideoTutorials(@NonNull GameRepresentation game, @NonNull PauseMenuSettings pauseMenuSettings) {
    List<VpsTutorialUrls> tutorials = new ArrayList<>();
    String extTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(extTableId)) {
      VpsTable tableById = ServerFX.client.getVpsTable(extTableId);
      if (tableById != null) {
        List<VpsTutorialUrls> tutorialFiles = tableById.getTutorialFiles();
        if (tutorialFiles != null && !tutorialFiles.isEmpty()) {
          for (VpsTutorialUrls tutorialFile : tutorialFiles) {
            if (!StringUtils.isEmpty(tutorialFile.getYoutubeId()) && tutorialFile.getAuthors().contains("Kongedam")) {
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

  private static void loadMedia(GameRepresentation game, List<PauseMenuItem> pauseMenuItems, PauseMenuItemTypes pauseType, VPinScreen screen, FrontendMediaRepresentation frontendMedia, String title, String text, String pictureImage, String videoImage) {
    List<FrontendMediaItemRepresentation> mediaItems = frontendMedia.getMediaItems(screen);
    for (FrontendMediaItemRepresentation mediaItem : mediaItems) {
      String mimeType = mediaItem.getMimeType();
      String baseType = mimeType.split("/")[0];
      if (baseType.equals("image")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream(pictureImage)));
        String url = ServerFX.client.getURL(mediaItem.getUri() + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset()));
        item.setDataImageUrl(url);
        pauseMenuItems.add(item);
      }
      else if (baseType.equals("video")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream(videoImage)));
        String url = ServerFX.client.getURL(mediaItem.getUri() + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset()));
        item.setVideoUrl(url);
        pauseMenuItems.add(item);
      }
    }
  }
}
