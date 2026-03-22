package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.VRSettings;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class VRPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(VRPreferencesController.class);

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private void onButtonRecord() {
    PreferencesDialogs.openButtonRecorder();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    VRSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.VR_SETTINGS, VRSettings.class);

    enabledCheckbox.setSelected(settings.isEnabled());
    enabledCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        settings.setEnabled(newValue);
        client.getPreferenceService().setJsonPreference(settings);
      }
    });
  }
}
