package de.mephisto.vpin.commons.fx.pausemenu.states;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.MenuController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuState;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;


public class StateMananger implements GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * AudioClips instead of MediaPlayers: the clips are kept for the lifetime of the server, a MediaPlayer
   * would keep a complete media pipeline (incl. audio output) open the whole time, also while tables are played.
   */
  private final AudioClip navSound;
  private final AudioClip enterSound;
  private final AudioClip backSound;

  private MenuState activeState;

  private final static StateMananger INSTANCE = new StateMananger();

  private MenuController menuController;
  private volatile boolean running = false;
  private PauseMenuSettings pauseMenuSettings;

  /**
   * Incremented on every show and exit of the menu, so that delayed listener registrations of a previous
   * menu session are dropped.
   */
  private final AtomicInteger session = new AtomicInteger();


  public static StateMananger getInstance() {
    return INSTANCE;
  }

  private StateMananger() {
    navSound = createAudioClip("select.mp3");
    enterSound = createAudioClip("enter.mp3");
    backSound = createAudioClip("back.mp3");
  }

  private AudioClip createAudioClip(String name) {
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
            in.transferTo(out);
          }
          externalForm = tempFile.toURI().toString();
        }
        return new AudioClip(externalForm);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to create audio clip for " + name + ": " + e.getMessage());
    }
    return null;
  }

  private static void play(AudioClip clip) {
    if (clip != null) {
      clip.play();
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
      play(navSound);
    }
    else if (button.equals(pauseMenuSettings.getRightButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      this.activeState = activeState.right();
      play(navSound);
    }
    else if (button.equals(pauseMenuSettings.getStartButton())) {
      ServerFX.forceShow(PauseMenu.getInstance().getStage());
      play(enterSound);
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
    int currentSession = session.get();
    Platform.runLater(() -> {
      handle(name);
      //debounces the input, the listener is registered again once the selection animation has finished
      registerListenerDelayed(currentSession, PauseMenuUIDefaults.SELECTION_SCALE_DURATION);
    });
  }

  /**
   * Must be called on the FX thread. The delay is done with a PauseTransition, so the FX thread is not blocked.
   */
  private void registerListenerDelayed(int listenerSession, long delayMs) {
    PauseTransition delay = new PauseTransition(Duration.millis(delayMs));
    delay.setOnFinished(e -> {
      //the menu may have been closed (or re-opened) in the meantime
      if (running && session.get() == listenerSession) {
        GameController.getInstance().addListener(StateMananger.getInstance());
      }
    });
    delay.play();
  }

  public void setControls(PauseMenuSettings pauseMenuSettings) {
    this.pauseMenuSettings = pauseMenuSettings;
  }

  /**
   * Must be called on the FX thread.
   */
  public void setState(PauseMenuState state) {
    int currentSession = session.incrementAndGet();
    running = true;
    menuController.setInitialState(state);

    //ignore the input that opened the menu: only listen to the controller once the menu animation has finished
    registerListenerDelayed(currentSession, PauseMenuUIDefaults.SELECTION_SCALE_DURATION * 2L);
  }

  public boolean isRunning() {
    return running;
  }

  public void exit() {
    running = false;
    session.incrementAndGet();
    Platform.runLater(() -> {
      menuController.reset();
    });
    GameController.getInstance().removeListener(StateMananger.getInstance());
  }
}
