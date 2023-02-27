package de.mephisto.vpin.poppermenu;

import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class MenuKeyListener {

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;

  private final MenuController controller;

  public MenuKeyListener(MenuController controller) {
    this.controller = controller;
    Media media = new Media(getClass().getClassLoader().getResource("select.mp3").toExternalForm());
    navPlayer = new MediaPlayer(media);
    navPlayer.setOnEndOfMedia(() -> {
      navPlayer.stop();
      navPlayer.seek(Duration.ZERO);
    });

    media = new Media(getClass().getClassLoader().getResource("enter.mp3").toExternalForm());
    enterPlayer = new MediaPlayer(media);
    enterPlayer.setOnEndOfMedia(() -> {
      enterPlayer.stop();
      enterPlayer.seek(Duration.ZERO);
    });
  }

  public void handle(KeyEvent  event) {
    switch (event.getCode()) {
      case LEFT: {
        navPlayer.play();
        controller.toggleInstall();
        break;
      }
      case RIGHT: {
        navPlayer.play();
        controller.toggleInstall();
        break;
      }
      case ENTER: {
        enterPlayer.play();
        break;
      }
    }
  }
}
