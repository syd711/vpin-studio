package de.mephisto.vpin.commons.fx.pausemenu.states;

import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.UIDefaults;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.popper.PinUPControl;
import de.mephisto.vpin.restclient.popper.PinUPControls;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StateMananger implements NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(StateMananger.class);

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;
  private final MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  private List<Integer> LEFT = new ArrayList<>();
  private List<Integer> RIGHT = new ArrayList<>();
  private List<Integer> ENTER = new ArrayList<>();
  private List<Integer> BACK = new ArrayList<>();

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
    if (LEFT.contains(keyCode)) {
      if (menuController.isAtStart()) {
        return;
      }

      this.activeState = activeState.left();
      navPlayer.play();
    } else if (RIGHT.contains(keyCode)) {
      if (menuController.isAtEnd()) {
        return;
      }

      this.activeState = activeState.right();
      navPlayer.play();
    } else if (ENTER.contains(keyCode)) {
      enterPlayer.play();
      this.activeState = activeState.enter();
      LOG.info("Entered " + this.activeState);
    } else if (BACK.contains(keyCode)) {
      PauseMenu.exitPauseMenu();
    }
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
    if (!running) {
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
    LEFT.addAll(Arrays.asList(pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_PRIOR), KeyEvent.VK_LEFT, KeyEvent.VK_KP_LEFT));
    RIGHT.addAll(Arrays.asList(pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_NEXT), KeyEvent.VK_RIGHT, KeyEvent.VK_KP_RIGHT));
    ENTER.addAll(Arrays.asList(pinUPControls.getKeyCode(PinUPControl.FUNCTION_GAME_START), KeyEvent.VK_1, KeyEvent.VK_ENTER));
    BACK.addAll(Arrays.asList(pinUPControls.getKeyCode(PinUPControl.FUNCTION_EXIT), KeyEvent.VK_ESCAPE));
  }

  public void setGame(GameRepresentation game, GameStatus status, PopperScreen cardScreen, PinUPPlayerDisplay tutorialDisplay, PauseMenuSettings pauseMenuSettings) {
    GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
    menuController.setGame(game, status, cardScreen, tutorialDisplay, pauseMenuSettings);
    running = true;
  }

  public void exit() {
    running = false;
    Platform.runLater(() -> {
      menuController.reset();
    });
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
  }
}
