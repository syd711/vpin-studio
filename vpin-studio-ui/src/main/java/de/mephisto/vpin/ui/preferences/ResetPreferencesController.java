package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.util.Keys;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ResetPreferencesController implements Initializable {

  @FXML
  private ComboBox<String> resetKeyCombo;

  @FXML
  private void onKeySelection() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    resetKeyCombo.setItems(FXCollections.observableList(Keys.getKeyNames()));
  }
}
