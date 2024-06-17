package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import static de.mephisto.vpin.ui.Studio.client;

public class BtnRecorderDialogController implements Initializable, DialogController, NativeKeyListener, GameControllerInputListener {
  private final static Logger LOG = LoggerFactory.getLogger(BtnRecorderDialogController.class);
  public static final String PRESS_KEY = "-- Press Key --";

  @FXML
  private Button cancelBtn;

  @FXML
  private Button okBtn;

  @FXML
  private Button bindStartBtn;

  @FXML
  private Button bindLaunchBtn;

  @FXML
  private Button bindLeftBtn;

  @FXML
  private Button bindOverlayBtn;

  @FXML
  private Button bindRightBtn;

  @FXML
  private Label keyCodeStart;

  @FXML
  private Label keyCodeLaunch;

  @FXML
  private Label keyCodeLeft;

  @FXML
  private Label keyCodeOverlay;

  @FXML
  private Label keyCodeRight;
  private PauseMenuSettings pauseMenuSettings;

  @FXML
  private void onBindClick(ActionEvent e) {
    refreshView();

    Button source = (Button) e.getSource();
    if (source.equals(bindLaunchBtn)) {
      bindLaunchBtn.setDisable(true);
      keyCodeLaunch.setText(PRESS_KEY);
      keyCodeLaunch.requestFocus();
    }
    else if (source.equals(bindStartBtn)) {
      bindStartBtn.setDisable(true);
      keyCodeStart.setText(PRESS_KEY);
      keyCodeStart.requestFocus();
    }
    else if (source.equals(bindLeftBtn)) {
      bindLeftBtn.setDisable(true);
      keyCodeLeft.setText(PRESS_KEY);
      keyCodeLeft.requestFocus();
    }
    else if (source.equals(bindRightBtn)) {
      bindRightBtn.setDisable(true);
      keyCodeRight.setText(PRESS_KEY);
      keyCodeRight.requestFocus();
    }
    else if (source.equals(bindOverlayBtn)) {
      bindOverlayBtn.setDisable(true);
      keyCodeOverlay.setText(PRESS_KEY);
      keyCodeOverlay.requestFocus();
    }
  }

  private void refreshView() {
    bindLaunchBtn.setDisable(false);
    bindStartBtn.setDisable(false);
    bindLeftBtn.setDisable(false);
    bindRightBtn.setDisable(false);
    bindOverlayBtn.setDisable(false);


    keyCodeLaunch.setText(getInputValue(pauseMenuSettings.getCustomLaunchKey(), pauseMenuSettings.getCustomLaunchButton()));
    keyCodeStart.setText(getInputValue(pauseMenuSettings.getCustomStartKey(), pauseMenuSettings.getCustomStartButton()));
    keyCodeLeft.setText(getInputValue(pauseMenuSettings.getCustomLeftKey(), pauseMenuSettings.getCustomLeftButton()));
    keyCodeRight.setText(getInputValue(pauseMenuSettings.getCustomRightKey(), pauseMenuSettings.getCustomRightButton()));
    keyCodeOverlay.setText(getInputValue(pauseMenuSettings.getCustomOverlayKey(), pauseMenuSettings.getCustomOverlayButton()));
  }

  private String getInputValue(int customKey, String customButton) {
    if (customKey > 0) {
      return String.valueOf(customKey);
    }
    if (!StringUtils.isEmpty(customButton)) {
      return customButton;
    }
    return "-";
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    doDestroy();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSave(ActionEvent e) {
    doDestroy();
    client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDelete(ActionEvent e) {
    pauseMenuSettings.setCustomLaunchKey(0);
    pauseMenuSettings.setCustomStartKey(0);
    pauseMenuSettings.setCustomLeftKey(0);
    pauseMenuSettings.setCustomRightKey(0);
    pauseMenuSettings.setCustomOverlayKey(0);
    client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    GameController.getInstance().addListener(this);

    try {
      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);
      GlobalScreen.addNativeKeyListener(this);
    }
    catch (Exception e) {
      LOG.error("Failed to bind key listener: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to bind key listener: " + e.getMessage());
    }

    refreshView();
  }

  @Override
  public void onDialogCancel() {
    doDestroy();
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    Platform.runLater(() -> {
      int code = nativeKeyEvent.getRawCode();
      String value = String.valueOf(code);
//      LOG.info("Recorded " + code + "/" + nativeKeyEvent.getKeyCode());
      if (bindLaunchBtn.isDisabled()) {
        bindLaunchBtn.setDisable(false);
        keyCodeLaunch.setText(value);
        pauseMenuSettings.setCustomLaunchKey(code);
        LOG.info("Registered " + value + " for launch.");
      }
      else if (bindStartBtn.isDisabled()) {
        bindStartBtn.setDisable(false);
        keyCodeStart.setText(value);
        pauseMenuSettings.setCustomStartKey(code);
        LOG.info("Registered " + value + " for start.");
      }
      else if (bindLeftBtn.isDisabled()) {
        bindLeftBtn.setDisable(false);
        keyCodeLeft.setText(value);
        pauseMenuSettings.setCustomLeftKey(code);
        LOG.info("Registered " + value + " for left.");
      }
      else if (bindRightBtn.isDisabled()) {
        bindRightBtn.setDisable(false);
        keyCodeRight.setText(value);
        pauseMenuSettings.setCustomRightKey(code);
        LOG.info("Registered " + value + " for right.");
      }
      else if (bindOverlayBtn.isDisabled()) {
        bindOverlayBtn.setDisable(false);
        keyCodeOverlay.setText(value);
        pauseMenuSettings.setCustomOverlayKey(code);
        LOG.info("Registered " + value + " for overlay.");
      }
    });
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void controllerEvent(String value) {
    Platform.runLater(() -> {
      if (bindLaunchBtn.isDisabled()) {
        bindLaunchBtn.setDisable(false);
        keyCodeLaunch.setText(value);
        pauseMenuSettings.setCustomLaunchButton(value);
        LOG.info("Registered " + value + " for launch.");
      }
      else if (bindStartBtn.isDisabled()) {
        bindStartBtn.setDisable(false);
        keyCodeStart.setText(value);
        pauseMenuSettings.setCustomStartButton(value);
        LOG.info("Registered " + value + " for start.");
      }
      else if (bindLeftBtn.isDisabled()) {
        bindLeftBtn.setDisable(false);
        keyCodeLeft.setText(value);
        pauseMenuSettings.setCustomLeftButton(value);
        LOG.info("Registered " + value + " for left.");
      }
      else if (bindRightBtn.isDisabled()) {
        bindRightBtn.setDisable(false);
        keyCodeRight.setText(value);
        pauseMenuSettings.setCustomRightButton(value);
        LOG.info("Registered " + value + " for right.");
      }
      else if (bindOverlayBtn.isDisabled()) {
        bindOverlayBtn.setDisable(false);
        keyCodeOverlay.setText(value);
        pauseMenuSettings.setCustomOverlayButton(value);
        LOG.info("Registered " + value + " for overlay.");
      }
    });
  }

  private void doDestroy() {
    GlobalScreen.removeNativeKeyListener(this);
    GameController.getInstance().removeListener(this);
  }
}
