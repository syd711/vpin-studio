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
  private Button deleteStartBtn;

  @FXML
  private Button deletePauseBtn;

  @FXML
  private Button deleteLeftBtn;

  @FXML
  private Button deleteOverlayBtn;

  @FXML
  private Button deleteRightBtn;

  @FXML
  private Button deleteResetBtn;


  @FXML
  private Button bindStartBtn;

  @FXML
  private Button bindPauseBtn;

  @FXML
  private Button bindLeftBtn;

  @FXML
  private Button bindOverlayBtn;

  @FXML
  private Button bindRightBtn;

  @FXML
  private Button bindResetBtn;


  @FXML
  private Label keyCodeStart;

  @FXML
  private Label keyCodePause;

  @FXML
  private Label keyCodeLeft;

  @FXML
  private Label keyCodeRight;

  @FXML
  private Label keyCodeOverlay;

  @FXML
  private Label keyCodeReset;


  @FXML
  private Label keyValueStart;

  @FXML
  private Label keyValuePause;

  @FXML
  private Label keyValueLeft;

  @FXML
  private Label keyValueRight;

  @FXML
  private Label keyValueOverlay;

  @FXML
  private Label keyValueReset;


  private PauseMenuSettings pauseMenuSettings;

  @FXML
  private void onDeleteBinding(ActionEvent e) {
    Button source = (Button) e.getSource();
    if (source.equals(deletePauseBtn)) {
      pauseMenuSettings.setCustomPauseButton(null);
      pauseMenuSettings.setCustomPauseKey(0);
    }
    else if (source.equals(deleteStartBtn)) {
      pauseMenuSettings.setCustomStartButton(null);
      pauseMenuSettings.setCustomStartKey(0);
    }
    else if (source.equals(deleteLeftBtn)) {
      pauseMenuSettings.setCustomLeftButton(null);
      pauseMenuSettings.setCustomLeftKey(0);
    }
    else if (source.equals(deleteRightBtn)) {
      pauseMenuSettings.setCustomRightButton(null);
      pauseMenuSettings.setCustomRightKey(0);
    }
    else if (source.equals(deleteOverlayBtn)) {
      pauseMenuSettings.setCustomOverlayButton(null);
      pauseMenuSettings.setCustomOverlayKey(0);
    }
    else if (source.equals(deleteResetBtn)) {
      pauseMenuSettings.setCustomResetButton(null);
      pauseMenuSettings.setCustomResetKey(0);
    }
    refreshView();
  }

  @FXML
  private void onBindClick(ActionEvent e) {
    refreshView();
    Button source = (Button) e.getSource();
    if (source.equals(bindPauseBtn)) {
      bindPauseBtn.setDisable(true);
      keyCodePause.setText(PRESS_KEY);
      keyCodePause.requestFocus();
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
    else if (source.equals(bindResetBtn)) {
      bindResetBtn.setDisable(true);
      keyCodeReset.setText(PRESS_KEY);
      keyCodeReset.requestFocus();
    }
  }

  private void refreshView() {
    bindPauseBtn.setDisable(false);
    bindStartBtn.setDisable(false);
    bindLeftBtn.setDisable(false);
    bindRightBtn.setDisable(false);
    bindOverlayBtn.setDisable(false);
    bindResetBtn.setDisable(false);


    keyCodePause.setText(getInputValue(pauseMenuSettings.getCustomPauseKey(), pauseMenuSettings.getCustomPauseButton()));
    keyCodeStart.setText(getInputValue(pauseMenuSettings.getCustomStartKey(), pauseMenuSettings.getCustomStartButton()));
    keyCodeLeft.setText(getInputValue(pauseMenuSettings.getCustomLeftKey(), pauseMenuSettings.getCustomLeftButton()));
    keyCodeRight.setText(getInputValue(pauseMenuSettings.getCustomRightKey(), pauseMenuSettings.getCustomRightButton()));
    keyCodeOverlay.setText(getInputValue(pauseMenuSettings.getCustomOverlayKey(), pauseMenuSettings.getCustomOverlayButton()));
    keyCodeReset.setText(getInputValue(pauseMenuSettings.getCustomResetKey(), pauseMenuSettings.getCustomResetButton()));

    keyValuePause.setText(getInputAsciiValue(pauseMenuSettings.getCustomPauseKey()));
    keyValueStart.setText(getInputAsciiValue(pauseMenuSettings.getCustomStartKey()));
    keyValueLeft.setText(getInputAsciiValue(pauseMenuSettings.getCustomLeftKey()));
    keyValueRight.setText(getInputAsciiValue(pauseMenuSettings.getCustomRightKey()));
    keyValueOverlay.setText(getInputAsciiValue(pauseMenuSettings.getCustomOverlayKey()));
    keyValueReset.setText(getInputAsciiValue(pauseMenuSettings.getCustomResetKey()));
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

  private String getInputAsciiValue(int customKey) {
    if (customKey > 0) {
      char c = (char) customKey;
      return String.valueOf(c);
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
    pauseMenuSettings.setCustomPauseKey(0);
    pauseMenuSettings.setCustomStartKey(0);
    pauseMenuSettings.setCustomLeftKey(0);
    pauseMenuSettings.setCustomRightKey(0);
    pauseMenuSettings.setCustomOverlayKey(0);
    pauseMenuSettings.setCustomResetKey(0);

    pauseMenuSettings.setCustomPauseButton(null);
    pauseMenuSettings.setCustomStartButton(null);
    pauseMenuSettings.setCustomLeftButton(null);
    pauseMenuSettings.setCustomRightButton(null);
    pauseMenuSettings.setCustomOverlayButton(null);
    pauseMenuSettings.setCustomResetButton(null);
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
      if (bindPauseBtn.isDisabled()) {
        bindPauseBtn.setDisable(false);
        keyCodePause.setText(value);
        pauseMenuSettings.setCustomPauseKey(code);
        LOG.info("Registered " + value + " for pause.");
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
      else if (bindResetBtn.isDisabled()) {
        bindResetBtn.setDisable(false);
        keyCodeReset.setText(value);
        pauseMenuSettings.setCustomResetKey(code);
        LOG.info("Registered " + value + " for reset.");
      }
      refreshView();
    });
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void controllerEvent(String value) {
    Platform.runLater(() -> {
      if (bindPauseBtn.isDisabled()) {
        bindPauseBtn.setDisable(false);
        keyCodePause.setText(value);
        pauseMenuSettings.setCustomPauseButton(value);
        LOG.info("Registered " + value + " for pause.");
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
      else if (bindResetBtn.isDisabled()) {
        bindResetBtn.setDisable(false);
        keyCodeReset.setText(value);
        pauseMenuSettings.setCustomResetButton(value);
        LOG.info("Registered " + value + " for reset.");
      }
      refreshView();
    });
  }

  private void doDestroy() {
    GlobalScreen.removeNativeKeyListener(this);
    GameController.getInstance().removeListener(this);
  }
}
