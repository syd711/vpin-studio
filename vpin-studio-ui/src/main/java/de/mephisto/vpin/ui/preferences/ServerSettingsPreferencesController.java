package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.PreferenceBindingUtil.debouncer;

public class ServerSettingsPreferencesController implements Initializable {

  @FXML
  private Label startupTimeLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private CheckBox serviceStartupCheckbox;

  @FXML
  private CheckBox useOriginalVbsFilesCheckbox;

  @FXML
  private CheckBox vpxMonitoringCheckbox;

  @FXML
  private Spinner<Integer> idleSpinner;

  @FXML
  private CheckBox launchPopperCheckbox;

  @FXML
  private Button shutdownBtn;

  @FXML
  private Button restartBtn;

  @FXML
  private ComboBox<String> mappingHsFileNameCombo;

  @FXML
  private ComboBox<String> mappingVpsTableIdCombo;

  @FXML
  private ComboBox<String> mappingVpsVersionIdCombo;

  @FXML
  private void onShutdown() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Remote System Shutdown", "Cancel", "Shutdown System", "Are you sure you want to shutdown the remote system?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().systemShutdown();
      WidgetFactory.showInformation(Studio.stage, "Remote System Shutdown", "The remote system will shutdown in less than a minute.");
    }
  }

  @FXML
  private void onRestart() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Server Restart", "Cancel", "Restart Server", "Are you sure you want to restart the VPin Studio Server?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getSystemService().restart();
      ProgressDialog.createProgressDialog(new RestartProgressModel());
    }
  }

  @FXML
  private void onBackup() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Backup Database", "This creates a copy with timestamp of the VPin Studio Servers database.", null, "Backup Database");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      String dbFile = client.getSystemService().backup();
      if (dbFile != null) {
        WidgetFactory.showInformation(Studio.stage, "Backup Database", "Created database backup \"" + dbFile + "\".");
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    shutdownBtn.setDisable(client.getSystemService().isLocal());

    Date startupTime = client.getSystemService().getStartupTime();
    int dbVersion = client.getPinUPPopperService().getVersion();

    startupTimeLabel.setText(DateFormat.getDateTimeInstance().format(startupTime));
    versionLabel.setText(client.getSystemService().getVersion());

    serviceStartupCheckbox.setSelected(client.getSystemService().autostartInstalled());
    serviceStartupCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        client.getSystemService().autostartInstall();
      }
      else {
        client.getSystemService().autostartUninstall();
      }
    });

    PreferenceEntryRepresentation idle = ServerFX.client.getPreference(PreferenceNames.IDLE_TIMEOUT);
    int timeout = idle.getIntValue();
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 60, timeout);
    idleSpinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(PreferenceNames.IDLE_TIMEOUT, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      client.getPreferenceService().setPreference(PreferenceNames.IDLE_TIMEOUT, String.valueOf(value1));
    }, 1000));


    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    List<String> hsFileNameMappingFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingHsFileNameCombo.setItems(FXCollections.observableList(hsFileNameMappingFields));
    mappingHsFileNameCombo.setValue(serverSettings.getMappingHsFileName());
    mappingHsFileNameCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingHsFileName(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    List<String> tableIdFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingVpsTableIdCombo.setItems(FXCollections.observableList(tableIdFields));
    mappingVpsTableIdCombo.setValue(serverSettings.getMappingVpsTableId());
    mappingVpsTableIdCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingVpsTableId(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    List<String> vpsTableVersionFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingVpsVersionIdCombo.setItems(FXCollections.observableList(vpsTableVersionFields));
    mappingVpsVersionIdCombo.setValue(serverSettings.getMappingVpsTableVersionId());
    mappingVpsVersionIdCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingVpsTableVersionId(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    launchPopperCheckbox.setSelected(serverSettings.isLaunchPopperOnExit());
    launchPopperCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setLaunchPopperOnExit(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    useOriginalVbsFilesCheckbox.setSelected(serverSettings.isKeepVbsFiles());
    useOriginalVbsFilesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setKeepVbsFiles(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });

    vpxMonitoringCheckbox.setSelected(serverSettings.isUseVPXTableMonitor());
    vpxMonitoringCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setUseVPXTableMonitor(t1);
      client.getPreferenceService().setJsonPreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
    });
  }
}
