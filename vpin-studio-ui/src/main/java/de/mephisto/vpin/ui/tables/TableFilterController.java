package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.CommentType;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameFilterRequest;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.ui.tables.dialogs.TableDataController;
import de.mephisto.vpin.ui.tables.models.TableStatus;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import de.mephisto.vpin.ui.util.tags.TagField;
import org.jspecify.annotations.NonNull;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TableFilterController extends BaseFilterController<GameRepresentation, GameRepresentationModel> implements Initializable, PreferenceChangeListener {

  @FXML
  private Node filterRoot;

  @FXML
  private CheckBox missingAssetsCheckBox;

  @FXML
  private CheckBox withNVOffsetCheckBox;

  @FXML
  private ComboBox<ValidationType> issueTypesCombo;

  @FXML
  private CheckBox vpsUpdatesCheckBox;

  @FXML
  private CheckBox versionUpdatesCheckBox;

  @FXML
  private CheckBox notPlayedCheckBox;

  @FXML
  private CheckBox notBackedUpCheckbox;

  @FXML
  private CheckBox noHighscoreSettingsCheckBox;

  @FXML
  private CheckBox noHighscoreSupportCheckBox;

  @FXML
  private CheckBox noVpsMappingTableCheckBox;

  @FXML
  private CheckBox iScoredCompetitionCheckBox;

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
  private Pane tagsFilter;

  @FXML
  private CheckBox todoTagsCheckBox;

  @FXML
  private ComboBox<TableStatus> statusCombo;

  @FXML
  private ComboBox<CommentType> commentsCombo;

  @FXML
  private Node metadataFilters;

  @FXML
  private TextField gameTypesField;

  @FXML
  private TextField manufacturersField;

  @FXML
  private CheckBox excludeManufacturersCheckBox;

  @FXML
  private ComboBox<Integer> minRatingCombo;

  @FXML
  private TextField yearFromField;

  @FXML
  private TextField yearToField;

  private TagField tagField;

  /**
   * Ids matching the current server-side filter. Null means no filter round-trip has
   * resolved yet, which is treated as "no id constraint" (show everything currently loaded).
   */
  private volatile Set<Integer> matchingIds;

  public void applyFilters() {
    // as we do not call filterGames() anymore, manually call saveFilterSettings to persist the reset
    JFXFuture.runAsync(() -> {
      client.getPreferenceService().setJsonPreference(filterSettings);
    }).thenLater(() -> {
      super.applyFilters();
    });
  }

  @Override
  protected boolean hasFilter() {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    return !filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator());
  }

  protected GameEmulatorRepresentation getEmulatorSelection() {
    return tableController.getEmulatorSelection();
  }

  @Override
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    refreshMatchingIds(searchTerm, playlist);
    Set<Integer> ids = matchingIds;
    return model -> ids == null || ids.contains(model.getGameId());
  }

  /**
   * Asynchronously resolves the ids matching the current filter criteria on the server,
   * then reconciles the table (fetching only the ids not yet loaded) once resolved.
   */
  private void refreshMatchingIds(String searchTerm, PlaylistRepresentation playlist) {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();

    GameFilterRequest request = new GameFilterRequest();
    request.setFilterSettings(filterSettings);
    request.setSearchTerm(searchTerm);
    request.setEmulatorId(emulatorSelection != null ? emulatorSelection.getId() : GameFilterRequest.ALL_VPX_ID);
    request.setPlaylistId(playlist != null ? playlist.getId() : null);

    JFXFuture.supplyAsync(() -> client.getGameService().filterGameIds(request))
        .thenAcceptLater(ids -> {
          matchingIds = new HashSet<>(ids);
          tableController.applyFilteredIds(matchingIds, GameRepresentationModel::getGameId, missingIds -> client.getGameService().getGamesByIds(missingIds));
        });
  }

  protected void resetFilters() {
    GameEmulatorRepresentation emulatorSelection = getEmulatorSelection();
    boolean resetted = filterSettings.isResetted(emulatorSelection == null || emulatorSelection.isVpxEmulator());
    if (!resetted) {
      this.filterSettings.reset();

      statusCombo.setValue(null);
      commentsCombo.setValue(null);
      issueTypesCombo.setValue(null);
      missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
      vpsUpdatesCheckBox.setSelected(filterSettings.isVpsUpdates());
      versionUpdatesCheckBox.setSelected(filterSettings.isVersionUpdates());
      notPlayedCheckBox.setSelected(filterSettings.isNotPlayed());
      notBackedUpCheckbox.setSelected(filterSettings.isNotBackedUp());
      noHighscoreSettingsCheckBox.setSelected(filterSettings.isNoHighscoreSettings());
      noHighscoreSupportCheckBox.setSelected(filterSettings.isNoHighscoreSupport());
      noVpsMappingTableCheckBox.setSelected(filterSettings.isNoVpsTableMapping());
      iScoredCompetitionCheckBox.setSelected(filterSettings.isIScored());
      iScoredCompetitionCheckBox.setSelected(filterSettings.isIScored());
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
      gameTypesField.setText("");
      manufacturersField.setText("");
      excludeManufacturersCheckBox.setSelected(false);
      minRatingCombo.setValue(null);
      yearFromField.setText("");
      yearToField.setText("");
    }
    tagField.setTags(filterSettings.getTags());
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    iScoredCompetitionCheckBox.managedProperty().bindBidirectional(iScoredCompetitionCheckBox.visibleProperty());

    configurationFilters.managedProperty().bindBidirectional(configurationFilters.visibleProperty());
    tableAssetFilters.managedProperty().bindBidirectional(tableAssetFilters.visibleProperty());
    vpsFilters.managedProperty().bindBidirectional(vpsFilters.visibleProperty());
    configurationIssuesFilter.managedProperty().bindBidirectional(configurationIssuesFilter.visibleProperty());
    withPupPackCheckBox.managedProperty().bindBidirectional(withPupPackCheckBox.visibleProperty());
    missingAssetsCheckBox.managedProperty().bindBidirectional(missingAssetsCheckBox.visibleProperty());
    notPlayedSettings.managedProperty().bindBidirectional(notPlayedSettings.visibleProperty());
    statusSettings.managedProperty().bindBidirectional(statusSettings.visibleProperty());

    metadataFilters.managedProperty().bindBidirectional(metadataFilters.visibleProperty());
    withPupPackCheckBox.setVisible(Features.PUPPACKS_ENABLED);
    statusSettings.setVisible(!Features.IS_STANDALONE);
    notPlayedSettings.setVisible(Features.STATISTICS_ENABLED);
    missingAssetsCheckBox.setVisible(Features.MEDIA_ENABLED);
  }

  public void loadFilterSettings(@NonNull FilterSettings filterSettings) {
    this.filterSettings = filterSettings;

    missingAssetsCheckBox.setSelected(filterSettings.isMissingAssets());
    missingAssetsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setMissingAssets(newValue);
      applyFilters();
    });

    List<ValidationType> issueTypes = GameValidationCode.values().stream().map(code -> new ValidationType(code, GameValidationCode.name(code))).collect(Collectors.toList());
    issueTypes.sort(Comparator.comparing(o -> o.name));
    issueTypes.addFirst( null);
    issueTypesCombo.setItems(FXCollections.observableList(issueTypes));

    if (filterSettings.getIssueType() != -1) {
      issueTypesCombo.setValue(new ValidationType(filterSettings.getIssueType(), GameValidationCode.name(filterSettings.getIssueType())));
    }

    issueTypesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        filterSettings.setIssueType(newValue.code);
      }
      else {
        filterSettings.setIssueType(-1);
      }

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
    notBackedUpCheckbox.setSelected(filterSettings.isNotBackedUp());
    notBackedUpCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setNotBackedUp(newValue);
      applyFilters();
    });
    iScoredCompetitionCheckBox.setSelected(filterSettings.isIScored());
    iScoredCompetitionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setIScored(newValue);
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

    loadMetadataFilters(filterSettings);

    List<TableStatus> statuses = new ArrayList<>(TableDataController.supportedStatuses());
    statuses.addFirst( null);
    statusCombo.setItems(FXCollections.observableList(statuses));
    statusCombo.setConverter(new StringConverter<TableStatus>() {
      @Override
      public String toString(TableStatus status) {
        if (status == null) {
          return "";
        }
        switch (status.getValue()) {
          case 0: return Messages.get("tables.tables_overview_filter.status_disabled");
          case 1: return Messages.get("tables.tables_overview_filter.status_normal");
          case 2: return Messages.get("tables.tables_overview_filter.status_mature");
          case 3: return Messages.get("tables.tables_overview_filter.status_wip");
          default: return status.getLabel();
        }
      }

      @Override
      public TableStatus fromString(String s) {
        return statusCombo.getValue();
      }
    });
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

    List<CommentType> noteTypes = new ArrayList<>(Arrays.asList(CommentType.values()));
    noteTypes.addFirst( null);
    commentsCombo.setItems(FXCollections.observableList(noteTypes));
    commentsCombo.setConverter(new StringConverter<CommentType>() {
      @Override
      public String toString(CommentType noteType) {
        if (noteType == null) {
          return "";
        }
        switch (noteType) {
          case Any: return Messages.get("tables.tables_overview_filter.note_type_any");
          case Errors: return Messages.get("tables.tables_overview_filter.note_type_errors");
          case Todos: return Messages.get("tables.tables_overview_filter.note_type_todos");
          case Outdated: return Messages.get("tables.tables_overview_filter.note_type_outdated");
          case None:
          default: return Messages.get("tables.tables_overview_filter.note_type_none");
        }
      }

      @Override
      public CommentType fromString(String s) {
        return commentsCombo.getValue();
      }
    });
    commentsCombo.setValue(filterSettings.getNoteType());
    commentsCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        filterSettings.setNoteType(null);
      }
      else {
        filterSettings.setNoteType(newValue);
      }
      applyFilters();
    });

    List<String> initialTags = client.getTaggingService().getTags();
    tagField = new TagField(initialTags);
    tagField.setAllowCustomTags(false);
    tagField.setPreferredWidth(200);
    tagField.setPreferredTagWidth(200);
    tagField.addListener(new ListChangeListener<String>() {
      @Override
      public void onChanged(Change<? extends String> c) {
        ObservableList<? extends String> list = c.getList();
        filterSettings.setTags(new ArrayList<>(list));
        refreshTodoTagsCheckBox();
        applyFilters();
      }
    });
    tagsFilter.getChildren().add(tagField);
    tagField.setTags(filterSettings.getTags());

    refreshTodoTagsCheckBox();
    todoTagsCheckBox.setOnAction(e -> {
      List<String> todoTags = getPauseMenuTodoTags();
      List<String> tags = tagField.getTags();
      tags.removeAll(todoTags);
      if (todoTagsCheckBox.isSelected()) {
        tags.addAll(todoTags);
      }
      tagField.setTags(tags);
    });

    client.getPreferenceService().addListener(this);
    preferencesChanged(PreferenceNames.ISCORED_SETTINGS, null);
  }

  private void loadMetadataFilters(@NonNull FilterSettings filterSettings) {
    gameTypesField.setText(filterSettings.getGameTypes());
    bindDebounced(gameTypesField, text -> filterSettings.setGameTypes(text.trim()));

    manufacturersField.setText(filterSettings.getManufacturers());
    bindDebounced(manufacturersField, text -> filterSettings.setManufacturers(text.trim()));

    excludeManufacturersCheckBox.setSelected(filterSettings.isExcludeManufacturers());
    excludeManufacturersCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setExcludeManufacturers(newValue);
      applyFilters();
    });

    // Popper's rating is 0-5, null means "any"
    minRatingCombo.setItems(FXCollections.observableList(Arrays.asList(null, 1, 2, 3, 4, 5)));
    minRatingCombo.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer rating) {
        return rating == null ? "" : "\u2605".repeat(rating) + " (" + rating + "+)";
      }

      @Override
      public Integer fromString(String s) {
        return minRatingCombo.getValue();
      }
    });
    minRatingCombo.setValue(filterSettings.getMinRating() > 0 ? filterSettings.getMinRating() : null);
    minRatingCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      filterSettings.setMinRating(newValue == null ? 0 : newValue);
      applyFilters();
    });

    yearFromField.setText(filterSettings.getYearFrom() > 0 ? String.valueOf(filterSettings.getYearFrom()) : "");
    bindDebounced(yearFromField, text -> filterSettings.setYearFrom(parseYear(text)));
    yearToField.setText(filterSettings.getYearTo() > 0 ? String.valueOf(filterSettings.getYearTo()) : "");
    bindDebounced(yearToField, text -> filterSettings.setYearTo(parseYear(text)));
  }

  /**
   * Text input triggers a server round-trip, so apply it only after the user stopped typing.
   */
  private void bindDebounced(TextField field, java.util.function.Consumer<String> setter) {
    PauseTransition debounce = new PauseTransition(Duration.millis(400));
    field.textProperty().addListener((observable, oldValue, newValue) -> {
      debounce.setOnFinished(e -> {
        setter.accept(newValue == null ? "" : newValue);
        applyFilters();
      });
      debounce.playFromStart();
    });
  }

  private static int parseYear(String text) {
    try {
      return Math.max(0, Integer.parseInt(text.trim()));
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  private List<String> getPauseMenuTodoTags() {
    TaggingSettings taggingSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.TAGGING_SETTINGS, TaggingSettings.class);
    return taggingSettings.getPauseMenuTags();
  }

  private void refreshTodoTagsCheckBox() {
    if (tagField != null) {
      List<String> todoTags = getPauseMenuTodoTags();
      todoTagsCheckBox.setSelected(!todoTags.isEmpty() && tagField.getTags().containsAll(todoTags));
    }
  }

  public TagField getTagField() {
    return tagField;
  }

  public void setEmulator(GameEmulatorRepresentation value) {
    filterSettings.setEmulatorId(value != null ? value.getId() : -1);

    boolean vpxMode = value == null || value.isVpxEmulator();
    configurationFilters.setVisible(vpxMode);
    tableAssetFilters.setVisible(vpxMode);
    vpsFilters.setVisible(vpxMode);
    configurationIssuesFilter.setVisible(vpxMode);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.ISCORED_SETTINGS.equalsIgnoreCase(key)) {
      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      iScoredCompetitionCheckBox.setVisible(iScoredSettings != null && iScoredSettings.isEnabled() && Features.COMPETITIONS_ENABLED);
    }
    else if (PreferenceNames.TAGGING_SETTINGS.equals(key)) {
      List<String> initialTags = client.getTaggingService().getTags();
      tagField.setSuggestions(initialTags);
      refreshTodoTagsCheckBox();
    }
  }

  class ValidationType {
    private final int code;
    private final String name;

    ValidationType(int code, String name) {
      this.code = code;
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      ValidationType that = (ValidationType) o;
      return code == that.code && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(code, name);
    }
  }
}
