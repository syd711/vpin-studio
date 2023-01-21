package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.ui.util.BindingUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class VPinNamePreferencesController implements Initializable {

  @FXML
  private TextField vpinNameText;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BindingUtil.bindTextField(vpinNameText, PreferenceNames.SYSTEM_NAME, UIDefaults.VPIN_NAME);
  }
}
