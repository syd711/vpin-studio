package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

public class ValidationsPreferencesController implements Initializable {

  public static final String IGNORED_VALIDATIONS = "ignoredValidations";
  private VPinStudioClient client;

  @FXML
  private Parent preferenceList;

  @FXML
  private void onPreferenceChange(ActionEvent event) {
    CheckBox checkBox = (CheckBox) event.getSource();
    String id = checkBox.getId();
    boolean checked = checkBox.isSelected();
    String code = id.split("_")[1];


    PreferenceEntryRepresentation entry = client.getPreference(IGNORED_VALIDATIONS);
    String ignoredValidations = entry.getValue();
    if (ignoredValidations == null) {
      ignoredValidations = "";
    }

    List<String> ignoreList = new ArrayList<>(Arrays.asList(ignoredValidations.split(",")));
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
    prefs.put("ignoredValidations", value);
    client.setPreferences(prefs);
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();

    Parent parent = preferenceList;
    List<CheckBox> settingsCheckboxes = new ArrayList<>();
    findAllCheckboxes(parent, settingsCheckboxes);

    PreferenceEntryRepresentation entry = client.getPreference(IGNORED_VALIDATIONS);
    String ignoredValidations = entry.getValue();
    if (ignoredValidations == null) {
      ignoredValidations = "";
    }

    List<String> ignoreList = new ArrayList<>(Arrays.asList(ignoredValidations.split(",")));
    for (CheckBox checkBox : settingsCheckboxes) {
      String id = checkBox.getId();
      String code = id.split("_")[1];
      checkBox.setSelected(!ignoreList.contains(code));
    }
  }

  private static void findAllCheckboxes(Parent parent, List<CheckBox> settingsCheckboxes) {
    for (Node node : parent.getChildrenUnmodifiable()) {
      if (node instanceof CheckBox) {
        CheckBox checkBox = (CheckBox) node;
        if (checkBox.getId() != null && checkBox.getId().startsWith("pref_")) {
          settingsCheckboxes.add(checkBox);
        }
      }
      if (node instanceof Parent)
        findAllCheckboxes((Parent) node, settingsCheckboxes);
    }
  }
}
