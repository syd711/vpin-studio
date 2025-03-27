package de.mephisto.vpin.ui.recorder.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.iscored.Score;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.recorder.RecordingWriteMode;
import de.mephisto.vpin.restclient.util.Ffmpeg;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class FFMpegOptionsDialogController implements DialogController, Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FFMpegOptionsDialogController.class);

  @FXML
  private Button okButton;

  @FXML
  private Button resetBtn;

  @FXML
  private TextField commandTextField;
  private VPinScreen vPinScreen;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onRestore() {
    commandTextField.setText(Ffmpeg.DEFAULT_COMMAND);
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(vPinScreen);
    option.setCustomFfmpegCommand(this.commandTextField.getText().trim());

    client.getPreferenceService().setJsonPreference(settings);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void setData(VPinScreen vPinScreen) {
    this.vPinScreen = vPinScreen;
    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(vPinScreen);

    if (!StringUtils.isEmpty(option.getCustomFfmpegCommand())) {
      commandTextField.setText(option.getCustomFfmpegCommand());
    }
    else {
      commandTextField.setText(Ffmpeg.DEFAULT_COMMAND);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
