package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;
import static de.mephisto.vpin.ui.Studio.client;

public class ValidatorsScreensPreferencesController implements Initializable {

  public static final String OPTIONS = "_Options";
  public static final String MEDIA = "_Media";

  private Map<String, ComboBox> optionsCombos = new HashMap<>();
  private Map<String, ComboBox> mediaCombos = new HashMap<>();
  private Map<VPinScreen, CheckBox> taggingCheckboxes = new HashMap<>();

  private ValidationSettings validationSettings;
  private TaggingSettings taggingSettings;
  private IgnoredValidationSettings ignoredValidationSettings;
  private RecorderSettings recorderSettings;

  @FXML
  private Parent preferenceList;

  @FXML
  private Pane tagPane;

  @FXML
  private CheckBox taggingEnabledCheckbox;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    int code = getValidationCode(id);
    VPinScreen screen = getScreenForCode(code);

    if (screen != null) {
      RecordingScreenOptions recordingScreenOption = recorderSettings.getRecordingScreenOption(screen);
      if (recordingScreenOption != null) {
        recordingScreenOption.setEnabled(checked);
        client.getPreferenceService().setJsonPreference(recorderSettings);
      }
    }

    ComboBox optionCombo = optionsCombos.get(id + OPTIONS);
    optionCombo.setDisable(!checked);
    ComboBox mediaCombo = mediaCombos.get(id + MEDIA);
    mediaCombo.setDisable(!checked);
    CheckBox taggingCheckbox = taggingCheckboxes.get(screen);
    taggingCheckbox.setDisable(!checked);

    ignoredValidationSettings.getIgnoredValidators().put(String.valueOf(code), !checked);
    client.getPreferenceService().setJsonPreference(ignoredValidationSettings);

    PreferencesController.markDirty(PreferenceType.validationSettings);
  }

  @FXML
  private void onTaggingPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    int code = getValidationCode(id);
    VPinScreen screen = getScreenForCode(code);

    if (checked) {
      taggingSettings.getTaggedScreens().add(screen);
    }
    else {
      taggingSettings.getTaggedScreens().remove(screen);
    }

    client.getPreferenceService().setJsonPreference(taggingSettings);
  }

  private static VPinScreen getScreenForCode(int code) {
    switch (code) {
      case CODE_NO_AUDIO: {
        return VPinScreen.Audio;
      }
      case CODE_NO_AUDIO_LAUNCH: {
        return VPinScreen.AudioLaunch;
      }
      case CODE_NO_APRON: {
        return VPinScreen.Menu;
      }
      case CODE_NO_INFO: {
        return VPinScreen.GameInfo;
      }
      case CODE_NO_HELP: {
        return VPinScreen.GameHelp;
      }
      case CODE_NO_TOPPER: {
        return VPinScreen.Topper;
      }
      case CODE_NO_BACKGLASS: {
        return VPinScreen.BackGlass;
      }
      case CODE_NO_DMD: {
        return VPinScreen.DMD;
      }
      case CODE_NO_PLAYFIELD: {
        return VPinScreen.PlayField;
      }
      case CODE_NO_LOADING: {
        return VPinScreen.Loading;
      }
      case CODE_NO_OTHER2: {
        return VPinScreen.Other2;
      }
      case CODE_NO_LOGO: {
        return VPinScreen.Logo;
      }
      case CODE_NO_WHEEL_IMAGE: {
        return VPinScreen.Wheel;
      }
    }
    return null;
  }

  @FXML
  private void onComboChange(ActionEvent event) {
    ComboBox comboBox = (ComboBox) event.getSource();

    JFXFuture.runAsync(() -> {
      String id = comboBox.getId().substring(0, comboBox.getId().lastIndexOf("_"));
      int validationCode = getValidationCode(id);
      ComboBox<ValidatorMedia> mediaCombo = mediaCombos.get(id + MEDIA);
      ComboBox<ValidatorOption> optionCombo = optionsCombos.get(id + OPTIONS);

      ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
      ValidationConfig config = defaultProfile.getOrCreateConfig(validationCode);
      config.setMedia(mediaCombo.getValue());
      config.setOption(optionCombo.getValue());

      client.getPreferenceService().setJsonPreference(validationSettings);
    }).thenLater(() -> {
      PreferencesController.markDirty(PreferenceType.validationSettings);
    });
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ignoredValidationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);
    recorderSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.RECORDER_SETTINGS, RecorderSettings.class);

    Frontend frontend = client.getFrontendService().getFrontendCached();

    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();

    findAllScreenCheckboxes(parent, settingsCheckboxes);
    findAllTaggingCheckboxes(parent, taggingCheckboxes);
    findAllMediaCombos(parent, mediaCombos);
    findAllOptionsCombos(parent, optionsCombos);

    validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    taggingSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.TAGGING_SETTINGS, TaggingSettings.class);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();

    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      if (!id.contains("_")) {
        continue;
      }

      int validationCode = getValidationCode(id);
      VPinScreen screenForCode = getScreenForCode(validationCode);

      ComboBox optionCombo = optionsCombos.get(id + OPTIONS);
      ComboBox mediaCombo = mediaCombos.get(id + MEDIA);
      CheckBox taggingCheckbox = taggingCheckboxes.get(screenForCode);

      boolean ignored = frontend.getIgnoredValidations().contains(validationCode);
      if (ignored) {
        checkBox.setVisible(false);
        optionCombo.setVisible(false);
        mediaCombo.setVisible(false);
        continue;
      }

      checkBox.setSelected(!ignoredValidationSettings.isIgnored(String.valueOf(validationCode)));


      optionCombo.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(ValidatorOption.values()))));
      initOption(defaultProfile, optionCombo, validationCode);

      optionCombo.setDisable(!checkBox.isSelected());
      mediaCombo.setDisable(!checkBox.isSelected());
      taggingCheckbox.setDisable(!checkBox.isSelected());

      mediaCombo.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(ValidatorMedia.values()))));
      initMedia(defaultProfile, mediaCombo, validationCode);
    }

    List<String> suggestions = client.getTaggingService().getTags();
    TagField tagField = new TagField(suggestions);
    tagField.setInputDisabled(!taggingSettings.isAutoTagScreensEnabled());
    tagField.setAllowCustomTags(true);
    tagField.setTags(taggingSettings.getScreenTags());
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        List<String> list = (List<String>) c.getList();
        taggingSettings.setScreenTags(new ArrayList<>(list));
        client.getPreferenceService().setJsonPreference(taggingSettings);
      }
    });
    tagPane.getChildren().add(tagField);

    for (VPinScreen taggedScreen : taggingSettings.getTaggedScreens()) {
      if (taggingCheckboxes.containsKey(taggedScreen)) {
        CheckBox checkBox = taggingCheckboxes.get(taggedScreen);
        checkBox.setSelected(true);
      }
    }

    taggingEnabledCheckbox.setSelected(taggingSettings.isAutoTagScreensEnabled());
    taggingEnabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        taggingSettings.setAutoTagScreensEnabled(newValue);
        tagField.setInputDisabled(!newValue);
        client.getPreferenceService().setJsonPreference(taggingSettings);
      }
    });
  }

  private static int getValidationCode(String id) {
    String validationCode = id.split("_")[1];
    return Integer.parseInt(validationCode);
  }

  private void initMedia(ValidationProfile defaultProfile, ComboBox<ValidatorMedia> mediaCombo, int id) {
    ValidationConfig config = defaultProfile.getOrCreateConfig(id);
    mediaCombo.setValue(config.getMedia());
  }

  private void initOption(ValidationProfile defaultProfile, ComboBox<ValidatorOption> optionCombo, int id) {
    ValidationConfig config = defaultProfile.getOrCreateConfig(id);
    optionCombo.setValue(config.getOption());
  }

  private static void findAllScreenCheckboxes(Parent parent, List<CheckBox> settingsCheckboxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) node;
        if (checkBox.getId() != null && !checkBox.getId().startsWith("Tag")) {
          settingsCheckboxes.add(checkBox);
        }
      }
      if (node instanceof Parent) {
        findAllScreenCheckboxes((Parent) node, settingsCheckboxes);
      }
    }
  }

  private static void findAllTaggingCheckboxes(Parent parent, Map<VPinScreen, CheckBox> taggingCheckboxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) node;
        if (checkBox.getId() != null && checkBox.getId().startsWith("Tag")) {
          int validationCode = getValidationCode(checkBox.getId());
          VPinScreen screenForCode = getScreenForCode(validationCode);
          taggingCheckboxes.put(screenForCode, checkBox);
        }
      }
      if (node instanceof Parent) {
        findAllTaggingCheckboxes((Parent) node, taggingCheckboxes);
      }
    }
  }

  private static void findAllOptionsCombos(Parent parent, Map<String, ComboBox> optionComboBoxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof ComboBox) {
        ComboBox comboBox = (ComboBox) node;
        if (comboBox.getId() != null && comboBox.getId().contains("Options")) {
          optionComboBoxes.put(comboBox.getId(), comboBox);
        }
      }
      if (node instanceof Parent) {
        findAllOptionsCombos((Parent) node, optionComboBoxes);
      }
    }
  }

  private static void findAllMediaCombos(Parent parent, Map<String, ComboBox> mediaComboBoxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof ComboBox) {
        ComboBox comboBox = (ComboBox) node;
        if (comboBox.getId() != null && comboBox.getId().contains("Media")) {
          mediaComboBoxes.put(comboBox.getId(), comboBox);
        }
      }
      if (node instanceof Parent) {
        findAllMediaCombos((Parent) node, mediaComboBoxes);
      }
    }
  }
}
