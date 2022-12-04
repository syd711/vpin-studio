package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperLaunchListener;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.KeyChecker;
import javafx.application.Platform;
import javafx.stage.Stage;
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

import java.util.logging.Level;

@Service
public class OverlayService implements InitializingBean, NativeKeyListener, PopperLaunchListener {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

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
      LOG.error("No overlay hotkey defined! Define a key binding on the overlay configuration tab and restart the service.");
    }

    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();
    overlayWindowFX = OverlayWindowFX.waitForOverlay();


    boolean pinUPRunning = popperService.isPinUPRunning();
    if (pinUPRunning) {
      popperLaunched();
    }
    else {
      LOG.info("Added VPin service popper status listener.");
      popperService.addPopperLaunchListener(this);
    }
  }


  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    String hotkey = (String) preferencesService.getPreferenceValue("overlayKey");
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
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }


  @Override
  public void popperLaunched() {
    Boolean startupLaunch = (Boolean) preferencesService.getPreferenceValue("overlayOnStartup");
    if (startupLaunch) {
      this.visible = !visible;
      overlayWindowFX.setVisible(visible);
    }
  }
}
