package de.mephisto.vpin.server.inputs;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.server.VPinStudioServerTray;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.KeyChecker;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Service
public class InputEventService implements InitializingBean, NativeKeyListener, PopperStatusChangeListener, PreferenceChangedListener, GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(InputEventService.class);

  @Autowired
  private PopperService popperService;

  @Autowired
  private OverlayClientImpl overlayClient;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private JobQueue queue;

  private boolean overlayVisible;
  private ShutdownThread shutdownThread;
  private boolean launchOverlayOnStartup = false;

  private boolean frontendIsRunning = false;
  private boolean vpxIsRunning = false;

  private Map<Integer, Long> timingMap = new ConcurrentHashMap<>();
  private PauseMenuSettings pauseMenuSettings;

  //-------------- Event Listening -------------------------------------------------------------------------------------
  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    shutdownThread.notifyKeyEvent();
    handleKeyEvent(nativeKeyEvent);
  }

  private void handleKeyEvent(NativeKeyEvent nativeKeyEvent) {
    if (isEventDebounced(nativeKeyEvent)) {
      return;
    }

    //always hide the overlay on any key press
    if (overlayVisible) {
      onOverlayHideEvent();
      return;
    }

    boolean showPauseInsteadOfOverlay = pauseMenuSettings.isUseOverlayKey();
    int pauseKey = pauseMenuSettings.getCustomPauseKey();
    int overlayKey = pauseMenuSettings.getCustomOverlayKey();

    if (overlayKey > 0) {
      KeyChecker keyChecker = new KeyChecker(overlayKey);
      if (keyChecker.matches(nativeKeyEvent) || (showPauseInsteadOfOverlay && nativeKeyEvent.getRawCode() == pauseMenuSettings.getCustomPauseKey())) {
        if (showPauseInsteadOfOverlay && vpxIsRunning) {
          onTogglePauseMenu();
          return;
        }

        onToggleOverlayEvent();
        return;
      }
    }

    //handle pause menu toggling
    if (pauseKey > 0) {
      KeyChecker keyChecker = new KeyChecker(pauseKey);
      if (keyChecker.matches(nativeKeyEvent)) {
        onPauseMenuEvent();
        return;
      }
    }

    //handle key based reset
    int resetKey = pauseMenuSettings.getCustomResetKey();
    if (resetKey > 0) {
      KeyChecker keyChecker = new KeyChecker(resetKey);
      if (keyChecker.matches(nativeKeyEvent)) {
        onResetEvent();
      }
    }
  }

  @Override
  public void controllerEvent(String name) {
    //always hide the overlay on any key press
    if (overlayVisible) {
      onOverlayHideEvent();
      return;
    }

    boolean showPauseInsteadOfOverlay = pauseMenuSettings.isUseOverlayKey();
    String pauseBtn = pauseMenuSettings.getCustomPauseButton();
    String overlayBtn = pauseMenuSettings.getCustomOverlayButton();

    if (overlayBtn != null) {
      if (name.equals(overlayBtn) || (showPauseInsteadOfOverlay && name.equals(pauseBtn))) {
        if (showPauseInsteadOfOverlay && vpxIsRunning) {
          onTogglePauseMenu();
          return;
        }

        onToggleOverlayEvent();
        return;
      }
    }

    //handle pause menu toggling
    if (name.equals(pauseBtn)) {
      onPauseMenuEvent();
      return;
    }

    //handle key based reset
    String resetBtn = pauseMenuSettings.getCustomResetButton();
    if (name.equals(resetBtn)) {
      onResetEvent();
    }
  }

  //-------------- Event Execution -------------------------------------------------------------------------------------

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  private void onToggleOverlayEvent() {
    this.overlayVisible = !overlayVisible;
    Platform.runLater(() -> {
      LOG.info("Toggle pause menu show, was visible: " + !overlayVisible);
      ServerFX.getInstance().showOverlay(overlayVisible);
    });
  }

  private void onTogglePauseMenu() {
    LOG.info("Toggle pause menu show");
    ServerFX.getInstance().togglePauseMenu();
  }

  private void onOverlayHideEvent() {
    LOG.info("Hiding overlay since key was pressed.");
    this.overlayVisible = false;
    Platform.runLater(() -> {
      ServerFX.getInstance().showOverlay(false);
    });
  }

  private void onPauseMenuEvent() {
    boolean vpxRunning = systemService.isVPXRunning();
    if (vpxRunning) {
      ServerFX.getInstance().togglePauseMenu();
    }
    else {
      ServerFX.getInstance().exitPauseMenu();
    }
  }

  private void onResetEvent() {
    new Thread(() -> {
      systemService.restartPopper();
      popperService.notifyPopperRestart();
    }).start();
  }

  private synchronized boolean isEventDebounced(NativeKeyEvent nativeKeyEvent) {
    long inputDeboundeMs = pauseMenuSettings.getInputDebounceMs();
    int pauseKey = pauseMenuSettings.getCustomPauseKey();
    int overlayKey = pauseMenuSettings.getCustomOverlayKey();

    if (inputDeboundeMs > 0) {
      if (overlayKey > 0) {
        KeyChecker keyChecker = new KeyChecker(overlayKey);
        if (keyChecker.matches(nativeKeyEvent)) {
          if (timingMap.containsKey(overlayKey) && (System.currentTimeMillis() - timingMap.get(overlayKey)) < inputDeboundeMs) {
            LOG.info("Debouncer: Skipped overlay key event, because it event within debounce range.");
            return true;
          }
          timingMap.put(overlayKey, System.currentTimeMillis());
          return false;
        }
      }

      if (pauseKey > 0) {
        KeyChecker keyChecker = new KeyChecker(pauseKey);
        if (keyChecker.matches(nativeKeyEvent)) {
          if (timingMap.containsKey(pauseKey) && (System.currentTimeMillis() - timingMap.get(pauseKey)) < inputDeboundeMs) {
            LOG.info("Debouncer: Skipped pause key event, because it event within debounce range.");
            return true;
          }
          timingMap.put(pauseKey, System.currentTimeMillis());
          return false;
        }
      }
    }
    return false;
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void popperLaunched() {
    refreshProcesses();

    Platform.runLater(() -> {
      if (this.launchOverlayOnStartup) {
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          //ignore
        }
        this.overlayVisible = true;
        ServerFX.getInstance().showOverlay(overlayVisible);
      }
    });
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    refreshProcesses();
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    refreshProcesses();
  }

  @Override
  public void popperExited() {
    refreshProcesses();
  }

  @Override
  public void popperRestarted() {
    refreshProcesses();
  }

  public void resetShutdownTimer() {
    shutdownThread.reset();
  }

  private void refreshProcesses() {
    List<ProcessHandle> processes = systemService.getProcesses();
    frontendIsRunning = systemService.isPopperMenuRunning(processes);
    vpxIsRunning = systemService.isVPXRunning(processes);
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      ServerFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    shutdownThread = new ShutdownThread(preferencesService, queue);
    shutdownThread.start();

    ServerFX.client = overlayClient;
    ServerFX.waitForOverlay();
    LOG.info("Finished initialization of OverlayWindowFX");

    new VPinStudioServerTray();
    LOG.info("Application tray created.");

    try {
      InetAddress localHost = InetAddress.getLocalHost();
      LOG.info("Server Address: " + localHost.getHostName() + "/" + localHost.getHostAddress());
    }
    catch (UnknownHostException e) {
      //
    }

    preferenceChanged(PreferenceNames.SHOW_OVERLAY_ON_STARTUP, null, null);
    preferenceChanged(PreferenceNames.PAUSE_MENU_SETTINGS, null, null);

    preferencesService.addChangeListener(this);

    boolean pinUPRunning = popperService.isPinUPRunning();
    if (pinUPRunning) {
      popperLaunched();
    }
    else {
      LOG.info("Added VPin service popper status listener.");
      popperService.addPopperStatusChangeListener(this);
    }

    try {
      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);
      GlobalScreen.addNativeKeyListener(this);
    }
    catch (Exception e) {
      LOG.error("Failed to register native key event hook: " + e.getMessage(), e);
    }

    GameController.getInstance();
    refreshProcesses();
    LOG.info("Server startup finished, running version is " + systemService.getVersion());
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    try {
      switch (propertyName) {
        case PreferenceNames.SHOW_OVERLAY_ON_STARTUP: {
          String startupLaunch = (String) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
          this.launchOverlayOnStartup = !StringUtils.isEmpty(startupLaunch) && Boolean.parseBoolean(startupLaunch);
          LOG.info("Show overlay on startup: " + this.launchOverlayOnStartup);
          break;
        }
        case PreferenceNames.PAUSE_MENU_SETTINGS: {
          pauseMenuSettings = preferencesService.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
          LOG.info("Pause key has been updated.");
          break;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error updating " + this.getClass().getSimpleName() + " settings: " + e.getMessage(), e);
    }
  }
}
