package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.util.tags.TagField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class ValidatorsVPXPreferencesController implements Initializable {

  @FXML
  private Parent preferenceList;

  @FXML
  private VBox pupPackValidator;

  @FXML
  private Pane tagPane;

  @FXML
  private CheckBox taggingEnabledCheckbox;

  private IgnoredValidationSettings ignoredValidationSettings;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    String code = id.split("_")[1];

    ignoredValidationSettings.getIgnoredValidators().put(code, !checked);
    client.getPreferenceService().setJsonPreference(ignoredValidationSettings);

    PreferencesController.markDirty(PreferenceType.serverSettings);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pupPackValidator.managedProperty().bindBidirectional(pupPackValidator.visibleProperty());

    ignoredValidationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);

    pupPackValidator.setVisible(Features.PUPPACKS_ENABLED);

    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();
    findAllCheckboxes(parent, settingsCheckboxes);
    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      if (!id.contains("_")) {
        continue;
      }

      String validationCode = id.split("_")[1];

      if (!Features.SCREEN_VALIDATOR) {
        if (validationCode.equals(String.valueOf(GameValidationCode.CODE_SCREEN_SIZE_ISSUE))) {
          checkBox.managedProperty().bindBidirectional(checkBox.visibleProperty());
          checkBox.setVisible(false);
        }
      }

      boolean ignored = ignoredValidationSettings.isIgnored(validationCode);
      checkBox.setSelected(!ignored);
    }


    TaggingSettings taggingSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.TAGGING_SETTINGS, TaggingSettings.class);
    List<String> suggestions = client.getTaggingService().getTags();
    TagField tagField = new TagField(suggestions);
    tagField.setInputDisabled(!taggingSettings.isAutoTagTablesEnabled());
    tagField.setAllowCustomTags(true);
    tagField.setTags(taggingSettings.getTableTags());
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        List<String> list = (List<String>) c.getList();
        taggingSettings.setTableTags(new ArrayList<>(list));
        client.getPreferenceService().setJsonPreference(taggingSettings);
      }
    });
    tagPane.getChildren().add(tagField);

    taggingEnabledCheckbox.setSelected(taggingSettings.isAutoTagTablesEnabled());
    taggingEnabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        taggingSettings.setAutoTagTablesEnabled(newValue);
        tagField.setInputDisabled(!newValue);
        client.getPreferenceService().setJsonPreference(taggingSettings);
      }
    });
  }

  private static void findAllCheckboxes(Parent parent, List<CheckBox> settingsCheckboxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) node;
        if (checkBox.getId() != null) {
          settingsCheckboxes.add(checkBox);
        }
      }
      if (node instanceof Parent)
        findAllCheckboxes((Parent) node, settingsCheckboxes);
    }
  }
}
