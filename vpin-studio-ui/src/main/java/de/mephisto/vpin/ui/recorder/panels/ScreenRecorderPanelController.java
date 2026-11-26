package de.mephisto.vpin.ui.recorder.panels;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingWriteMode;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.monitor.MonitoringManager;
import de.mephisto.vpin.ui.recorder.RecorderController;
import de.mephisto.vpin.ui.recorder.RecorderDialogs;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.util.StringConverter;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class ScreenRecorderPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorderPanelController.class);

  private final static List<RecordingWriteMode> RECORD_MODE_LIST = Arrays.asList(RecordingWriteMode.ifMissing, RecordingWriteMode.overwrite, RecordingWriteMode.append);
  /** the preview is only shown when the card is at least this wide (options column + a useful preview) */
  private static final double PREVIEW_MIN_CARD_WIDTH = 560;
  private static final double PREVIEW_MAX_WIDTH = 480;
  private static final double PREVIEW_MAX_HEIGHT = 240;

  @FXML
  private Pane root;

  @FXML
  private Pane preview;

  @FXML
  private StackPane previewBox;

  @FXML
  private ImageView imageView;

  @FXML
  private Label screenName;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Button settingsBtn;

  @FXML
  private CheckBox fps60Checkbox;

  @FXML
  private CheckBox inGameRecordingCheckbox;

  @FXML
  private CheckBox expertModeCheckbox;

  @FXML
  private CheckBox rotationCheckbox;

  @FXML
  private Spinner<Integer> durationSpinner;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private Pane rotationPanel;

  @FXML
  private Pane fpsPanel;

  @FXML
  private ComboBox<RecordingWriteMode> recordModeComboBox;

  private FrontendPlayerDisplay recordingScreen;

  @FXML
  private void onSettings() {
    RecorderDialogs.openRecordingSettings(recordingScreen.getScreen());
  }

  public void setData(RecorderController recorderController, FrontendPlayerDisplay recordingScreen) {
    this.recordingScreen = recordingScreen;
    screenName.setText(recordingScreen.getScreen().name());
    if (recordingScreen.getScreen().name().equalsIgnoreCase("Menu")) {
      screenName.setText(recordingScreen.getScreen().name() + "/FullDMD");
    }
    resolutionLabel.setText(Messages.get("recorder.screen.resolution", recordingScreen.getWidth(), recordingScreen.getHeight()));

    // the preview takes the space next to the options and is hidden when the card gets too narrow
    root.widthProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(this::refresh));

    recordModeComboBox.setItems(FXCollections.observableList(RECORD_MODE_LIST));
    recordModeComboBox.setConverter(new StringConverter<RecordingWriteMode>() {
      @Override
      public String toString(RecordingWriteMode mode) {
        if (mode == null) {
          return "";
        }
        switch (mode) {
          case overwrite: return Messages.get("recorder.screen_recorder_panel.record_mode.overwrite");
          case append: return Messages.get("recorder.screen_recorder_panel.record_mode.append");
          case ifMissing:
          default: return Messages.get("recorder.screen_recorder_panel.record_mode.if_missing");
        }
      }

      @Override
      public RecordingWriteMode fromString(String s) {
        return recordModeComboBox.getValue();
      }
    });

    RecorderSettings settingsLookup = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    if (settingsLookup == null) {
      settingsLookup = new RecorderSettings();
    }
    final RecorderSettings settings = settingsLookup;

    RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);
    if (option == null) {
      option = new RecordingScreenOptions();
      option.setRecordingDuration(10);
      option.setDisplayName(recordingScreen.getScreen().name());
      option.setRecordMode(RecordingWriteMode.ifMissing);
      settings.getRecordingScreenOptions().add(option);
    }

    recordModeComboBox.setValue(option.getRecordMode());
    recordModeComboBox.valueProperty().addListener(new ChangeListener<RecordingWriteMode>() {
      @Override
      public void changed(ObservableValue<? extends RecordingWriteMode> observable, RecordingWriteMode oldValue, RecordingWriteMode newValue) {
        Platform.runLater(() -> {
          RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
          if (s != null) {
            RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
            if (option2 != null) {
              option2.setRecordMode(newValue);
              client.getPreferenceService().setJsonPreference(s);
            }
          }
        });
      }
    });

    fps60Checkbox.setSelected(option.isFps60());
    fps60Checkbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
      if (s != null) {
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        if (option2 != null) {
          option2.setFps60(t1);
          client.getPreferenceService().setJsonPreference(s);
        }
      }
    });


    expertModeCheckbox.setSelected(option.isExpertSettingsEnabled());
    expertModeCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
      if (s != null) {
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        if (option2 != null) {
          option2.setExpertSettingsEnabled(t1);
          client.getPreferenceService().setJsonPreference(s);

          fpsPanel.setVisible(!t1);
          rotationPanel.setVisible(!t1 && VPinScreen.PlayField.equals(this.recordingScreen.getScreen()));
          settingsBtn.setDisable(!t1);
        }
      }
    });

    inGameRecordingCheckbox.setSelected(option.isInGameRecording());
    inGameRecordingCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
      if (s != null) {
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        if (option2 != null) {
          option2.setInGameRecording(t1);
          client.getPreferenceService().setJsonPreference(s);
        }
      }
    });


    settingsBtn.setDisable(!option.isExpertSettingsEnabled());
    fpsPanel.setVisible(!option.isExpertSettingsEnabled());

    rotationPanel.setVisible(recordingScreen.getScreen().equals(VPinScreen.PlayField) && !option.isExpertSettingsEnabled());
    rotationCheckbox.setSelected(option.isRotated());
    if (rotationPanel.isVisible() && option.isRotated()) {
      imageView.setRotate(180);
    }

    rotationCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
      if (s != null) {
        RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
        if (option2 != null) {
          option2.setRotated(t1);
          client.getPreferenceService().setJsonPreference(s);

          if (recordingScreen.getScreen().equals(VPinScreen.PlayField)) {
            if (t1) {
              imageView.setRotate(180);
            }
            else {
              imageView.setRotate(0);
            }
          }
        }
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 3600, option.getRecordingDuration());
    factory.setConverter(new StringConverter<Integer>() {
      @Override public String toString(Integer v)   { return v == null ? "3" : v.toString(); }
      @Override public Integer fromString(String s) { return s.isEmpty() ? 3 : Integer.parseInt(s); }
    });
    durationSpinner.setValueFactory(factory);
    durationSpinner.getEditor().setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
    String key = "duration" + option.getDisplayName();
    durationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(key, () -> {
        RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        if (s != null) {
          RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
          if (option2 != null) {
            option2.setRecordingDuration(newValue);
            client.getPreferenceService().setJsonPreference(s);
          }
        }
      }, 300);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3600, option.getInitialDelay());
    factory1.setConverter(new StringConverter<Integer>() {
      @Override public String toString(Integer v)   { return v == null ? "0" : v.toString(); }
      @Override public Integer fromString(String s) { return s.isEmpty() ? 0 : Integer.parseInt(s); }
    });
    delaySpinner.setValueFactory(factory1);
    delaySpinner.getEditor().setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
    String spinnerKey = "delay" + option.getDisplayName();
    delaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce(spinnerKey, () -> {
        RecorderSettings s = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        if (s != null) {
          RecordingScreenOptions option2 = s.getRecordingScreenOption(recordingScreen);
          if (option2 != null) {
            option2.setInitialDelay(newValue);
            client.getPreferenceService().setJsonPreference(s);
          }
        }
      }, 300);
    });

    refresh();
  }

  public void refresh() {
    if (!root.isVisible()) {
      return;
    }

    boolean showPreview = root.getWidth() >= PREVIEW_MIN_CARD_WIDTH;
    if (preview.isVisible() != showPreview) {
      preview.setVisible(showPreview);
    }

    if (showPreview) {
      Insets insets = previewBox.getInsets();
      double width = Math.min(PREVIEW_MAX_WIDTH, preview.getWidth() - insets.getLeft() - insets.getRight());
      if (width <= 0) {
        return;
      }
      double height = Math.min(PREVIEW_MAX_HEIGHT, width * 9 / 16);
      imageView.setFitWidth(width);
      imageView.setFitHeight(height);

      Image image = MonitoringManager.getInstance().getRecordableScreenImage(recordingScreen);
      imageView.setImage(image);
    }
  }

  public void setVisible(boolean b) {
    root.setVisible(b);
  }

  public boolean isVisible() {
    return root.isVisible();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    root.managedProperty().bindBidirectional(root.visibleProperty());
    preview.managedProperty().bindBidirectional(preview.visibleProperty());
    fpsPanel.managedProperty().bindBidirectional(fpsPanel.visibleProperty());
    rotationPanel.managedProperty().bindBidirectional(rotationPanel.visibleProperty());
  }

  public VPinScreen getScreen() {
    return recordingScreen.getScreen();
  }
}
