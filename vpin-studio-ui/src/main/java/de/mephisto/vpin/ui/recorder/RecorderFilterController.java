package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.TableOverviewPredicateFactory;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static de.mephisto.vpin.ui.Studio.client;

public class RecorderFilterController extends BaseFilterController<GameRepresentation, GameRepresentationModel>
    implements Initializable {

  @FXML
  private CheckBox missingAssetsCheckBox;

  @FXML
  private CheckBox withNVOffsetCheckBox;

  @FXML
  private CheckBox otherIssuesCheckbox;

  @FXML
  private CheckBox vpsUpdatesCheckBox;

  @FXML
  private CheckBox versionUpdatesCheckBox;

  @FXML
  private CheckBox notPlayedCheckBox;

  @FXML
  private CheckBox noHighscoreSettingsCheckBox;

  @FXML
  private CheckBox noHighscoreSupportCheckBox;

  @FXML
  private CheckBox noVpsMappingTableCheckBox;

  @FXML
  private CheckBox noVpsMappingVersionCheckBox;

  @FXML
  private CheckBox withBackglassCheckBox;

  @FXML
  private CheckBox withPupPackCheckBox;

  @FXML
  private CheckBox withAltSoundCheckBox;

  @FXML
  private CheckBox withAltColorCheckBox;

  @FXML
  private CheckBox withIniCheckBox;

  @FXML
  private CheckBox withPovCheckBox;

  @FXML
  private CheckBox withResCheckBox;

  @FXML
  private CheckBox withAliasCheckBox;

  @FXML
  private Node notPlayedSettings;

  @FXML
  private Node statusSettings;

  @FXML
  private Node configurationFilters;

  @FXML
  private Node tableAssetFilters;

  @FXML
  private Node configurationIssuesFilter;

  @FXML
  private Node vpsFilters;

  @FXML
  private ComboBox<TableStatus> statusCombo;

  @FXML
  private ComboBox<NoteType> notesCombo;

  private FilterSettings filterSettings;

  private TableOverviewPredicateFactory predicateFactory = new TableOverviewPredicateFactory();


  public void applyFilters() {
    // as we do not call filterGames() anymore, manually call saveFilterSettings to persist the reset
    client.getPreferenceService().setJsonPreference(PreferenceNames.FILTER_SETTINGS, filterSettings);
    super.applyFilters();
  }

  @Override
  protected boolean hasFilter() {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    return !filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator());
  }

  private GameEmulatorRepresentation getEmulatorSelection() {
    return ((RecorderController) tableController).getEmulatorSelection();
  }

  @Override
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, PlaylistRepresentation playlistRepresentation) {
    return predicateFactory.buildPredicate(searchTerm, playlistRepresentation, getEmulatorSelection(), filterSettings);
  }

  protected void resetFilters() {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    if (!filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator())) {
      this.filterSettings = new FilterSettings();

      statusCombo.setValue(null);
      notesCombo.setValue(null);
      missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
      otherIssuesCheckbox.setSelected(filterSettings.isOtherIssues());
      vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
      versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
      notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
      noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
      noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
      noVpsMappingTableCheckBox.setSelected(filterSettings.isNoVpsTableMapping());
      noVpsMappingVersionCheckBox.setSelected(filterSettings.isNoVpsVersionMapping());
      withBackglassCheckBox.setSelected(filterSettings.isWithBackglass());
      withPupPackCheckBox.setSelected(filterSettings.isWithPupPack());
      withAltSoundCheckBox.setSelected(filterSettings.isWithAltSound());
      withAltColorCheckBox.setSelected(filterSettings.isWithAltColor());
      withIniCheckBox.setSelected(filterSettings.isWithIni());
      withPovCheckBox.setSelected(filterSettings.isWithPov());
      withResCheckBox.setSelected(filterSettings.isWithRes());
      withNVOffsetCheckBox.setSelected(filterSettings.isWithNVOffset());
      withAliasCheckBox.setSelected(filterSettings.isWithAlias());
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    configurationFilters.managedProperty().bindBidirectional(configurationFilters.visibleProperty());
    tableAssetFilters.managedProperty().bindBidirectional(tableAssetFilters.visibleProperty());
    vpsFilters.managedProperty().bindBidirectional(vpsFilters.visibleProperty());
    configurationIssuesFilter.managedProperty().bindBidirectional(configurationIssuesFilter.visibleProperty());
    withPupPackCheckBox.managedProperty().bindBidirectional(withPupPackCheckBox.visibleProperty());
    missingAssetsCheckBox.managedProperty().bindBidirectional(missingAssetsCheckBox.visibleProperty());
    notPlayedSettings.managedProperty().bindBidirectional(notPlayedSettings.visibleProperty());
    statusSettings.managedProperty().bindBidirectional(statusSettings.visibleProperty());

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    withPupPackCheckBox.setVisible(frontendType.supportPupPacks());
    statusSettings.setVisible(!frontendType.equals(FrontendType.Standalone));
    notPlayedSettings.setVisible(frontendType.supportStatistics());
    missingAssetsCheckBox.setVisible(frontendType.supportMedias());

    filterSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.FILTER_SETTINGS, FilterSettings.class);
    missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
    missingAssetsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setMissingAssets(newValue);
      applyFilters();
    });
    otherIssuesCheckbox.setSelected(filterSettings.isOtherIssues());
    otherIssuesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setOtherIssues(newValue);
      applyFilters();
    });
    vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
    vpsUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVpsUpdates(newValue);
      applyFilters();
    });
    versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
    versionUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVersionUpdates(newValue);
      applyFilters();
    });
    notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
    notPlayedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNotPlayed(newValue);
      applyFilters();
    });
    noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
    noHighscoreSettingsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSettings(newValue);
      applyFilters();
    });
    noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
    noHighscoreSupportCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSupport(newValue);
      applyFilters();
    });
    noVpsMappingTableCheckBox.setSelected(filterSettings.isNoVpsTableMapping());
    noVpsMappingTableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoVpsTableMapping(newValue);
      applyFilters();
    });
    noVpsMappingVersionCheckBox.setSelected(filterSettings.isNoVpsVersionMapping());
    noVpsMappingVersionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoVpsVersionMapping(newValue);
      applyFilters();
    });
    withBackglassCheckBox.setSelected(filterSettings.isWithBackglass());
    withBackglassCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithBackglass(newValue);
      applyFilters();
    });
    withPupPackCheckBox.setSelected(filterSettings.isWithPupPack());
    withPupPackCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPupPack(newValue);
      applyFilters();
    });
    withAltSoundCheckBox.setSelected(filterSettings.isWithAltSound());
    withAltSoundCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltSound(newValue);
      applyFilters();
    });
    withAltColorCheckBox.setSelected(filterSettings.isWithAltColor());
    withAltColorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltColor(newValue);
      applyFilters();
    });
    withPovCheckBox.setSelected(filterSettings.isWithPov());
    withPovCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPov(newValue);
      applyFilters();
    });
    withResCheckBox.setSelected(filterSettings.isWithRes());
    withResCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithRes(newValue);
      applyFilters();
    });
    withIniCheckBox.setSelected(filterSettings.isWithIni());
    withIniCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithIni(newValue);
      applyFilters();
    });
    withNVOffsetCheckBox.setSelected(filterSettings.isWithNVOffset());
    withNVOffsetCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithNVOffset(newValue);
      applyFilters();
    });
    withAliasCheckBox.setSelected(filterSettings.isWithAlias());
    withAliasCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAlias(newValue);
      applyFilters();
    });

    List<TableStatus> statuses = new ArrayList<>(TableDataController.supportedStatuses(frontendType));
    statuses.add(0, null);
    statusCombo.setItems(FXCollections.observableList(statuses));
    if (filterSettings.getGameStatus() >= 0) {
      statusCombo.setValue(statuses.get(filterSettings.getGameStatus()));
    }
    statusCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setGameStatus(-1);
      }
      else {
        filterSettings.setGameStatus(newValue.getValue());
      }
      applyFilters();
    });

    List<NoteType> noteTypes = new ArrayList<>(Arrays.asList(NoteType.values()));
    noteTypes.add(0, null);
    notesCombo.setItems(FXCollections.observableList(noteTypes));
    notesCombo.setValue(filterSettings.getNoteType());
    notesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setNoteType(null);
      }
      else {
        filterSettings.setNoteType(newValue);
      }
      applyFilters();
    });
  }

  public void setEmulator(GameEmulatorRepresentation value) {
    filterSettings.setEmulatorId(value != null ? value.getId() : -1);

    boolean vpxMode = value == null || value.isVpxEmulator();
    configurationFilters.setVisible(vpxMode);
    tableAssetFilters.setVisible(vpxMode);
    vpsFilters.setVisible(vpxMode);
    configurationIssuesFilter.setVisible(vpxMode);
  }
}