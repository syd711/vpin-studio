package de.mephisto.vpin.tablemanager.states;

import de.mephisto.vpin.tablemanager.Menu;
import de.mephisto.vpin.tablemanager.MenuController;
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
  private final MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();
  private boolean blocked;

  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
    String s = Menu.class.getResource("select.mp3").toExternalForm();
    System.out.println(s);
    Media media = new Media(Menu.class.getResource("select.mp3").toExternalForm());
    navPlayer = new MediaPlayer(media);
    navPlayer.setOnEndOfMedia(() -> {
      navPlayer.stop();
      navPlayer.seek(Duration.ZERO);
    });

    media = new Media(Menu.class.getResource("enter.mp3").toExternalForm());
    enterPlayer = new MediaPlayer(media);
    enterPlayer.setOnEndOfMedia(() -> {
      enterPlayer.stop();
      enterPlayer.seek(Duration.ZERO);
    });

    media = new Media(Menu.class.getResource("back.mp3").toExternalForm());
    backPlayer = new MediaPlayer(media);
    backPlayer.setOnEndOfMedia(() -> {
      backPlayer.stop();
      backPlayer.seek(Duration.ZERO);
    });
  }

  public void setInputBlocked(boolean blocked) {
    if(blocked) {
      this.blocked = true;
    }
    else {
      new Thread(() -> {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          //ignore
        }
        this.blocked = false;
      }).start();
    }
  }

  public void init(MenuController controller) {
    this.activeState = new MainMenuState(controller);
  }

  public void handle(KeyEvent event) {
    if (blocked) {
      return;
    }

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
        backPlayer.play();
        this.activeState = activeState.back();
        LOG.info("Went back to " + this.activeState);
        break;
      }
    }
  }
}
