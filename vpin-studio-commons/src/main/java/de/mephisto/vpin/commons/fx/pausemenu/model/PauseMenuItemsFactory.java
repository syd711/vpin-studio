package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTutorialUrls;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PauseMenuItemsFactory {
  public static List<PauseMenuItem> createPauseMenuItems(@NonNull GameRepresentation game, @Nullable PopperScreen cardScreen) {
    List<PauseMenuItem> pauseMenuItems = new ArrayList<>();
    PauseMenuItem item = new PauseMenuItem(PauseMenuItemTypes.exit, "Continue", "Continue Game", new Image(PauseMenu.class.getResourceAsStream("continue.png")));
    pauseMenuItems.add(item);

    item = new PauseMenuItem(PauseMenuItemTypes.highscores, "Highscores", "Highscore Card", new Image(PauseMenu.class.getResourceAsStream("highscores.png")));
    InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), cardScreen);
    if (imageStream != null) {
      Image scoreImage = new Image(imageStream);
      item.setDataImage(scoreImage);
      pauseMenuItems.add(item);
    }

    if (cardScreen == null || !cardScreen.equals(PopperScreen.GameInfo)) {
      loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.info, PopperScreen.GameInfo, "Instructions", "Info Card", "infocard.png", "infovideo.png");
    }
    if (cardScreen == null || !cardScreen.equals(PopperScreen.Other2)) {
      loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.info, PopperScreen.Other2, "Instructions", "Info", "infocard.png", "infovideo.png");
    }
    if (cardScreen == null || !cardScreen.equals(PopperScreen.GameHelp)) {
      loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.help, PopperScreen.GameHelp, "Rules", "Table Rules", "rules.png", "rules.png");
    }


    String extTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(extTableId)) {
      VpsTable tableById = VPS.getInstance().getTableById(extTableId);
      if (tableById != null) {
        List<VpsTutorialUrls> tutorialFiles = tableById.getTutorialFiles();
        if (tutorialFiles != null && !tutorialFiles.isEmpty()) {
          for (VpsTutorialUrls tutorialFile : tutorialFiles) {
            if (!StringUtils.isEmpty(tutorialFile.getYoutubeId())) {
              item = new PauseMenuItem(PauseMenuItemTypes.help, "Help", "YouTube: " + tutorialFile.getTitle(), new Image(PauseMenu.class.getResourceAsStream("video.png")));
              item.setYouTubeUrl("https://www.youtube.com/embed/" + tutorialFile.getYoutubeId() + "?autoplay=1&controls=1");
              String url = "https://img.youtube.com/vi/" + tutorialFile.getYoutubeId() + "/0.jpg";
              Image scoreImage = new Image(PauseMenu.client.getCachedUrlImage(url));
              item.setDataImage(scoreImage);
              pauseMenuItems.add(item);
            }
          }
        }
      }
    }


    return pauseMenuItems;
  }

  private static void loadPopperMedia(GameRepresentation game, List<PauseMenuItem> pauseMenuItems, PauseMenuItemTypes pauseType, PopperScreen screen, String title, String text, String pictureImage, String videoImage) {
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
