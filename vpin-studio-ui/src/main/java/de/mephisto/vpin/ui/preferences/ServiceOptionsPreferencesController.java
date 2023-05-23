package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class ServiceOptionsPreferencesController implements Initializable {

  @FXML
  private CheckBox serviceStartupCheckbox;

  @FXML
  private Spinner<Integer> idleSpinner;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    serviceStartupCheckbox.setSelected(client.getSystemService().autostartInstalled());
    serviceStartupCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue) {
        client.getSystemService().autostartInstall();
      }
      else {
        client.getSystemService().autostartUninstall();
      }
    });

    PreferenceEntryRepresentation idle = OverlayWindowFX.client.getPreference(PreferenceNames.IDLE_TIMEOUT);
    int timeout = idle.getIntValue();
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, timeout);
    idleSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      client.getPreferenceService().setPreference(PreferenceNames.IDLE_TIMEOUT, String.valueOf(value1));
    }, 1000));
  }
}
