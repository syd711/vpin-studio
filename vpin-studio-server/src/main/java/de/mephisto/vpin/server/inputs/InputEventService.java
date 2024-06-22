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
public class InputEventService implements InitializingBean, PopperStatusChangeListener, PreferenceChangedListener, GameControllerInputListener {
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

  private final Map<String, Long> timingMap = new ConcurrentHashMap<>();
  private PauseMenuSettings pauseMenuSettings;

  //-------------- Event Listening -------------------------------------------------------------------------------------

  @Override
  public void controllerEvent(String name) {
    if (isEventDebounced(name)) {
      return;
    }

    //always hide the overlay on any key press
    if (overlayVisible) {
      onToggleOverlayEvent();
      return;
    }

    boolean showPauseInsteadOfOverlay = pauseMenuSettings.isUseOverlayKey();
    String pauseBtn = pauseMenuSettings.getPauseButton();
    String overlayBtn = pauseMenuSettings.getOverlayButton();

    if (overlayBtn != null) {
      if (name.equals(overlayBtn) || (showPauseInsteadOfOverlay && name.equals(pauseBtn))) {
        if (showPauseInsteadOfOverlay && vpxIsRunning) {
          onTogglePauseMenu();
          return;
        }

        if (frontendIsRunning) {
          onToggleOverlayEvent();
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
    String resetBtn = pauseMenuSettings.getResetButton();
    if (name.equals(resetBtn)) {
      onResetEvent();
    }
  }

  //-------------- Event Execution -------------------------------------------------------------------------------------

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
    frontendIsRunning = false;
    vpxIsRunning = false;
    new Thread(() -> {
      systemService.restartPopper();
      popperService.notifyPopperRestart();
    }).start();
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
  public void popperLaunched() {
    frontendIsRunning = true;

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
    vpxIsRunning = false;
    frontendIsRunning = true;
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    vpxIsRunning = false;
    frontendIsRunning = true;
  }

  @Override
  public void popperExited() {
    vpxIsRunning = false;
    frontendIsRunning = false;
  }

  @Override
  public void popperRestarted() {
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
        case PreferenceNames.SHOW_OVERLAY_ON_STARTUP: {
          String startupLaunch = (String) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
          this.launchOverlayOnStartup = !StringUtils.isEmpty(startupLaunch) && Boolean.parseBoolean(startupLaunch);
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

    frontendIsRunning = popperService.isPinUPRunning();
    if (frontendIsRunning) {
      popperLaunched();
    }

    LOG.info("Added VPin service popper status listener.");
    popperService.addPopperStatusChangeListener(this);

    GameController.getInstance().addListener(this);
    LOG.info("Server startup finished, running version is " + systemService.getVersion());
  }
}
