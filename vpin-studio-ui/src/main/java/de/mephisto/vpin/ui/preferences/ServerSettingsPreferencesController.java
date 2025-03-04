package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class ServerSettingsPreferencesController implements Initializable {

  @FXML
  private Label startupTimeLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label systemIdLabel;

  @FXML
  private CheckBox useOriginalVbsFilesCheckbox;

  @FXML
  private CheckBox keepModificationDateCheckbox;

  @FXML
  private CheckBox vpxMonitoringCheckbox;

  @FXML
  private CheckBox uploadTableBackups;

  @FXML
  private CheckBox launchFrontendCheckbox;

  @FXML
  private Node launchOnExitOption;

  @FXML
  private VBox vpxMonitorSettings;

  @FXML
  private ComboBox<String> mappingHsFileNameCombo;

  @FXML
  private ComboBox<String> mappingVpsTableIdCombo;

  @FXML
  private ComboBox<String> mappingVpsVersionIdCombo;

  @FXML
  private ComboBox<String> patchVersionCombo;

  @FXML
  private VBox popperDataMappingFields;

  @FXML
  private void onCopySystemId() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(systemIdLabel.getText());
    clipboard.setContent(content);
  }

  @FXML
  private void onMediaIndex() {
    Optional<ButtonType> result = WidgetFactory.showAlertOption(Studio.stage, "Media Cache", "Cancel", "Regenerate Media Cache", "Regenerate the media cache?", null);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      Platform.runLater(() -> {
        ProgressDialog.createProgressDialog(new RegenerateMediaCacheProgressModel(client.getGameService().getVpxGamesCached()));
      });
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
    vpxMonitorSettings.managedProperty().bindBidirectional(vpxMonitorSettings.visibleProperty());
    launchOnExitOption.managedProperty().bindBidirectional(launchOnExitOption.visibleProperty());
    vpxMonitorSettings.setVisible(Features.VPX_MONITORING);

    popperDataMappingFields.managedProperty().bindBidirectional(popperDataMappingFields.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendType frontendType = frontend.getFrontendType();
    popperDataMappingFields.setVisible(frontendType.supportExtendedFields());
    launchOnExitOption.setVisible(frontendType.supportMedias());
    launchFrontendCheckbox.setText("Launch " + frontend.getName() +  " on maintenance exit.");

    Date startupTime = client.getSystemService().getStartupTime();
    startupTimeLabel.setText(DateFormat.getDateTimeInstance().format(startupTime));
    versionLabel.setText(client.getSystemService().getVersion());
    systemIdLabel.setText(client.getSystemService().getSystemId().getSystemId());

    ServerSettings serverSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    List<String> hsFileNameMappingFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingHsFileNameCombo.setItems(FXCollections.observableList(hsFileNameMappingFields));
    mappingHsFileNameCombo.setValue(serverSettings.getMappingHsFileName());
    mappingHsFileNameCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingHsFileName(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    List<String> tableIdFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingVpsTableIdCombo.setItems(FXCollections.observableList(tableIdFields));
    mappingVpsTableIdCombo.setValue(serverSettings.getMappingVpsTableId());
    mappingVpsTableIdCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingVpsTableId(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    List<String> vpsTableVersionFields = Arrays.asList("WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    mappingVpsVersionIdCombo.setItems(FXCollections.observableList(vpsTableVersionFields));
    mappingVpsVersionIdCombo.setValue(serverSettings.getMappingVpsTableVersionId());
    mappingVpsVersionIdCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingVpsTableVersionId(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    List<String> patchVersionFields = Arrays.asList(null, "WEBGameID", "CUSTOM2", "CUSTOM3", "CUSTOM4", "CUSTOM5");
    patchVersionCombo.setItems(FXCollections.observableList(patchVersionFields));
    patchVersionCombo.setValue(serverSettings.getMappingPatchVersion());
    patchVersionCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setMappingPatchVersion(newValue);
      PreferencesController.markDirty(PreferenceType.serverSettings);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    launchFrontendCheckbox.setSelected(serverSettings.isLaunchPopperOnExit());
    launchFrontendCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setLaunchPopperOnExit(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    useOriginalVbsFilesCheckbox.setSelected(serverSettings.isKeepVbsFiles());
    useOriginalVbsFilesCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setKeepVbsFiles(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    keepModificationDateCheckbox.setSelected(serverSettings.isKeepModificationDate());
    keepModificationDateCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setKeepModificationDate(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    vpxMonitoringCheckbox.setSelected(serverSettings.isUseVPXTableMonitor());
    vpxMonitoringCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setUseVPXTableMonitor(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });

    uploadTableBackups.setSelected(serverSettings.isBackupTableOnOverwrite());
    uploadTableBackups.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      serverSettings.setBackupTableOnOverwrite(t1);
      client.getPreferenceService().setJsonPreference(serverSettings);
    });
  }
}
