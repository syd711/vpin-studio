package de.mephisto.vpin.ui.backups.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.BackupExportDescriptor;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TablesBackupDialogController implements Initializable, DialogController {

  @FXML
  private Label titleLabel;

  @FXML
  private CheckBox removeFromPlaylistCheckbox;


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
  private VBox frontendColumn;

  @FXML
  private ComboBox<BackupSourceRepresentation> sourceCombo;


  private List<GameRepresentation> games;

  @FXML
  private void onExportClick(ActionEvent e) throws Exception {
    BackupSourceRepresentation source = sourceCombo.getValue();

    BackupExportDescriptor descriptor = new BackupExportDescriptor();
    descriptor.setBackupSourceId(source.getId());
    descriptor.setRemoveFromPlaylists(removeFromPlaylistCheckbox.isSelected());
    descriptor.getGameIds().addAll(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));
    Studio.client.getArchiveService().backupTable(descriptor);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        //ignore
      }
      Platform.runLater(() -> {
        JobPoller.getInstance().setPolling();
      });
    }).start();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
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
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    frontendColumn.managedProperty().bindBidirectional(frontendColumn.visibleProperty());
    pupPackCheckBox.managedProperty().bindBidirectional(pupPackCheckBox.visibleProperty());
    popperMediaCheckBox.managedProperty().bindBidirectional(popperMediaCheckBox.visibleProperty());

    List<BackupSourceRepresentation> repositories = new ArrayList<>(client.getArchiveService().getBackupSources());
    sourceCombo.setItems(FXCollections.observableList(repositories));
    sourceCombo.getSelectionModel().select(0);

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
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    if(games.size() == 1) {
      this.titleLabel.setText(games.get(0).getGameDisplayName());
    }
    else {
      this.titleLabel.setText("Backup of " + games.size() + " tables");
    }
  }
}
