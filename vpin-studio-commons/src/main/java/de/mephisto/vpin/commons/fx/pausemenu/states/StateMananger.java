package de.mephisto.vpin.commons.fx.pausemenu.states;

import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.UIDefaults;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StateMananger implements NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(StateMananger.class);

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;
  private final MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  private int LEFT;
  private int RIGHT;
  private int ENTER;
  private int BACK;

  private MenuController menuController;
  private boolean running = false;

  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
    Media media = new Media(PauseMenu.class.getResource("select.mp3").toExternalForm());
    navPlayer = new MediaPlayer(media);
    navPlayer.setOnEndOfMedia(() -> {
      navPlayer.stop();
      navPlayer.seek(Duration.ZERO);
    });

    media = new Media(PauseMenu.class.getResource("enter.mp3").toExternalForm());
    enterPlayer = new MediaPlayer(media);
    enterPlayer.setOnEndOfMedia(() -> {
      enterPlayer.stop();
      enterPlayer.seek(Duration.ZERO);
    });

    media = new Media(PauseMenu.class.getResource("back.mp3").toExternalForm());
    backPlayer = new MediaPlayer(media);
    backPlayer.setOnEndOfMedia(() -> {
      backPlayer.stop();
      backPlayer.seek(Duration.ZERO);
    });
  }

  public void init(MenuController controller) {
    this.menuController = controller;
    this.activeState = new MenuItemSelectionState(controller);
  }

  public void handle(int keyCode) {
    if (keyCode == LEFT) {
      if (menuController.isAtStart()) {
        return;
      }

      this.activeState = activeState.left();
      navPlayer.play();
    }
    else if (keyCode == RIGHT) {
      if (menuController.isAtEnd()) {
        return;
      }

      this.activeState = activeState.right();
      navPlayer.play();
    }
    else if (keyCode == ENTER) {
      enterPlayer.play();
      this.activeState = activeState.enter();
      LOG.info("Entered " + this.activeState);
    }
    else if (keyCode == BACK) {
      PauseMenu.exit();
    }
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
    if(!running) {
      return;
    }

    Platform.runLater(() -> {
      handle(nativeKeyEvent.getRawCode());
      new Thread(() -> {
        try {
          Thread.sleep(UIDefaults.SELECTION_SCALE_DURATION);
          GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
    });

  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
  }

  public void setControls(PinUPControls pinUPControls) {
    LEFT = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_PRIOR);
    RIGHT = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_NEXT);
    ENTER = pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_START);
    BACK = pinUPControls.getKeyCode(PinUPControl.FUNCTION_EXIT);
  }

  public void setGame(GameRepresentation game, GameStatus status, PopperScreen screen) {
    GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
    menuController.setGame(game, status, screen);
    running = true;
  }

  public void exit() {
    running = false;
    menuController.reset();
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
  }
}
