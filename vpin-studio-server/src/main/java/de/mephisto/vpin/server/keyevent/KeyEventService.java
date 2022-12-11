package de.mephisto.vpin.server.keyevent;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.popper.PopperLaunchListener;
import de.mephisto.vpin.server.popper.PopperService;
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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.logging.Level;

@Service
public class KeyEventService implements InitializingBean, NativeKeyListener, PopperLaunchListener {
  private final static Logger LOG = LoggerFactory.getLogger(KeyEventService.class);

  @Autowired
  private PopperService popperService;

  @Autowired
  private OverlayClientImpl overlayClient;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private boolean visible;

  private OverlayWindowFX overlayWindowFX;

  @Override
  public void afterPropertiesSet() throws NativeHookException {
    GlobalScreen.registerNativeHook();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);
    GlobalScreen.addNativeKeyListener(this);

    String hotkey = (String) preferencesService.getPreferenceValue("overlayKey");
    if (StringUtils.isEmpty(hotkey)) {
      LOG.error("No overlay hotkey defined! Define a key binding on the overlay configuration tab.");
    }

    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();

    overlayWindowFX.client = overlayClient;
    overlayWindowFX = OverlayWindowFX.waitForOverlay();
  }


  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    String hotkey = (String) preferencesService.getPreferenceValue(PreferenceNames.OVERLAY_KEY);
    String resetKey = (String) preferencesService.getPreferenceValue(PreferenceNames.RESET_KEY);

    if (!StringUtils.isEmpty(hotkey)) {
      KeyChecker keyChecker = new KeyChecker(hotkey);
      if (keyChecker.matches(nativeKeyEvent) || visible) {
        this.visible = !visible;
        Platform.runLater(() -> {
          LOG.info("Toggle show (Key " + hotkey + ")");
          overlayWindowFX.setVisible(visible);
        });
      }
    }

    if(!StringUtils.isEmpty(resetKey)) {
      KeyChecker keyChecker = new KeyChecker(resetKey);
      if (keyChecker.matches(nativeKeyEvent)) {
        systemService.restartPopper();
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }


  @Override
  public void popperLaunched() {
    Platform.runLater(() -> {
      Boolean startupLaunch = (Boolean) preferencesService.getPreferenceValue(PreferenceNames.SHOW_OVERLAY_ON_STARTUP);
      if (startupLaunch) {
        this.visible = !visible;
        overlayWindowFX.setVisible(visible);
      }
    });
  }

  @EventListener(ApplicationReadyEvent.class)
  public void afterStartup() {
    boolean pinUPRunning = popperService.isPinUPRunning();
    if (pinUPRunning) {
      popperLaunched();
    }
    else {
      LOG.info("Added VPin service popper status listener.");
      popperService.addPopperLaunchListener(this);
    }
  }
}
