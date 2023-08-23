package de.mephisto.vpin.server.keyevent;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.VPinStudioServerTray;
import de.mephisto.vpin.server.pinvol.PinVolService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
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
public class KeyEventService implements InitializingBean, NativeKeyListener, PopperStatusChangeListener {
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
  private PinVolService pinVolService;

  private boolean visible;

  private ShutdownThread shutdownThread;

  @Override
  public void afterPropertiesSet() throws NativeHookException {
    GlobalScreen.registerNativeHook();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);
    GlobalScreen.addNativeKeyListener(this);

    String hotkey = (String) preferencesService.getPreferenceValue("overlayKey");
    if (StringUtils.isEmpty(hotkey)) {
      LOG.warn("No overlay hotkey defined! Define a key binding on the overlay configuration tab.");
    }

    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    shutdownThread = new ShutdownThread(preferencesService);
    shutdownThread.start();

    OverlayWindowFX.client = overlayClient;
    OverlayWindowFX.waitForOverlay();
    LOG.info("Finished initialization of OverlayWindowFX");
    afterStartup();
  }


  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    shutdownThread.notifyKeyEvent();

    String hotkey = (String) preferencesService.getPreferenceValue(PreferenceNames.OVERLAY_KEY);
    String resetKey = (String) preferencesService.getPreferenceValue(PreferenceNames.RESET_KEY);

    if (!StringUtils.isEmpty(hotkey)) {
      KeyChecker keyChecker = new KeyChecker(hotkey);
      if (keyChecker.matches(nativeKeyEvent) || visible) {
        this.visible = !visible;
        Platform.runLater(() -> {
          LOG.info("Toggle show (Key " + hotkey + ")");
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
      String startupLaunch = (String) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
      if (!StringUtils.isEmpty(startupLaunch) && Boolean.parseBoolean(startupLaunch)) {
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

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {

  }

  @Override
  public void popperExited() {

  }

  @Override
  public void popperRestarted() {

  }

  public void resetShutdownTimer() {
    shutdownThread.reset();
  }

  private void afterStartup() {
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
  }
}
