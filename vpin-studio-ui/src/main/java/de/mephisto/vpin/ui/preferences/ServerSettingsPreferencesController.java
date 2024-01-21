package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

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

  @FXML
  private CheckBox launchPopperCheckbox;

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


    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    autoApplyVPSCheckbox.setSelected(serverSettings.isVpsAutoApplyToPopper());
    autoApplyVPSCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setVpsAutoApplyToPopper(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });


    keepNamesCheckbox.setSelected(serverSettings.isVpxKeepFileNames());
    keepNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setVpxKeepFileNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    keepDisplayNamesCheckbox.setSelected(serverSettings.isVpxKeepDisplayNames());
    keepDisplayNamesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setVpxKeepDisplayNames(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    launchPopperCheckbox.setSelected(serverSettings.isLaunchPopperOnExit());
    launchPopperCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setLaunchPopperOnExit(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });
  }
}
