package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.controller.GameController;
import de.mephisto.vpin.commons.utils.controller.GameControllerInputListener;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
  private Button deleteScreenshotBtn;

  @FXML
  private Button deleteRightBtn;

  @FXML
  private Button deleteResetBtn;

  @FXML
  private Button deleteRecordingBtn;

  @FXML
  private Button bindScreenshotBtn;

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
  private Label keyCodeScreenshot;

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

  @FXML
  private CheckBox expertModeCheckbox;

  @FXML
  private TextField textScreenshotBtn;

  @FXML
  private TextField textStartBtn;

  @FXML
  private TextField textPauseBtn;

  @FXML
  private TextField textLeftBtn;

  @FXML
  private TextField textOverlayBtn;

  @FXML
  private TextField textRightBtn;

  @FXML
  private TextField textResetBtn;

  @FXML
  private TextField textRecordingBtn;


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
    else if (source.equals(deleteScreenshotBtn)) {
      pauseMenuSettings.setScreenshotButton(null);
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
      textPauseBtn.setDisable(true);
      keyCodePause.setText(PRESS_KEY);
      keyCodePause.requestFocus();
    }
    else if (source.equals(bindStartBtn)) {
      bindStartBtn.setDisable(true);
      textStartBtn.setDisable(true);
      keyCodeStart.setText(PRESS_KEY);
      keyCodeStart.requestFocus();
    }
    else if (source.equals(bindLeftBtn)) {
      bindLeftBtn.setDisable(true);
      textLeftBtn.setDisable(true);
      keyCodeLeft.setText(PRESS_KEY);
      keyCodeLeft.requestFocus();
    }
    else if (source.equals(bindRightBtn)) {
      bindRightBtn.setDisable(true);
      textRightBtn.setDisable(true);
      keyCodeRight.setText(PRESS_KEY);
      keyCodeRight.requestFocus();
    }
    else if (source.equals(bindOverlayBtn)) {
      bindOverlayBtn.setDisable(true);
      textOverlayBtn.setDisable(true);
      keyCodeOverlay.setText(PRESS_KEY);
      keyCodeOverlay.requestFocus();
    }
    else if (source.equals(bindScreenshotBtn)) {
      bindScreenshotBtn.setDisable(true);
      textScreenshotBtn.setDisable(true);
      keyCodeScreenshot.setText(PRESS_KEY);
      keyCodeScreenshot.requestFocus();
    }
    else if (source.equals(bindResetBtn)) {
      bindResetBtn.setDisable(true);
      textResetBtn.setDisable(true);
      keyCodeReset.setText(PRESS_KEY);
      keyCodeReset.requestFocus();
    }
    else if (source.equals(bindRecordingBtn)) {
      bindRecordingBtn.setDisable(true);
      textRecordingBtn.setDisable(true);
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
    bindScreenshotBtn.setDisable(false);

    keyCodePause.setText(getInputValue(pauseMenuSettings.getPauseButton()));
    keyCodeStart.setText(getInputValue(pauseMenuSettings.getStartButton()));
    keyCodeLeft.setText(getInputValue(pauseMenuSettings.getLeftButton()));
    keyCodeRight.setText(getInputValue(pauseMenuSettings.getRightButton()));
    keyCodeOverlay.setText(getInputValue(pauseMenuSettings.getOverlayButton()));
    keyCodeScreenshot.setText(getInputValue(pauseMenuSettings.getScreenshotButton()));
    keyCodeReset.setText(getInputValue(pauseMenuSettings.getResetButton()));
    keyCodeRecording.setText(getInputValue(pauseMenuSettings.getRecordingButton()));

    textScreenshotBtn.setText(pauseMenuSettings.getScreenshotButton());
    textStartBtn.setText(pauseMenuSettings.getStartButton());
    textPauseBtn.setText(pauseMenuSettings.getPauseButton());
    textLeftBtn.setText(pauseMenuSettings.getLeftButton());
    textOverlayBtn.setText(pauseMenuSettings.getOverlayButton());
    textRightBtn.setText(pauseMenuSettings.getRightButton());
    textResetBtn.setText(pauseMenuSettings.getResetButton());
    textRecordingBtn.setText(pauseMenuSettings.getRecordingButton());
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
    client.getPreferenceService().setJsonPreference(pauseMenuSettings);
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
    pauseMenuSettings.setScreenshotButton(null);
    client.getPreferenceService().setJsonPreference(pauseMenuSettings);
    refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pauseMenuSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.PAUSE_MENU_SETTINGS, PauseMenuSettings.class);

    textScreenshotBtn.managedProperty().bindBidirectional(textScreenshotBtn.visibleProperty());
    textStartBtn.managedProperty().bindBidirectional(textStartBtn.visibleProperty());
    textPauseBtn.managedProperty().bindBidirectional(textPauseBtn.visibleProperty());
    textLeftBtn.managedProperty().bindBidirectional(textLeftBtn.visibleProperty());
    textOverlayBtn.managedProperty().bindBidirectional(textOverlayBtn.visibleProperty());
    textRightBtn.managedProperty().bindBidirectional(textRightBtn.visibleProperty());
    textResetBtn.managedProperty().bindBidirectional(textResetBtn.visibleProperty());
    textRecordingBtn.managedProperty().bindBidirectional(textRecordingBtn.visibleProperty());

    textScreenshotBtn.setVisible(false);
    textStartBtn.setVisible(false);
    textPauseBtn.setVisible(false);
    textLeftBtn.setVisible(false);
    textOverlayBtn.setVisible(false);
    textRightBtn.setVisible(false);
    textResetBtn.setVisible(false);
    textRecordingBtn.setVisible(false);

    keyCodeScreenshot.managedProperty().bindBidirectional(keyCodeScreenshot.visibleProperty());
    textScreenshotBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setScreenshotButton(newValue));

    keyCodeStart.managedProperty().bindBidirectional(keyCodeStart.visibleProperty());
    textStartBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setStartButton(newValue));

    keyCodePause.managedProperty().bindBidirectional(keyCodePause.visibleProperty());
    textPauseBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setPauseButton(newValue));

    keyCodeLeft.managedProperty().bindBidirectional(keyCodeLeft.visibleProperty());
    textLeftBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setLeftButton(newValue));

    keyCodeOverlay.managedProperty().bindBidirectional(keyCodeOverlay.visibleProperty());
    textOverlayBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setOverlayButton(newValue));

    keyCodeRight.managedProperty().bindBidirectional(keyCodeRight.visibleProperty());
    textRightBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setRightButton(newValue));

    keyCodeReset.managedProperty().bindBidirectional(keyCodeReset.visibleProperty());
    textResetBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setResetButton(newValue));

    keyCodeRecording.managedProperty().bindBidirectional(keyCodeRecording.visibleProperty());
    textRecordingBtn.textProperty().addListener((observable, oldValue, newValue) -> pauseMenuSettings.setRecordingButton(newValue));

    expertModeCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        textScreenshotBtn.setVisible(newValue);
        textStartBtn.setVisible(newValue);
        textPauseBtn.setVisible(newValue);
        textLeftBtn.setVisible(newValue);
        textOverlayBtn.setVisible(newValue);
        textRightBtn.setVisible(newValue);
        textResetBtn.setVisible(newValue);
        textRecordingBtn.setVisible(newValue);

        keyCodeScreenshot.setVisible(!newValue);
        keyCodeStart.setVisible(!newValue);
        keyCodePause.setVisible(!newValue);
        keyCodeLeft.setVisible(!newValue);
        keyCodeOverlay.setVisible(!newValue);
        keyCodeRight.setVisible(!newValue);
        keyCodeReset.setVisible(!newValue);
        keyCodeRecording.setVisible(!newValue);
      }
    });

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
        textPauseBtn.setDisable(false);
        textPauseBtn.setText(value);
        keyCodePause.setText(value);
        pauseMenuSettings.setPauseButton(value);
        LOG.info("Registered " + value + " for pause.");
      }
      else if (bindStartBtn.isDisabled()) {
        bindStartBtn.setDisable(false);
        textStartBtn.setDisable(false);
        textStartBtn.setText(value);
        keyCodeStart.setText(value);
        pauseMenuSettings.setStartButton(value);
        LOG.info("Registered " + value + " for start.");
      }
      else if (bindLeftBtn.isDisabled()) {
        bindLeftBtn.setDisable(false);
        textLeftBtn.setDisable(false);
        keyCodeLeft.setText(value);
        textLeftBtn.setText(value);
        pauseMenuSettings.setLeftButton(value);
        LOG.info("Registered " + value + " for left.");
      }
      else if (bindRightBtn.isDisabled()) {
        bindRightBtn.setDisable(false);
        textRightBtn.setDisable(false);
        keyCodeRight.setText(value);
        textRightBtn.setText(value);
        pauseMenuSettings.setRightButton(value);
        LOG.info("Registered " + value + " for right.");
      }
      else if (bindOverlayBtn.isDisabled()) {
        bindOverlayBtn.setDisable(false);
        textOverlayBtn.setDisable(false);
        keyCodeOverlay.setText(value);
        textOverlayBtn.setText(value);
        pauseMenuSettings.setOverlayButton(value);
        LOG.info("Registered " + value + " for overlay.");
      }
      else if (bindScreenshotBtn.isDisabled()) {
        bindScreenshotBtn.setDisable(false);
        textScreenshotBtn.setDisable(false);
        keyCodeScreenshot.setText(value);
        textScreenshotBtn.setText(value);
        pauseMenuSettings.setScreenshotButton(value);
        LOG.info("Registered " + value + " for screenshots.");
      }
      else if (bindResetBtn.isDisabled()) {
        bindResetBtn.setDisable(false);
        textResetBtn.setDisable(false);
        keyCodeReset.setText(value);
        textResetBtn.setText(value);
        pauseMenuSettings.setResetButton(value);
        LOG.info("Registered " + value + " for reset.");
      }
      else if (bindRecordingBtn.isDisabled()) {
        bindRecordingBtn.setDisable(false);
        textRecordingBtn.setDisable(false);
        keyCodeRecording.setText(value);
        textRecordingBtn.setText(value);
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
