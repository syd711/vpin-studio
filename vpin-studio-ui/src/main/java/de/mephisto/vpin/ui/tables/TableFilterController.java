package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.NoteType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TableFilterController extends BaseFilterController implements Initializable {

  @FXML
  private VBox filterPanel;

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
  private CheckBox withPovIniCheckBox;

  @FXML
  private CheckBox withAliasCheckBox;

  @FXML
  private VBox filterRoot;

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

  private boolean updatesDisabled = false;
  private FilterSettings filterSettings;
  private TableOverviewController tableOverviewController;


  @FXML
  private void onReset() {
    GameEmulatorRepresentation emulatorSelection = tableOverviewController.getEmulatorSelection();
    if (!filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator())) {
      updateSettings(new FilterSettings());
      applyFilter();
    }
  }

  public void setTableController(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
    super.setupDrawer(filterRoot, tableOverviewController.getFilterButton(), 
      tableOverviewController.getTableStack(), tableOverviewController.getTableView());

  }

  @FXML
  public void toggle() {
    super.toggleDrawer();
  }

  private void updateSettings(FilterSettings filterSettings) {
    updatesDisabled = true;
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
    withPovIniCheckBox.setSelected(filterSettings.isWithPovIni());
    withNVOffsetCheckBox.setSelected(filterSettings.isWithNVOffset());
    withAliasCheckBox.setSelected(filterSettings.isWithAlias());
    updatesDisabled = false;
  }

  public void applyFilter() {
    GameEmulatorRepresentation emulatorSelection = tableOverviewController.getEmulatorSelection();
    super.toggleFilterButton(!filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator()));

    if (updatesDisabled) {
      return;
    }

    Platform.runLater(() -> {
      tableOverviewController.onRefresh(filterSettings);
    });
  }

  public FilterSettings getFilterSettings() {
    return filterSettings;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

    configurationFilters.managedProperty().bindBidirectional(configurationFilters.visibleProperty());
    tableAssetFilters.managedProperty().bindBidirectional(tableAssetFilters.visibleProperty());
    vpsFilters.managedProperty().bindBidirectional(vpsFilters.visibleProperty());
    configurationIssuesFilter.managedProperty().bindBidirectional(configurationIssuesFilter.visibleProperty());

    List<GameEmulatorRepresentation> gameEmulators = new ArrayList<>(Studio.client.getFrontendService().getGameEmulators());
    gameEmulators.add(0, null);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);

    filterSettings = new FilterSettings();
    missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
    missingAssetsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setMissingAssets(newValue);
      applyFilter();
    });
    otherIssuesCheckbox.setSelected(filterSettings.isOtherIssues());
    otherIssuesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setOtherIssues(newValue);
      applyFilter();
    });
    vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
    vpsUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVpsUpdates(newValue);
      applyFilter();
    });
    versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
    versionUpdatesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setVersionUpdates(newValue);
      applyFilter();
    });
    notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
    notPlayedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNotPlayed(newValue);
      applyFilter();
    });
    noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
    noHighscoreSettingsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSettings(newValue);
      applyFilter();
    });
    noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
    noHighscoreSupportCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoHighscoreSupport(newValue);
      applyFilter();
    });
    noVpsMappingTableCheckBox.setSelected(filterSettings.isNoVpsTableMapping());
    noVpsMappingTableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoVpsTableMapping(newValue);
      applyFilter();
    });
    noVpsMappingVersionCheckBox.setSelected(filterSettings.isNoVpsVersionMapping());
    noVpsMappingVersionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNoVpsVersionMapping(newValue);
      applyFilter();
    });
    withBackglassCheckBox.setSelected(filterSettings.isWithBackglass());
    withBackglassCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithBackglass(newValue);
      applyFilter();
    });
    withPupPackCheckBox.setSelected(filterSettings.isWithPupPack());
    withPupPackCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPupPack(newValue);
      applyFilter();
    });
    withAltSoundCheckBox.setSelected(filterSettings.isWithAltSound());
    withAltSoundCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltSound(newValue);
      applyFilter();
    });
    withAltColorCheckBox.setSelected(filterSettings.isWithAltColor());
    withAltColorCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAltColor(newValue);
      applyFilter();
    });
    withPovIniCheckBox.setSelected(filterSettings.isWithPovIni());
    withPovIniCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithPovIni(newValue);
      applyFilter();
    });
    withNVOffsetCheckBox.setSelected(filterSettings.isWithNVOffset());
    withNVOffsetCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithNVOffset(newValue);
      applyFilter();
    });
    withAliasCheckBox.setSelected(filterSettings.isWithAlias());
    withAliasCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setWithAlias(newValue);
      applyFilter();
    });

    List<TableStatus> statuses = new ArrayList<>(TableDataController.TABLE_STATUSES_15);
    statuses.add(0, null);
    statusCombo.setItems(FXCollections.observableList(statuses));
    statusCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setGameStatus(-1);
      }
      else {
        filterSettings.setGameStatus(newValue.getValue());
      }
      applyFilter();
    });

    List<NoteType> noteTypes = new ArrayList<>(Arrays.asList(NoteType.values()));
    noteTypes.add(0, null);
    notesCombo.setItems(FXCollections.observableList(noteTypes));
    notesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setNoteType(null);
      }
      else {
        filterSettings.setNoteType(newValue);
      }
      applyFilter();
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
