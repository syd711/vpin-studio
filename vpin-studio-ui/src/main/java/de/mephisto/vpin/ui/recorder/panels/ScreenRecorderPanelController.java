package de.mephisto.vpin.ui.recorder.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.recorder.RecordMode;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.recorder.RecorderController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.restclient.client.VPinStudioClient.API;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class ScreenRecorderPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ScreenRecorderPanelController.class);

  private final static List<RecordMode> RECORD_MODE_LIST = Arrays.asList(RecordMode.ifMissing, RecordMode.overwrite);
  private static final int DEBOUNCE_MS = 100;

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
  private CheckBox enabledCheckbox;

  @FXML
  private Spinner<Integer> durationSpinner;

  @FXML
  private Spinner<Integer> delaySpinner;

  @FXML
  private ComboBox<RecordMode> recordModeComboBox;

  private RecordingScreen recordingScreen;

  private Image imageCached = null;
  private ChangeListener<Boolean> enabledCheckboxListener;

  public void setData(RecorderController recorderController, RecordingScreen recordingScreen) {
    root.prefWidthProperty().bind(Studio.stage.widthProperty().subtract(960));

    this.recordingScreen = recordingScreen;
    screenName.setText(recordingScreen.getScreen().name());

    recordModeComboBox.setItems(FXCollections.observableList(RECORD_MODE_LIST));

    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);
    if (option == null) {
      option = new RecordingScreenOptions();
      option.setEnabled(true);
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
        RecorderSettings settings2 = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option2 = settings.getRecordingScreenOption(recordingScreen);
        option2.setRecordMode(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings2);
      }
    });

    enabledCheckbox.setSelected(option.isEnabled());
    enabledCheckboxListener = getEnabledCheckboxListener(recorderController, recordingScreen);
    enabledCheckbox.selectedProperty().addListener(enabledCheckboxListener);

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 3600, option.getRecordingDuration());
    durationSpinner.setValueFactory(factory);
    durationSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("duration", () -> {
        RecorderSettings settings2 = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option2 = settings.getRecordingScreenOption(recordingScreen);
        option2.setRecordingDuration(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings2);
      }, 500);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory1 = new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 3600, option.getInitialDelay());
    delaySpinner.setValueFactory(factory1);
    delaySpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
      debouncer.debounce("delay", () -> {
        RecorderSettings settings2 = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option2 = settings.getRecordingScreenOption(recordingScreen);
        option2.setInitialDelay(newValue);
        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings2);
      }, 500);
    });

    refresh();
  }

  @NotNull
  private  ChangeListener<Boolean> getEnabledCheckboxListener(RecorderController recorderController, RecordingScreen recordingScreen) {
    return new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
        RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);
        option.setEnabled(newValue);

        client.getPreferenceService().setJsonPreference(PreferenceNames.RECORDER_SETTINGS, settings);
        recorderController.refreshSelection();
        refresh();
      }
    };
  }

  public void invalidate() {
    imageCached = new Image(client.getRestClient().getBaseUrl() + API + "recorder/preview/" + recordingScreen.getScreen().name());
  }

  public void refresh() {
    previewTitle.setText("Screen Preview (" + recordingScreen.getDisplay().getWidth() + " x " + recordingScreen.getDisplay().getHeight() + ")");

    preview.setVisible(Studio.stage.widthProperty().intValue() > 1500);
    RecorderSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);
    RecordingScreenOptions option = settings.getRecordingScreenOption(recordingScreen);

    enabledCheckbox.selectedProperty().removeListener(enabledCheckboxListener);
    enabledCheckbox.setSelected(option.isEnabled());
    enabledCheckbox.selectedProperty().addListener(enabledCheckboxListener);

    if (option.isEnabled()) {
      if (!root.getStyleClass().contains("selection-panel-selected")) {
        root.getStyleClass().add("selection-panel-selected");
      }
    }
    else {
      root.getStyleClass().remove("selection-panel-selected");
    }

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

      imageView.setVisible(option.isEnabled());
      previewLabel.setVisible(!option.isEnabled());
      if (option.isEnabled()) {
        if (imageCached == null) {
          imageCached = new Image(client.getRestClient().getBaseUrl() + API + "recorder/preview/" + recordingScreen.getScreen().name());
        }
        imageView.setImage(imageCached);
      }
      else {
        previewLabel.setText("Recording not enabled");
        previewLabel.setStyle(WidgetFactory.MEDIA_CONTAINER_LABEL);
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    preview.managedProperty().bindBidirectional(preview.visibleProperty());
    previewLabel.managedProperty().bindBidirectional(previewLabel.visibleProperty());
    imageView.managedProperty().bindBidirectional(imageView.visibleProperty());
  }
}
