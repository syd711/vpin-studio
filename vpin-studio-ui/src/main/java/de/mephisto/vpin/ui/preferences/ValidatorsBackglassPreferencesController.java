package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ValidatorsBackglassPreferencesController implements Initializable {

  @FXML
  private Parent preferenceList;

  private IgnoredValidationSettings ignoredValidationSettings;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    String code = id.split("_")[1];
    boolean checked = checkBox.isSelected();

    ignoredValidationSettings.getIgnoredValidators().put(code, !checked);
    client.getPreferenceService().setJsonPreference(ignoredValidationSettings);

    PreferencesController.markDirty(PreferenceType.serverSettings);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    ignoredValidationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class);

    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();
    findAllCheckboxes(parent, settingsCheckboxes);
    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      String validationCode = id.split("_")[1];

      boolean ignored = ignoredValidationSettings.isIgnored(validationCode);
      checkBox.setSelected(!ignored);
    }
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
