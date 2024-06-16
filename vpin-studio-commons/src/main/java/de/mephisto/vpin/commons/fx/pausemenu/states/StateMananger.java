package de.mephisto.vpin.commons.fx.pausemenu.states;

import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.UIDefaults;
import de.mephisto.vpin.commons.utils.VPXKeyManager;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.FrontendControls;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
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
import java.util.stream.Collectors;


public class StateMananger implements NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(StateMananger.class);

  private final MediaPlayer navPlayer;
  private final MediaPlayer enterPlayer;
  private final MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  private final List<Integer> LEFT = new ArrayList<>();
  private final List<Integer> RIGHT = new ArrayList<>();
  private final List<Integer> ENTER = new ArrayList<>();

  private int RECORDED_LEFT = 0;
  private int RECORDED_RIGHT = 0;
  private int RECORDED_START = 0;

  private MenuController menuController;
  private boolean running = false;
  private int leftFlip;
  private int rightFlip;
  private int start;


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

  public void handle(int keyCode, int rawCode) {
    LOG.info("Pause Menu Key Event [keyCode/keyCodeRaw]: " + keyCode + "/" + rawCode);
    if (LEFT.contains(rawCode) || isVPXMapped(keyCode, rawCode, leftFlip) || isRecordedMapped(rawCode, RECORDED_LEFT)) {
      this.activeState = activeState.left();
      navPlayer.play();
    }
    else if (RIGHT.contains(rawCode) || isVPXMapped(keyCode, rawCode, rightFlip) || isRecordedMapped(rawCode, RECORDED_RIGHT)) {
      this.activeState = activeState.right();
      navPlayer.play();
    }
    else if (ENTER.contains(rawCode) || isVPXMapped(keyCode, rawCode, start) || isRecordedMapped(rawCode, RECORDED_START)) {
      enterPlayer.play();
      this.activeState = activeState.enter();
      LOG.info("Entered " + this.activeState);
    }
  }

  private boolean isRecordedMapped(int rawCode, int recorded) {
    return rawCode == recorded;
  }

  private boolean isVPXMapped(int keyCode, int rawCode, int vpxDirectXKey) {
    if (keyCode == vpxDirectXKey) {
      return true;
    }

    if (keyCode == 29 || keyCode == 42 || keyCode == 3638) { //Ctrl + shift
      return rawCode == vpxDirectXKey;
    }

    return false;
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
      handle(nativeKeyEvent.getKeyCode(), nativeKeyEvent.getRawCode());
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

  public void setControls(FrontendControls frontendControls, PauseMenuSettings pauseMenuSettings) {
    VPXKeyManager.getInstance().reloadKeyBinding();

    LEFT.clear();
    RIGHT.clear();
    ENTER.clear();

    LEFT.addAll(Arrays.asList(frontendControls.getKeyCode(FrontendControl.FUNCTION_GAME_PRIOR), KeyEvent.VK_LEFT, KeyEvent.VK_KP_LEFT));
    LOG.info("LEFT codes: " + String.join(", ", LEFT.stream().map(String::valueOf).collect(Collectors.toList())));

    RIGHT.addAll(Arrays.asList(frontendControls.getKeyCode(FrontendControl.FUNCTION_GAME_NEXT), KeyEvent.VK_RIGHT, KeyEvent.VK_KP_RIGHT));
    LOG.info("RIGHT codes: " + String.join(", ", RIGHT.stream().map(String::valueOf).collect(Collectors.toList())));

    ENTER.addAll(Arrays.asList(frontendControls.getKeyCode(FrontendControl.FUNCTION_GAME_START), KeyEvent.VK_1, KeyEvent.VK_ENTER));
    LOG.info("LEFT codes: " + String.join(", ", ENTER.stream().map(String::valueOf).collect(Collectors.toList())));

    RECORDED_START = 0;
    RECORDED_LEFT = 0;
    RECORDED_RIGHT = 0;
    if (pauseMenuSettings.getCustomStartKey() > 0) {
      LOG.info("Added custom pause menu key: START [" + pauseMenuSettings.getCustomStartKey() + "]");
      RECORDED_START = pauseMenuSettings.getCustomStartKey();
    }
    if (pauseMenuSettings.getCustomStartKey() > 0) {
      LOG.info("Added custom pause menu key: LEFT [" + pauseMenuSettings.getCustomLeftKey() + "]");
      RECORDED_LEFT = pauseMenuSettings.getCustomLeftKey();
    }
    if (pauseMenuSettings.getCustomStartKey() > 0) {
      LOG.info("Added custom pause menu key: RIGHT [" + pauseMenuSettings.getCustomRightKey() + "]");
      RECORDED_RIGHT = pauseMenuSettings.getCustomRightKey();
    }

    leftFlip = VPXKeyManager.getInstance().getBinding(VPXKeyManager.LFlipKey);
    rightFlip = VPXKeyManager.getInstance().getBinding(VPXKeyManager.RFlipKey);
    start = VPXKeyManager.getInstance().getBinding(VPXKeyManager.StartGameKey);
  }

  public void setGame(GameRepresentation game, GameStatus status, VpsTable table, VPinScreen cardScreen, FrontendPlayerDisplay tutorialDisplay, PauseMenuSettings pauseMenuSettings) {
    GlobalScreen.addNativeKeyListener(StateMananger.getInstance());
    menuController.setGame(game, status, table, cardScreen, tutorialDisplay, pauseMenuSettings);
    running = true;
  }

  public boolean isRunning() {
    return running;
  }

  public void exit() {
    running = false;
    Platform.runLater(() -> {
      menuController.reset();
    });
    GlobalScreen.removeNativeKeyListener(StateMananger.getInstance());
  }
}
