package de.mephisto.vpin.commons.fx.pausemenu.states;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuState;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


public class StateMananger implements GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private MediaPlayer navPlayer;
  private MediaPlayer enterPlayer;
  private MediaPlayer backPlayer;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  private MenuController menuController;
  private boolean running = false;
  private PauseMenuSettings pauseMenuSettings;


  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
    try {
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
    catch (Exception e) {
      LOG.error("StateManager init failed: {}", e.getMessage(), e);
    }
  }

  public void init(MenuController controller) {
    this.menuController = controller;
    this.activeState = new MenuItemSelectionState(controller);
  }

  public void handle(String button) {
    if (button == null) {
      return;
    }

    if (button.equals(pauseMenuSettings.getLeftButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      this.activeState = activeState.left();
      navPlayer.play();
    }
    else if (button.equals(pauseMenuSettings.getRightButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      this.activeState = activeState.right();
      navPlayer.play();
    }
    else if (button.equals(pauseMenuSettings.getStartButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      enterPlayer.play();
      this.activeState = activeState.enter();
      LOG.info("Entered {}", this.activeState);
    }
  }

  @Override
  public void controllerEvent(String name) {
    GameController.getInstance().removeListener(StateMananger.getInstance());
    if (!running) {
      return;
    }
    Platform.runLater(() -> {
      handle(name);
      new Thread(() -> {
        try {
          Thread.sleep(PauseMenuUIDefaults.SELECTION_SCALE_DURATION);
          GameController.getInstance().addListener(StateMananger.getInstance());
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
    });
  }

  public void setControls(PauseMenuSettings pauseMenuSettings) {
    this.pauseMenuSettings = pauseMenuSettings;
  }

  public void setState(PauseMenuState state) {
    menuController.setInitialState(state);

    try {
      Thread.sleep(PauseMenuUIDefaults.SELECTION_SCALE_DURATION * 2);
    }
    catch (InterruptedException e) {
      //ignore
    }
    GameController.getInstance().addListener(StateMananger.getInstance());
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
    GameController.getInstance().removeListener(StateMananger.getInstance());
  }
}
