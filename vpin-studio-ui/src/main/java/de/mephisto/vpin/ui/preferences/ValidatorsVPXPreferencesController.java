package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

public class ValidatorsVPXPreferencesController implements Initializable {

  @FXML
  private Parent preferenceList;
  private List<String> ignoreList;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    String code = id.split("_")[1];
    if (checked) {
      ignoreList.remove(code);
    }
    else {
      if (ignoreList.contains(code)) {
        return;
      }
      ignoreList.add(code);
    }

    String value = StringUtils.join(ignoreList, ",");
    Map<String, Object> prefs = new HashMap<>();
    prefs.put(PreferenceNames.IGNORED_VALIDATIONS, value);
    Studio.client.getPreferenceService().setPreferences(prefs);

    PreferencesController.markDirty();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();
    findAllCheckboxes(parent, settingsCheckboxes);

    PreferenceEntryRepresentation entry = Studio.client.getPreference(PreferenceNames.IGNORED_VALIDATIONS);
    ignoreList = entry.getCSVValue();
    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      String validationCode = id.split("_")[1];

      boolean ignored = ignoreList.contains(validationCode);
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
