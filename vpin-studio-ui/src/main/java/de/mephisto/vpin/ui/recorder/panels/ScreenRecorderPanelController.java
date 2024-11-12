package de.mephisto.vpin.ui.recorder.panels;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordMode;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.monitor.MonitoringManager;
import de.mephisto.vpin.ui.recorder.RecorderController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class ScreenRecorderPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorderPanelController.class);

  private final static List<RecordMode> RECORD_MODE_LIST = Arrays.asList(RecordMode.ifMissing, RecordMode.overwrite, RecordMode.append);

  @FXML
  private Pane root;

  @FXML
  private Pane preview;

  @FXML
  private ImageView imageView;

  @FXML
  private Label screenName;

  @FXML
  private Label previewTitle;

  @FXML
  private Label previewLabel;

  @FXML
  private CheckBox fps60Checkbox;

  @FXML
  private Spinner<Integer> durationSpinner;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private ComboBox<RecordMode> recordModeComboBox;

  private RecordingScreen recordingScreen;

  public void setData(RecorderController recorderController, RecordingScreen recordingScreen) {
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(1040));

    this.recordingScreen = recordingScreen;
    screenName.setText(recordingScreen.getScreen().name());
    if (recordingScreen.getScreen().name().equalsIgnoreCase("Menu")) {
      screenName.setText(recordingScreen.getScreen().name() + "/FullDMD");
    }

    recordModeComboBox.setItems(FXCollections.observableList(RECORD_MODE_LIST));

    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);
    if (option == null) {
      option = new RecordingScreenOptions();
      option.setRecordingDuration(10);
      option.setDisplayName(recordingScreen.getScreen().name());
      option.setRecordMode(RecordMode.ifMissing);
      settings.getRecordingScreenOptions().add(option);

      client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings);
    }

    recordModeComboBox.setValue(option.getRecordMode());
    recordModeComboBox.valueProperty().addListener(new ChangeListener<RecordMode>() {
      @Override
      public void changed(ObservableValue<? extends RecordMode> observable, RecordMode oldValue, RecordMode newValue) {
        Platform.runLater(() -> {
          RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
          RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
          option2.setRecordMode(newValue);
          client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, s);
        });
      }
    });

    fps60Checkbox.setSelected(option.isFps60());
    fps60Checkbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
      RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
      option2.setFps60(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, s);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 3600, option.getRecordingDuration());
    durationSpinner.setValueFactory(factory);
    String key = "duration" + option.getDisplayName();
    durationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(key, () -> {
        RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        option2.setRecordingDuration(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, s);
      }, 300);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3600, option.getInitialDelay());
    delaySpinner.setValueFactory(factory1);
    String spinnerKey = "delay" + option.getDisplayName();
    delaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(spinnerKey, () -> {
        RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        option2.setInitialDelay(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, s);
      }, 300);
    });

    refresh();
  }

  public void refresh() {
    if(!root.isVisible()) {
      return;
    }

    previewTitle.setText("Screen Preview (" + recordingScreen.getDisplay().getWidth() + " x " + recordingScreen.getDisplay().getHeight() + ")");

    preview.setVisible(Studio.stage.widthProperty().intValue() > 1700);
    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);

    if (preview.isVisible()) {
      double w = preview.widthProperty().get();
      double h = preview.heightProperty().get();
      if (h > 300) {
        h = 300;
        w = h * 16 / 9;
      }

      if (w > 500) {
        w = 500;
        h = w * 9 / 16;
      }

      double width = w - 12;
      double height = width * 9 / 16;


      preview.setPrefWidth(w);
      preview.setPrefHeight(h * 8 / 16);

      imageView.setFitWidth(width);
      imageView.setFitHeight(height);

      Image image = MonitoringManager.getInstance().getRecordableScreenImage(recordingScreen);
      imageView.setImage(image);
    }
  }

  public void setVisible(boolean b) {
    root.setVisible(b);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
    preview.managedProperty().bindBidirectional(preview.visibleProperty());
    previewLabel.managedProperty().bindBidirectional(previewLabel.visibleProperty());
    imageView.managedProperty().bindBidirectional(imageView.visibleProperty());
  }

  public VPinScreen getScreen() {
    return recordingScreen.getScreen();
  }
}
