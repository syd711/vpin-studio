package de.mephisto.vpin.ui.backups.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class BackupRestoreDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(BackupRestoreDialogController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Pane emuGrid;

  @FXML
  private CheckBox directb2sCheckBox;

  @FXML
  private CheckBox pupPackCheckBox;

  @FXML
  private CheckBox romCheckBox;

  @FXML
  private CheckBox nvRamCheckBox;

  @FXML
  private CheckBox resCheckBox;

  @FXML
  private CheckBox vbsCheckBox;

  @FXML
  private CheckBox popperMediaCheckBox;

  @FXML
  private CheckBox povCheckBox;

  @FXML
  private CheckBox iniCheckBox;

  @FXML
  private CheckBox musicCheckBox;

  @FXML
  private CheckBox altSoundCheckBox;

  @FXML
  private CheckBox altColorCheckBox;

  @FXML
  private CheckBox highscoreCheckBox;

  @FXML
  private CheckBox dmdCheckBox;

  @FXML
  private CheckBox vpxCheckBox;

  @FXML
  private CheckBox registryDataCheckBox;

  @FXML
  private CheckBox b2sSettingsCheckbox;

  @FXML
  private VBox frontendColumn;


  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private List<BackupDescriptorRepresentation> backupDescriptors;


  @FXML
  private void onImport(ActionEvent e) {
    ArchiveRestoreDescriptor restoreDescriptor = new ArchiveRestoreDescriptor();
    restoreDescriptor.setEmulatorId(emulatorCombo.getValue().getId());

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      Platform.runLater(() -> {
        try {
          for (BackupDescriptorRepresentation descriptor : this.backupDescriptors) {
            restoreDescriptor.setFilename(descriptor.getFilename());
            restoreDescriptor.setArchiveSourceId(descriptor.getSource().getId());
            client.getBackupService().restoreTable(restoreDescriptor);
          }
          JobPoller.getInstance().setPolling();
        }
        catch (Exception ex) {
          LOG.error("Failed to restore: " + ex.getMessage(), ex);
          WidgetFactory.showAlert(Studio.stage, "Restore Failed", "Failed to trigger import: " + ex.getMessage());
        }

      });
    }).start();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(List<BackupDescriptorRepresentation> archiveDescriptors) {
    this.backupDescriptors = archiveDescriptors;

    String title = "Restore " + this.backupDescriptors.size() + " Tables?";
    if (this.backupDescriptors.size() == 1) {
      title = "Restore \"" + this.backupDescriptors.get(0).getTableDetails().getGameDisplayName() + "\"?";
    }
    titleLabel.setText(title);

    BackupType backupType = client.getSystemService().getSystemSummary().getBackupType();
    emuGrid.setVisible(backupType.equals(BackupType.VPA));
  }

  private void refreshImportsSelection(BackupSettings backupSettings) {
    directb2sCheckBox.setSelected(backupSettings.isDirectb2s());
    pupPackCheckBox.setSelected(backupSettings.isPupPack());
    romCheckBox.setSelected(backupSettings.isRom());
    nvRamCheckBox.setSelected(backupSettings.isNvRam());
    resCheckBox.setSelected(backupSettings.isRes());
    vbsCheckBox.setSelected(backupSettings.isVbs());
    popperMediaCheckBox.setSelected(backupSettings.isFrontendMedia());
    povCheckBox.setSelected(backupSettings.isPov());
    iniCheckBox.setSelected(backupSettings.isIni());
    musicCheckBox.setSelected(backupSettings.isMusic());
    altSoundCheckBox.setSelected(backupSettings.isAltSound());
    altColorCheckBox.setSelected(backupSettings.isAltColor());
    highscoreCheckBox.setSelected(backupSettings.isHighscore());
    dmdCheckBox.setSelected(backupSettings.isDmd());
    vpxCheckBox.setSelected(backupSettings.isVpx());
    registryDataCheckBox.setSelected(backupSettings.isRegistryData());
    b2sSettingsCheckbox.setSelected(backupSettings.isB2sSettings());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameEmulatorRepresentation> emulators = client.getEmulatorService().getVpxGameEmulators();
    ObservableList<GameEmulatorRepresentation> data = FXCollections.observableList(emulators);
    this.emulatorCombo.setItems(data);
    this.emulatorCombo.setValue(data.get(0));


    frontendColumn.managedProperty().bindBidirectional(frontendColumn.visibleProperty());
    pupPackCheckBox.managedProperty().bindBidirectional(pupPackCheckBox.visibleProperty());
    popperMediaCheckBox.managedProperty().bindBidirectional(popperMediaCheckBox.visibleProperty());

    frontendColumn.setVisible(!Features.IS_STANDALONE);

    BackupSettings backupSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
    refreshImportsSelection(backupSettings);

    directb2sCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setDirectb2s(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    pupPackCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setPupPack(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    romCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setRom(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    nvRamCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setNvRam(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    resCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setRes(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    vbsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setVbs(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    popperMediaCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setFrontendMedia(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    povCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setPov(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    iniCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setIni(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    musicCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setMusic(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    altSoundCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setAltSound(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    altColorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setAltColor(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    highscoreCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setHighscore(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    dmdCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setDmd(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    vpxCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setVpx(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    registryDataCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setRegistryData(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
    b2sSettingsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      backupSettings.setB2sSettings(newValue);
      client.getPreferenceService().setJsonPreference(backupSettings);
    });
  }
}
