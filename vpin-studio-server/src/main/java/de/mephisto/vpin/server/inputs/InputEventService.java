package de.mephisto.vpin.server.inputs;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.server.VPinStudioServerTray;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.recorder.RecorderService;
import de.mephisto.vpin.server.recorder.ScreenshotService;
import de.mephisto.vpin.server.system.SystemService;
import javafx.application.Platform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InputEventService implements InitializingBean, TableStatusChangeListener, FrontendStatusChangeListener, PreferenceChangedListener, GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(InputEventService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private RecorderService recorderService;

  @Autowired
  private ScreenshotService screenshotService;

  @Autowired
  private JobQueue queue;

  private boolean overlayVisible;
  private ShutdownThread shutdownThread;
  private boolean launchOverlayOnStartup = false;

  private boolean frontendIsRunning = false;
  private boolean vpxIsRunning = false;

  private final Map<String, Long> timingMap = new ConcurrentHashMap<>();
  private PauseMenuSettings pauseMenuSettings;

  //-------------- Event Listening -------------------------------------------------------------------------------------

  @Override
  public void controllerEvent(String name) {
    //reset shutdown thread
    shutdownThread.notifyKeyEvent();

    if (isEventDebounced(name)) {
      return;
    }

    if (isEventFiltered(name)) {
      return;
    }

    //always hide the overlay on any key press
    if (overlayVisible) {
      onToggleOverlayEvent(name);
      return;
    }

    boolean showPauseInsteadOfOverlay = pauseMenuSettings.isUseOverlayKey();
    String pauseBtn = pauseMenuSettings.getPauseButton();
    String overlayBtn = pauseMenuSettings.getOverlayButton();
    String recordBtn = pauseMenuSettings.getRecordingButton();
    String screenshotBtn = pauseMenuSettings.getScreenshotButton();
    String resetBtn = pauseMenuSettings.getResetButton();

    if (name.equals(recordBtn)) {
      if (frontendStatusService.getGameStatus().isActive()) {
        LOG.info("Active game found for to recording, triggering recorder.");
        SLOG.info("Active game found for to recording, triggering recorder.");
        recorderService.startInGameRecording();
        return;
      }
      else {
        LOG.info("Record button pressed, but no active game found.");
        return;
      }
    }

    if (name.equals(screenshotBtn)) {
      if (frontendStatusService.getGameStatus().isActive()) {
        LOG.info("Active game found for to screenshot, starting generation.");
        screenshotService.takeScreenshots(frontendStatusService.getGameStatus().getGameId());
        return;
      }
      else {
        LOG.info("Screenshot button pressed, but no active game found, take menu screenshots.");
        screenshotService.takeScreenshots(-1);
        return;
      }
    }

    if (overlayBtn != null) {
      if (name.equals(overlayBtn) || (showPauseInsteadOfOverlay && name.equals(pauseBtn))) {
        if (showPauseInsteadOfOverlay && vpxIsRunning) {
          onTogglePauseMenu();
          return;
        }

        if (frontendIsRunning) {
          onToggleOverlayEvent(name);
          return;
        }
      }
    }

    //handle pause menu toggling
    if (name.equals(pauseBtn)) {
      onPauseMenuEvent();
      return;
    }

    //handle key based reset
    if (name.equals(resetBtn)) {
      onResetEvent();
      return;
    }
  }

  //-------------- Event Execution -------------------------------------------------------------------------------------

  private void onToggleOverlayEvent(String eventName) {
    LOG.info("Toggling overlay for key event '" + eventName + "'");
    this.overlayVisible = !overlayVisible;
    Platform.runLater(() -> {
      LOG.info("Toggle overlay visibility, was visible: " + !overlayVisible);
      SLOG.info("Toggle overlay visibility, was visible: " + !overlayVisible);
      ServerFX.getInstance().showOverlay(overlayVisible);
    });
  }

  private void onTogglePauseMenu() {
    LOG.info("Toggle pause menu show");
    SLOG.info("Toggle pause menu show");
    ServerFX.getInstance().togglePauseMenu();
  }

  private void onPauseMenuEvent() {
    boolean emulatorRunning = systemService.isPinballEmulatorRunning();
    if (emulatorRunning) {
      ServerFX.getInstance().togglePauseMenu();
    }
    else {
      ServerFX.getInstance().exitPauseMenu();
    }
  }

  private void onResetEvent() {
    frontendIsRunning = false;
    vpxIsRunning = false;
    new Thread(() -> {
      frontendService.restartFrontend();
      frontendStatusService.notifyFrontendRestart();
    }).start();
  }


  private boolean isEventFiltered(String name) {
    if (pauseMenuSettings != null) {
      String inputFilterList = pauseMenuSettings.getInputFilterList();
      if (!StringUtils.isEmpty(inputFilterList)) {
        String[] split = inputFilterList.toLowerCase().split(",");
        for (String s : split) {
          if (!StringUtils.isEmpty(s) && name.toLowerCase().contains(s)) {
            return true;
          }
        }
      }
    }
    return false;
  }


  private synchronized boolean isEventDebounced(String eventName) {
    long inputDebounceMs = pauseMenuSettings.getInputDebounceMs();
    String pauseKey = pauseMenuSettings.getPauseButton();
    String overlayKey = pauseMenuSettings.getOverlayButton();

    if (inputDebounceMs > 0) {
      if (overlayKey != null) {
        if (overlayKey.matches(eventName)) {
          if (timingMap.containsKey(overlayKey) && (System.currentTimeMillis() - timingMap.get(overlayKey)) < inputDebounceMs) {
            LOG.info("Debouncer: Skipped overlay key event, because it event within debounce range.");
            return true;
          }
          timingMap.put(overlayKey, System.currentTimeMillis());
          return false;
        }
      }

      if (pauseKey != null) {
        if (pauseKey.matches(eventName)) {
          if (timingMap.containsKey(pauseKey) && (System.currentTimeMillis() - timingMap.get(pauseKey)) < inputDebounceMs) {
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
  public void frontendLaunched() {
    frontendIsRunning = true;

    //disable this for recording mode
    if (!frontendStatusService.isEventsEnabled()) {
      return;
    }

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
    vpxIsRunning = true;
    frontendIsRunning = true;
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    vpxIsRunning = false;
    frontendIsRunning = true;
    ServerFX.getInstance().exitPauseMenu();
  }

  @Override
  public void frontendExited() {
    vpxIsRunning = false;
    frontendIsRunning = false;
    ServerFX.getInstance().exitPauseMenu();
  }

  @Override
  public void frontendRestarted() {
    frontendIsRunning = true;
    vpxIsRunning = false;
  }

  public void resetShutdownTimer() {
    shutdownThread.reset();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    timingMap.clear();

    try {
      switch (propertyName) {
        case PreferenceNames.OVERLAY_SETTINGS: {
          OverlaySettings overlaySettings = preferencesService.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
          this.launchOverlayOnStartup = overlaySettings.isShowOnStartup();
          LOG.info("Show overlay on startup: " + this.launchOverlayOnStartup);
          break;
        }
        case PreferenceNames.PAUSE_MENU_SETTINGS: {
          pauseMenuSettings = preferencesService.getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);
          LOG.info("Input settings have been reloaded.");
          break;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Error updating " + this.getClass().getSimpleName() + " settings: " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() {
    ServerFX.client = new VPinStudioClient("localhost");
    new Thread(() -> {
      ServerFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    shutdownThread = new ShutdownThread(preferencesService, queue);
    shutdownThread.start();

    ServerFX.waitForOverlay();
    ServerFX.getInstance().setOverlayTitle(
        frontendService.getFrontendType().equals(FrontendType.Popper) ? "PinUP Popper" : "VPin Studio Overlay");
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

    preferenceChanged(PreferenceNames.OVERLAY_SETTINGS, null, null);
    preferenceChanged(PreferenceNames.PAUSE_MENU_SETTINGS, null, null);

    preferencesService.addChangeListener(this);

    frontendIsRunning = frontendService.isFrontendRunning();
    if (frontendIsRunning) {
      frontendLaunched();
    }

    LOG.info("Added VPin service status listener.");
    frontendStatusService.addTableStatusChangeListener(this);
    frontendStatusService.addFrontendStatusChangeListener(this);

    GameController.getInstance().addListener(this);
    LOG.info("Server startup finished, running version is " + systemService.getVersion());
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
