package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItemTypes;
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

    item = new PauseMenuItem(PauseMenuItemTypes.info, "Instructions", "Info Card", new Image(PauseMenu.class.getResourceAsStream("infocard.png")));
    imageStream = PauseMenu.client.getGameMediaItem(game.getId(), PopperScreen.GameInfo);
    if (imageStream != null) {
      Image scoreImage = new Image(imageStream);
      item.setDataImage(scoreImage);
      pauseMenuItems.add(item);
    }

    item = new PauseMenuItem(PauseMenuItemTypes.rules, "Rules", "Table Rules", new Image(PauseMenu.class.getResourceAsStream("rules.png")));
    imageStream = PauseMenu.client.getGameMediaItem(game.getId(), PopperScreen.GameHelp);
    if (imageStream != null) {
      Image scoreImage = new Image(imageStream);
      item.setDataImage(scoreImage);
      pauseMenuItems.add(item);
    }

    item = new PauseMenuItem(PauseMenuItemTypes.help, "Help", "Additional Info or Help", new Image(PauseMenu.class.getResourceAsStream("help.png")));
    item.setYouTubeUrl("https://www.youtube.com/embed/CZNNfFcwsLQ?si=y_U4h8CQSWb1bgvE");
    pauseMenuItems.add(item);

    return pauseMenuItems;
  }
}
