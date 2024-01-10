package de.mephisto.vpin.server.keyevent;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.server.VPinStudioServerTray;
import de.mephisto.vpin.server.games.Game;
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
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

@Service
public class KeyEventService implements InitializingBean, NativeKeyListener, PopperStatusChangeListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(KeyEventService.class);

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

  private boolean visible;
  private ShutdownThread shutdownThread;
  private boolean launchOverlayOnStartup = false;
  private String overlayKey;
  private String resetKey;
  private Game activeGame;

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    shutdownThread.notifyKeyEvent();

    if (!StringUtils.isEmpty(overlayKey)) {
      KeyChecker keyChecker = new KeyChecker(overlayKey);
      if (keyChecker.matches(nativeKeyEvent) || visible) {
        this.visible = !visible;
        Platform.runLater(() -> {
          LOG.info("Toggle show (Key " + overlayKey + ")");
          OverlayWindowFX.getInstance().setVisible(visible);
        });
      }
    }

    if (!StringUtils.isEmpty(resetKey)) {
      KeyChecker keyChecker = new KeyChecker(resetKey);
      if (keyChecker.matches(nativeKeyEvent)) {
        new Thread(() -> {
          systemService.restartPopper();
          popperService.notifyPopperRestart();
        }).start();
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void popperLaunched() {
    Platform.runLater(() -> {
      if (this.launchOverlayOnStartup) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          //ignore
        }
        this.visible = !visible;
        OverlayWindowFX.getInstance().setVisible(visible);
      }
    });
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    this.activeGame = event.getGame();
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    this.activeGame = null;
  }

  @Override
  public void popperExited() {
    this.activeGame = null;
  }

  @Override
  public void popperRestarted() {
    this.activeGame = null;
  }

  public void resetShutdownTimer() {
    shutdownThread.reset();
  }

  @Override
  public void afterPropertiesSet() throws NativeHookException {
    GlobalScreen.registerNativeHook();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);
    GlobalScreen.addNativeKeyListener(this);

    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    shutdownThread = new ShutdownThread(preferencesService, queue);
    shutdownThread.start();

    OverlayWindowFX.client = overlayClient;
    OverlayWindowFX.waitForOverlay();
    LOG.info("Finished initialization of OverlayWindowFX");

    new VPinStudioServerTray();
    LOG.info("Application tray created.");

    boolean pinUPRunning = popperService.isPinUPRunning();
    if (pinUPRunning) {
      popperLaunched();
    }
    else {
      LOG.info("Added VPin service popper status listener.");
      popperService.addPopperStatusChangeListener(this);
    }

    LOG.info("Server startup finished, running version is " + systemService.getVersion());

    try {
      InetAddress localHost = InetAddress.getLocalHost();
      LOG.info("Server Address: " + localHost.getHostName() + "/" + localHost.getHostAddress());
    } catch (UnknownHostException e) {
      //
    }
    preferenceChanged(PreferenceNames.SHOW_OVERLAY_ON_STARTUP, null, null);
    preferenceChanged(PreferenceNames.OVERLAY_KEY, null, null);
    preferenceChanged(PreferenceNames.RESET_KEY, null, null);

    preferencesService.addChangeListener(this);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    switch (propertyName) {
      case PreferenceNames.SHOW_OVERLAY_ON_STARTUP: {
        String startupLaunch = (String) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
        this.launchOverlayOnStartup = !StringUtils.isEmpty(startupLaunch) && Boolean.parseBoolean(startupLaunch);
        break;
      }
      case PreferenceNames.OVERLAY_KEY: {
        overlayKey = (String) preferencesService.getPreferenceValue(PreferenceNames.OVERLAY_KEY);
        break;
      }
      case PreferenceNames.RESET_KEY: {
        resetKey = (String) preferencesService.getPreferenceValue(PreferenceNames.RESET_KEY);
        break;
      }
    }
  }
}
