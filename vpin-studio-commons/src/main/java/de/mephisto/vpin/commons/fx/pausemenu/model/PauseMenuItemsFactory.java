package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PauseMenuItemsFactory {
  public static List<PauseMenuItem> createPauseMenuItems(GameRepresentation game, PopperScreen cardScreen) {
    List<PauseMenuItem> pauseMenuItems = new ArrayList<>();
    PauseMenuItem item = new PauseMenuItem(PauseMenuItemTypes.exit, "Exit", "Continue Game", new Image(PauseMenu.class.getResourceAsStream("exit.png")));
    pauseMenuItems.add(item);

    item = new PauseMenuItem(PauseMenuItemTypes.highscores, "Highscores", "Highscore Card", new Image(PauseMenu.class.getResourceAsStream("highscores.png")));
    InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), cardScreen);
    if (imageStream != null) {
      Image scoreImage = new Image(imageStream);
      item.setDataImage(scoreImage);
      pauseMenuItems.add(item);
    }

    loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.info, PopperScreen.GameInfo, "Instructions", "Info Card");
    loadPopperMedia(game, pauseMenuItems, PauseMenuItemTypes.help, PopperScreen.GameHelp, "Rules", "Table Rules");


    item = new PauseMenuItem(PauseMenuItemTypes.help, "Help", "Additional Info or Help", new Image(PauseMenu.class.getResourceAsStream("help.png")));
    item.setYouTubeUrl("https://www.youtube.com/embed/EHkNdf4JaEI?si=OjpWfYpwyXDLsa7Z");
    pauseMenuItems.add(item);

    return pauseMenuItems;
  }

  private static void loadPopperMedia(GameRepresentation game, List<PauseMenuItem> pauseMenuItems, PauseMenuItemTypes pauseType, PopperScreen screen, String title, String text) {
    List<GameMediaItemRepresentation> mediaItems = game.getGameMedia().getMediaItems(screen);
    for (GameMediaItemRepresentation mediaItem : mediaItems) {

      String mimeType = mediaItem.getMimeType();
      String baseType = mimeType.split("/")[0];
      if (baseType.equals("image")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream("infocard.png")));
        String url = PauseMenu.client.getURL(mediaItem.getUri());
        InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), screen);
        Image scoreImage = new Image(imageStream);
        item.setDataImage(scoreImage);
        pauseMenuItems.add(item);
      }
      else if (baseType.equals("video")) {
        PauseMenuItem item = new PauseMenuItem(pauseType, title, text, new Image(PauseMenu.class.getResourceAsStream("infocard.png")));
        String url = PauseMenu.client.getURL(mediaItem.getUri());
        item.setVideoUrl(url);
        pauseMenuItems.add(item);
      }
    }
  }
}
