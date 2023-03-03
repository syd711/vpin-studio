package de.mephisto.vpin.poppermenu.states;

import de.mephisto.vpin.poppermenu.MenuController;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMananger {
  private final static Logger LOG = LoggerFactory.getLogger(StateMananger.class);

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
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

  public void init(MenuController controller) {
    this.activeState = new MainMenuState(controller);
  }

  public void handle(KeyEvent event) {
    switch (event.getCode()) {
      case LEFT: {
        navPlayer.play();
        this.activeState = activeState.left();
        break;
      }
      case RIGHT: {
        navPlayer.play();
        this.activeState = activeState.right();
        break;
      }
      case ENTER: {
        enterPlayer.play();
        this.activeState = activeState.enter();
        LOG.info("Entered " + this.activeState);
        break;
      }
      case ESCAPE: {
        enterPlayer.play();
        this.activeState = activeState.back();
        LOG.info("Went back to " + this.activeState);
        break;
      }
    }
  }
}
