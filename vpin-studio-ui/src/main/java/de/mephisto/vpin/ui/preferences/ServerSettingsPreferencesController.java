package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.BindingUtil.debouncer;

public class ServerSettingsPreferencesController implements Initializable {

  @FXML
  private Label startupTimeLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private CheckBox serviceStartupCheckbox;

  @FXML
  private Spinner<Integer> idleSpinner;

  @FXML
  private CheckBox autoApplyVPSCheckbox;

  @FXML
  private CheckBox keepNamesCheckbox;

  @FXML
  private CheckBox keepDisplayNamesCheckbox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Date startupTime = client.getSystemService().getStartupTime();
    startupTimeLabel.setText(DateFormat.getDateTimeInstance().format(startupTime));
    versionLabel.setText(client.getSystemService().getVersion());

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


    PreferenceEntryRepresentation serverPreferences = client.getPreference(PreferenceNames.SERVER_SETTINGS);
    List<String> serverSettings = serverPreferences.getCSVValue();

    autoApplyVPSCheckbox.setSelected(serverSettings.contains(PreferenceNames.SERVER_AUTO_APPLY_VPS_TO_POPPER));
    autoApplyVPSCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!t1) {
        serverSettings.remove(PreferenceNames.SERVER_AUTO_APPLY_VPS_TO_POPPER);
      }
      else if (!serverSettings.contains(PreferenceNames.SERVER_AUTO_APPLY_VPS_TO_POPPER)) {
        serverSettings.add(PreferenceNames.SERVER_AUTO_APPLY_VPS_TO_POPPER);
      }
      client.getPreferenceService().setPreference(PreferenceNames.SERVER_SETTINGS, String.join(",", serverSettings));
    });


    keepNamesCheckbox.setSelected(serverSettings.contains(PreferenceNames.SERVER_KEEP_EXISTING_VXP_FILENAMES));
    keepNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!t1) {
        serverSettings.remove(PreferenceNames.SERVER_KEEP_EXISTING_VXP_FILENAMES);
      }
      else if (!serverSettings.contains(PreferenceNames.SERVER_KEEP_EXISTING_VXP_FILENAMES)) {
        serverSettings.add(PreferenceNames.SERVER_KEEP_EXISTING_VXP_FILENAMES);
      }
      client.getPreferenceService().setPreference(PreferenceNames.SERVER_SETTINGS, String.join(",", serverSettings));
    });

    keepDisplayNamesCheckbox.setSelected(serverSettings.contains(PreferenceNames.SERVER_KEEP_EXISTING_DISPLAY_NAMES));
    keepDisplayNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!t1) {
        serverSettings.remove(PreferenceNames.SERVER_KEEP_EXISTING_DISPLAY_NAMES);
      }
      else if (!serverSettings.contains(PreferenceNames.SERVER_KEEP_EXISTING_DISPLAY_NAMES)) {
        serverSettings.add(PreferenceNames.SERVER_KEEP_EXISTING_DISPLAY_NAMES);
      }
      client.getPreferenceService().setPreference(PreferenceNames.SERVER_SETTINGS, String.join(",", serverSettings));
    });
  }
}
