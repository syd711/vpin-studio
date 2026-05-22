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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;


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
    navPlayer = createMediaPlayer("select.mp3");
    enterPlayer = createMediaPlayer("enter.mp3");
    backPlayer = createMediaPlayer("back.mp3");
  }

  private MediaPlayer createMediaPlayer(String name) {
    try {
      URL resource = PauseMenu.class.getResource(name);
      if (resource != null) {
        String externalForm = resource.toExternalForm();
        // Handle "nested" or "jar" protocols by extracting to temp file if necessary
        if (externalForm.startsWith("nested") || externalForm.startsWith("jar")) {
          File tempFile = File.createTempFile("vpin-studio-sound-", "-" + name);
          tempFile.deleteOnExit();
          try (InputStream in = resource.openStream();
               FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
              out.write(buffer, 0, read);
            }
          }
          externalForm = tempFile.toURI().toString();
        }

        Media media = new Media(externalForm);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(() -> {
          mediaPlayer.stop();
          mediaPlayer.seek(Duration.ZERO);
        });
        return mediaPlayer;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create media player for " + name + ": " + e.getMessage());
    }
    return null;
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
      if (navPlayer != null) {
        navPlayer.play();
      }
    }
    else if (button.equals(pauseMenuSettings.getRightButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      this.activeState = activeState.right();
      if (navPlayer != null) {
        navPlayer.play();
      }
    }
    else if (button.equals(pauseMenuSettings.getStartButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      if (enterPlayer != null) {
        enterPlayer.play();
      }
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
