package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class BtnRecorderDialogController implements Initializable, DialogController, GameControllerInputListener {
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
  private Button deleteRecordingBtn;


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
  private Button bindRecordingBtn;


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
  private Label keyCodeRecording;


  private PauseMenuSettings pauseMenuSettings;

  @FXML
  private void onDeleteBinding(ActionEvent e) {
    Button source = (Button) e.getSource();
    if (source.equals(deletePauseBtn)) {
      pauseMenuSettings.setPauseButton(null);
    }
    else if (source.equals(deleteStartBtn)) {
      pauseMenuSettings.setStartButton(null);
    }
    else if (source.equals(deleteLeftBtn)) {
      pauseMenuSettings.setLeftButton(null);
    }
    else if (source.equals(deleteRightBtn)) {
      pauseMenuSettings.setRightButton(null);
    }
    else if (source.equals(deleteOverlayBtn)) {
      pauseMenuSettings.setOverlayButton(null);
    }
    else if (source.equals(deleteResetBtn)) {
      pauseMenuSettings.setResetButton(null);
    }
    else if (source.equals(deleteRecordingBtn)) {
      pauseMenuSettings.setRecordingButton(null);
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
    else if (source.equals(bindRecordingBtn)) {
      bindRecordingBtn.setDisable(true);
      keyCodeRecording.setText(PRESS_KEY);
      keyCodeRecording.requestFocus();
    }
  }

  private void refreshView() {
    bindPauseBtn.setDisable(false);
    bindStartBtn.setDisable(false);
    bindLeftBtn.setDisable(false);
    bindRightBtn.setDisable(false);
    bindOverlayBtn.setDisable(false);
    bindResetBtn.setDisable(false);
    bindRecordingBtn.setDisable(false);


    keyCodePause.setText(getInputValue(pauseMenuSettings.getPauseButton()));
    keyCodeStart.setText(getInputValue(pauseMenuSettings.getStartButton()));
    keyCodeLeft.setText(getInputValue(pauseMenuSettings.getLeftButton()));
    keyCodeRight.setText(getInputValue(pauseMenuSettings.getRightButton()));
    keyCodeOverlay.setText(getInputValue(pauseMenuSettings.getOverlayButton()));
    keyCodeReset.setText(getInputValue(pauseMenuSettings.getResetButton()));
    keyCodeRecording.setText(getInputValue(pauseMenuSettings.getRecordingButton()));
  }

  private String getInputValue(String customButton) {
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
    pauseMenuSettings.setPauseButton(null);
    pauseMenuSettings.setStartButton(null);
    pauseMenuSettings.setLeftButton(null);
    pauseMenuSettings.setRightButton(null);
    pauseMenuSettings.setOverlayButton(null);
    pauseMenuSettings.setResetButton(null);
    pauseMenuSettings.setRecordingButton(null);
    client.getPreferenceService().setJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, pauseMenuSettings);
    refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    GameController.getInstance().addListener(this);
    refreshView();
  }

  @Override
  public void onDialogCancel() {
    doDestroy();
  }

  @Override
  public void controllerEvent(String value) {
    Platform.runLater(() -> {
      if (bindPauseBtn.isDisabled()) {
        bindPauseBtn.setDisable(false);
        keyCodePause.setText(value);
        pauseMenuSettings.setPauseButton(value);
        LOG.info("Registered " + value + " for pause.");
      }
      else if (bindStartBtn.isDisabled()) {
        bindStartBtn.setDisable(false);
        keyCodeStart.setText(value);
        pauseMenuSettings.setStartButton(value);
        LOG.info("Registered " + value + " for start.");
      }
      else if (bindLeftBtn.isDisabled()) {
        bindLeftBtn.setDisable(false);
        keyCodeLeft.setText(value);
        pauseMenuSettings.setLeftButton(value);
        LOG.info("Registered " + value + " for left.");
      }
      else if (bindRightBtn.isDisabled()) {
        bindRightBtn.setDisable(false);
        keyCodeRight.setText(value);
        pauseMenuSettings.setRightButton(value);
        LOG.info("Registered " + value + " for right.");
      }
      else if (bindOverlayBtn.isDisabled()) {
        bindOverlayBtn.setDisable(false);
        keyCodeOverlay.setText(value);
        pauseMenuSettings.setOverlayButton(value);
        LOG.info("Registered " + value + " for overlay.");
      }
      else if (bindResetBtn.isDisabled()) {
        bindResetBtn.setDisable(false);
        keyCodeReset.setText(value);
        pauseMenuSettings.setResetButton(value);
        LOG.info("Registered " + value + " for reset.");
      }
      else if (bindRecordingBtn.isDisabled()) {
        bindRecordingBtn.setDisable(false);
        keyCodeRecording.setText(value);
        pauseMenuSettings.setRecordingButton(value);
        LOG.info("Registered " + value + " for recording.");
      }
      refreshView();
    });
  }

  private void doDestroy() {
    GameController.getInstance().removeListener(this);
  }
}
