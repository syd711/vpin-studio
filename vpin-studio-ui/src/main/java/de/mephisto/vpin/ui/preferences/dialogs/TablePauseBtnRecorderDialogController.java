package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TablePauseBtnRecorderDialogController implements Initializable, DialogController, NativeKeyListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablePauseBtnRecorderDialogController.class);
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
  private Button bindRightBtn;

  @FXML
  private Label keyCodeStart;

  @FXML
  private Label keyCodeLaunch;

  @FXML
  private Label keyCodeLeft;

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
  }

  private void refreshView() {
    bindLaunchBtn.setDisable(false);
    bindStartBtn.setDisable(false);
    bindLeftBtn.setDisable(false);
    bindRightBtn.setDisable(false);
    keyCodeLaunch.setText(pauseMenuSettings.getCustomLaunchKey() > 0 ? String.valueOf(pauseMenuSettings.getCustomLaunchKey()) : "-");
    keyCodeStart.setText(pauseMenuSettings.getCustomStartKey() > 0 ? String.valueOf(pauseMenuSettings.getCustomStartKey()) : "-");
    keyCodeLeft.setText(pauseMenuSettings.getCustomLeftKey() > 0 ? String.valueOf(pauseMenuSettings.getCustomLeftKey()) : "-");
    keyCodeRight.setText(pauseMenuSettings.getCustomRightKey() > 0 ? String.valueOf(pauseMenuSettings.getCustomRightKey()) : "-");
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSave(ActionEvent e) {
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
    client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    try {
      GlobalScreen.registerNativeHook();
      java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
      logger.setLevel(Level.OFF);
      logger.setUseParentHandlers(false);
      GlobalScreen.addNativeKeyListener(this);
    } catch (Exception e) {
      LOG.error("Failed to bind key listener: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to bind key listener: " + e.getMessage());
    }

    refreshView();
  }

  @Override
  public void onDialogCancel() {
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
    Platform.runLater(() -> {
      int keyCode = nativeKeyEvent.getKeyCode();
      String value = String.valueOf(keyCode);
      if (bindLaunchBtn.isDisabled()) {
        bindLaunchBtn.setDisable(false);
        keyCodeLaunch.setText(value);
        pauseMenuSettings.setCustomLaunchKey(keyCode);
        LOG.info("Registered " + value + " for launch.");
      }
      else if (bindStartBtn.isDisabled()) {
        bindStartBtn.setDisable(false);
        keyCodeStart.setText(value);
        pauseMenuSettings.setCustomStartKey(keyCode);
        LOG.info("Registered " + value + " for start.");
      }
      else if (bindLeftBtn.isDisabled()) {
        bindLeftBtn.setDisable(false);
        keyCodeLeft.setText(value);
        pauseMenuSettings.setCustomLeftKey(keyCode);
        LOG.info("Registered " + value + " for left.");
      }
      else if (bindRightBtn.isDisabled()) {
        bindRightBtn.setDisable(false);
        keyCodeRight.setText(value);
        pauseMenuSettings.setCustomRightKey(keyCode);
        LOG.info("Registered " + value + " for right.");
      }

    });
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }
}
