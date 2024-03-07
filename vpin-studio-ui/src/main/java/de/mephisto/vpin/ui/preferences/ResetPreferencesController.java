package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import de.mephisto.vpin.ui.util.Keys;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResetPreferencesController implements Initializable {

  @FXML
  private ComboBox<String> resetKeyCombo;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    resetKeyCombo.setItems(FXCollections.observableList(keyNames));

    PreferenceBindingUtil.bindComboBox(resetKeyCombo, PreferenceNames.RESET_KEY);
  }
}
