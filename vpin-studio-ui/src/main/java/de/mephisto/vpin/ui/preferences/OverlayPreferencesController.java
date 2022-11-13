package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.ui.util.BindingUtil;
import de.mephisto.vpin.ui.util.Keys;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OverlayPreferencesController implements Initializable {
  @FXML
  private ComboBox<String> overlayKeyCombo;

  @FXML
  private CheckBox showOverlayOnStartupCheckbox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<String> keyNames = Keys.getKeyNames();
    keyNames.add(0, "");
    overlayKeyCombo.setItems(FXCollections.observableList(keyNames));

    BindingUtil.bindCheckbox(showOverlayOnStartupCheckbox, PreferenceNames.SHOW_OVERLAY_ON_STARTUP, false);
    BindingUtil.bindComboBox(overlayKeyCombo, PreferenceNames.OVERLAY_KEY);
  }
}
