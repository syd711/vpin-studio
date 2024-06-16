package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.validation.*;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class ValidatorsScreensPreferencesController implements Initializable {

  public static final String OPTIONS = "_Options";
  public static final String MEDIA = "_Media";
  @FXML
  private Parent preferenceList;
  private List<String> ignoreList;
  private Map<String, ComboBox> optionsCombos;
  private Map<String, ComboBox> mediaCombos;
  private ValidationSettings validationSettings;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    int code = getValidationCode(id);
    if (checked) {
      ignoreList.remove(String.valueOf(code));
    }
    else {
      if (ignoreList.contains(String.valueOf(code))) {
        return;
      }
      ignoreList.add(String.valueOf(code));
    }

    ComboBox optionCombo = optionsCombos.get(id + OPTIONS);
    optionCombo.setDisable(!checked);
    ComboBox mediaCombo = mediaCombos.get(id + MEDIA);
    mediaCombo.setDisable(!checked);

    String value = StringUtils.join(ignoreList, ",");
    Map<String, Object> prefs = new HashMap<>();
    prefs.put(PreferenceNames.IGNORED_VALIDATIONS, value);
    client.getPreferenceService().setPreferences(prefs);
    PreferencesController.markDirty(PreferenceType.validationSettings);
  }

  @FXML
  private void onComboChange(ActionEvent event) {
    ComboBox comboBox = (ComboBox) event.getSource();
    String id = comboBox.getId().substring(0, comboBox.getId().lastIndexOf("_"));
    int validationCode = getValidationCode(id);
    ComboBox<ValidatorMedia> mediaCombo = mediaCombos.get(id + MEDIA);
    ComboBox<ValidatorOption> optionCombo = optionsCombos.get(id + OPTIONS);

    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();
    ValidationConfig config = defaultProfile.getOrCreateConfig(validationCode);
    config.setMedia(mediaCombo.getValue());
    config.setOption(optionCombo.getValue());

    client.getPreferenceService().setJsonPreference(PreferenceNames.VALIDATION_SETTINGS, validationSettings);
    PreferencesController.markDirty(PreferenceType.validationSettings);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Frontend frontend = client.getFrontendService().getFrontend();

    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();
    optionsCombos = new HashMap<>();
    mediaCombos = new HashMap<>();

    findAllCheckboxes(parent, settingsCheckboxes);
    findAllMediaCombos(parent, mediaCombos);
    findAllOptionsCombos(parent, optionsCombos);

    PreferenceEntryRepresentation entry = client.getPreference(PreferenceNames.IGNORED_VALIDATIONS);
    validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);
    ValidationProfile defaultProfile = validationSettings.getDefaultProfile();

    ignoreList = entry.getCSVValue();
    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      int validationCode = getValidationCode(id);

      ComboBox optionCombo = optionsCombos.get(id + OPTIONS);
      ComboBox mediaCombo = mediaCombos.get(id + MEDIA);

      boolean ignored = frontend.getIgnoredValidations().contains(validationCode);
      if (ignored) {
        checkBox.setVisible(false);
        optionCombo.setVisible(false);
        mediaCombo.setVisible(false);
        continue;
      }

      checkBox.setSelected(!ignoreList.contains(String.valueOf(validationCode)));


      optionCombo.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(ValidatorOption.values()))));
      initOption(defaultProfile, optionCombo, validationCode);

      optionCombo.setDisable(!checkBox.isSelected());
      mediaCombo.setDisable(!checkBox.isSelected());

      mediaCombo.setItems(FXCollections.observableList(new ArrayList<>(Arrays.asList(ValidatorMedia.values()))));
      initMedia(defaultProfile, mediaCombo, validationCode);
    }
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

  private static void findAllCheckboxes(Parent parent, List<CheckBox> settingsCheckboxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) node;
        if (checkBox.getId() != null) {
          settingsCheckboxes.add(checkBox);
        }
      }
      if (node instanceof Parent) {
        findAllCheckboxes((Parent) node, settingsCheckboxes);
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
