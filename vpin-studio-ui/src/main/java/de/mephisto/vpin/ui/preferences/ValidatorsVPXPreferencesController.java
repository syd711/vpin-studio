package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ValidatorsVPXPreferencesController implements Initializable {

  @FXML
  private Parent preferenceList;

  @FXML
  private VBox pupPackValidator;

  private IgnoredValidationSettings ignoredValidationSettings;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    String code = id.split("_")[1];

    ignoredValidationSettings.getIgnoredValidators().put(code, !checked);
    client.getPreferenceService().setJsonPreference(PreferenceNames.IGNORED_VALIDATIONS, ignoredValidationSettings);

    PreferencesController.markDirty(PreferenceType.serverSettings);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    pupPackValidator.managedProperty().bindBidirectional(pupPackValidator.visibleProperty());

    ignoredValidationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.IGNORED_VALIDATIONS, IgnoredValidationSettings.class);

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    pupPackValidator.setVisible(frontendType.supportPupPacks());

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
