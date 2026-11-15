package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.VpsGameCellContainer;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.WovpPatchProgressModel;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.vps.VpsTableContainer;
import de.mephisto.vpin.ui.vps.VpsVersionContainer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class WeeklySubscriptionsController extends BaseCompetitionController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(WeeklySubscriptionsController.class);

  @FXML
  private TableView<WeeklyCompetitionModel> tableView;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> tableColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> typeColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> patchedColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, String> vpsTableColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> vpsTableVersionColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, String> startDateColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, String> endDateColumn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button wovpPatchBtn;

  @FXML
  private Button wovpUnpatchBtn;

  @FXML
  private StackPane tableStack;

  @FXML
  private Button eventLogBtn;

  @FXML
  private ToolBar toolbar;

  private Parent loadingOverlay;


  private PlayButtonController playButtonController;
  private CompetitionsController competitionsController;
  private List<CompetitionRepresentation> weeklySubscriptions;

  /**
   * WoVP patch status by game id, resolved in the background on reload since the check reads the table script.
   */
  private final Map<Integer, Boolean> patchStatus = new ConcurrentHashMap<>();

  private boolean active = false;
  private boolean markDirty = false;

  // Add a public no-args constructor
  public WeeklySubscriptionsController() {
  }

  @FXML
  public void onStop() {
    Dialogs.killFrontend();
  }

  @FXML
  private void onEventLog(ActionEvent e) {
    WeeklyCompetitionModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      GameRepresentation gameRepresentation = value.getGame();
      if (gameRepresentation.isEventLogAvailable()) {
        TableDialogs.openEventLogDialog(gameRepresentation);
      }
    }
  }

  @FXML
  private void onWovpPatch(ActionEvent e) {
    runWovpPatch(getPatchCandidates(true), true);
  }

  @FXML
  private void onWovpUnpatch(ActionEvent e) {
    runWovpPatch(getPatchCandidates(false), false);
  }

  /**
   * The VPX tables of the current selection that are not already in the target patch state.
   */
  private List<GameRepresentation> getPatchCandidates(boolean patch) {
    List<GameRepresentation> result = new ArrayList<>();
    for (WeeklyCompetitionModel model : tableView.getSelectionModel().getSelectedItems()) {
      GameRepresentation game = model.getGame();
      if (game == null || !client.getEmulatorService().isVpxGame(game) || result.contains(game)) {
        continue;
      }
      Boolean patched = patchStatus.get(game.getId());
      if (patched == null || patched != patch) {
        result.add(game);
      }
    }
    return result;
  }

  @FXML
  private void onOpenTable(ActionEvent e) {
    WeeklyCompetitionModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      GameRepresentation game = value.getGame();
      if (game != null) {
        NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId()));
      }
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    WeeklyCompetitionModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      GameRepresentation game = value.getGame();
      if (game != null) {
        TableDialogs.openTableDataDialog(null, game);
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    client.getImageCache().clear("https://worldofvirtualpinball.com/");
    doReload(true);
  }

  private void doReload(boolean forceReload) {
    markDirty = false;
    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
    if (!wovpSettings.isEnabled()) {
      tableView.setItems(FXCollections.emptyObservableList());
      tableView.refresh();
      tableStack.getChildren().remove(loadingOverlay);
      tableView.setVisible(true);
      return;
    }

    WeeklyCompetitionModel selection = tableView.getSelectionModel().getSelectedItem();
    JFXFuture.supplyAsync(() -> {
      client.getCompetitionService().synchronizeWeeklyCompetitions(forceReload);
      weeklySubscriptions = client.getCompetitionService().getWeeklyCompetitions();

      patchStatus.clear();
      for (CompetitionRepresentation weeklySubscription : weeklySubscriptions) {
        int gameId = weeklySubscription.getGameId();
        if (gameId > 0 && !patchStatus.containsKey(gameId)) {
          try {
            patchStatus.put(gameId, client.getWovpService().isPatched(gameId));
          }
          catch (Exception e) {
            LOG.error("Failed to resolve WoVP patch status for game {}: {}", gameId, e.getMessage());
          }
        }
      }
      return weeklySubscriptions;
    }).thenAcceptLater((weeklySubscriptions) -> {
      filterCompetitions(weeklySubscriptions);

      //refresh re-synced tables
      for (CompetitionRepresentation weeklySubscription : weeklySubscriptions) {
        int gameId = weeklySubscription.getGameId();
        client.getAssetService().clearWheelIconCache(gameId);
        EventManager.getInstance().notifyTableChange(gameId, null);
      }

      if (selection != null && tableView.getItems().contains(selection)) {
        tableView.getSelectionModel().select(selection);
      }
      else if (!tableView.getItems().isEmpty()) {
        tableView.getSelectionModel().select(0);
      }
      else {
        refreshView(Optional.empty());
      }

      tableStack.getChildren().remove(loadingOverlay);
      tableView.setVisible(true);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    NavigationController.setBreadCrumb(List.of(Messages.get("navigation.competitions")));
    tableView.setPlaceholder(new Label(Messages.get("competitions.competitions_weekly.no_weekly_challenge_found")));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loader.setResources(Messages.getBundle());
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage(Messages.get("competitions.competitions_weekly.loading_competitions"));
      loaderController.setHint(Messages.get("competitions.competitions_weekly.loading_competitions.hint"));
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: {}", e.getMessage(), e);
    }

    typeColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();
      VBox box = new VBox();
      box.setAlignment(Pos.CENTER);
      Label modeLabel = new Label();
      modeLabel.getStyleClass().add("default-text");
      modeLabel.setStyle(getLabelCss(value));
      modeLabel.setText(value.getMode());

      box.getChildren().add(modeLabel);

      return new SimpleObjectProperty(box);
    });

    patchedColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();
      GameRepresentation game = value.getGame();
      if (game == null) {
        return new SimpleObjectProperty<>("");
      }

      Boolean patched = patchStatus.get(game.getId());
      if (patched == null) {
        return new SimpleObjectProperty<>("-");
      }

      Label label = new Label();
      if (patched) {
        label.setGraphic(WidgetFactory.createCheckIcon());
        label.setTooltip(new Tooltip(Messages.get("competitions.competitions_weekly.wovp_patched.yes")));
      }
      else {
        label.setGraphic(WidgetFactory.createExclamationIcon());
        label.setTooltip(new Tooltip(Messages.get("competitions.competitions_weekly.wovp_patched.no")));
      }
      return new SimpleObjectProperty<>(label);
    });

    tableColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();
      String issues = value.getCompetition().getIssues();

      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setStyle(getLabelCss(value));
      fallbackLabel.setWrapText(true);
      fallbackLabel.setPrefWidth(300);
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText(Messages.get("competitions.competitions_weekly.no_matching_vps_table_found"));
        return new SimpleObjectProperty<>(fallbackLabel);
      }


      if (value.getGame() == null) {
        issues = issues.replaceAll("\\|", "\n");

        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText(issues);
        fallbackLabel.setTooltip(new Tooltip(issues));
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      return new SimpleObjectProperty(new VpsGameCellContainer(Arrays.asList(value.game), vpsTable, getLabelCss(cellData.getValue())));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable != null) {
        return new SimpleObjectProperty(new VpsTableContainer(vpsTable, getLabelCss(value)));
      }
      return new SimpleStringProperty(Messages.get("competitions.competitions_weekly.no_matching_vps_table_found_2"));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();

      Label fallbackLabel = new Label();
      fallbackLabel.setStyle(getLabelCss(value));

      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.getStyleClass().add("default-text");
        fallbackLabel.setText(Messages.get("competitions.competitions_weekly.no_matching_vps_table_found_2"));
        return new SimpleObjectProperty<>(fallbackLabel);
      }
      VpsTableVersion vpsTableVersion = vpsTable.getTableVersionById(value.getVpsTableVersionId());
      if (vpsTableVersion == null) {
        fallbackLabel.getStyleClass().add("default-text");
        fallbackLabel.setText(Messages.get("competitions.competitions_weekly.all_versions_allowed"));
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      boolean downloadAction = client.getGameService().getGamesByVpsTable(value.getVpsTableId(), value.getVpsTableVersionId()).isEmpty();
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTable, vpsTableVersion, getLabelCss(cellData.getValue()), downloadAction));
    });

    startDateColumn.setCellValueFactory(cellData -> {
      VBox vBox = new VBox(3);
      WeeklyCompetitionModel value = cellData.getValue();
      Label dateLabel = new Label();
      dateLabel.getStyleClass().add("default-text");
      dateLabel.setStyle(getLabelCss(value));

      if (value.competition == null) {
        return new SimpleObjectProperty<>("-");
      }
      dateLabel.setText(DateUtil.formatDateTime(value.competition.getStartDate()));

      vBox.getChildren().add(dateLabel);
      return new SimpleObjectProperty(vBox);
    });

    endDateColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();

      VBox vBox = new VBox(3);

      Label endDateLabel = new Label();
      endDateLabel.getStyleClass().add("default-text");
      endDateLabel.setStyle(getLabelCss(value));
      endDateLabel.setText(DateUtil.formatDateTime(value.competition.getEndDate()));

      HBox durationBox = new HBox(3);
      Label durationLabel = new Label();
      durationLabel.setPrefWidth(80);
      durationLabel.getStyleClass().add("default-text");
      durationLabel.setStyle(getLabelCss(value));
      durationLabel.setText(Messages.get("competitions.discord_competition_edit.duration"));
      Label durationValueLabel = new Label();
      durationValueLabel.getStyleClass().add("default-text");
      durationValueLabel.setStyle(getLabelCss(value));
      durationValueLabel.setText(DateUtil.formatDuration(value.competition.getStartDate(), value.competition.getEndDate()));
      durationBox.getChildren().addAll(durationLabel, durationValueLabel);

      VBox remainingBox = new VBox(3);
      Label timeRemainingLabel = new Label();
      timeRemainingLabel.setPrefWidth(80);
      timeRemainingLabel.getStyleClass().add("default-text");
      timeRemainingLabel.setStyle(getLabelCss(value));
      timeRemainingLabel.setText(Messages.get("competitions.competitions_weekly.remaining"));
      Label timeRemainingValueLabel = new Label();
      timeRemainingValueLabel.getStyleClass().add("default-text");
      timeRemainingValueLabel.setStyle(getLabelCss(value));
      timeRemainingValueLabel.setText(DateUtil.formatDuration(OffsetDateTime.now(), value.competition.getEndDate()));
      remainingBox.getChildren().addAll(timeRemainingLabel, timeRemainingValueLabel);

      vBox.getChildren().addAll(endDateLabel, durationBox, remainingBox);
      return new SimpleObjectProperty(vBox);
    });

    tableView.setPlaceholder(new Label("                          " + Messages.get("competitions.competitions_weekly.try_weekly_challenges")));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });
    tableView.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<WeeklyCompetitionModel>) c -> {
      wovpPatchBtn.setDisable(getPatchCandidates(true).isEmpty());
      wovpUnpatchBtn.setDisable(getPatchCandidates(false).isEmpty());
    });
    wovpPatchBtn.setDisable(true);
    wovpUnpatchBtn.setDisable(true);

    tableView.setRowFactory(tv -> {
      TableRow<WeeklyCompetitionModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onEdit();
        }
      });
      final ContextMenu menu = new ContextMenu();
      // filled in the capturing phase: ContextMenu#show ignores menus without items, so it must be populated before the row's own handler shows it
      row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> refreshContextMenu(menu, row.getItem()));
      row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> row.setContextMenu(isNowEmpty ? null : menu));
      return row;
    });

    tableView.setSortPolicy(new Callback<TableView<WeeklyCompetitionModel>, Boolean>() {
      @Override
      public Boolean call(TableView<WeeklyCompetitionModel> gameRepresentationTableView) {
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<WeeklyCompetitionModel, ?> column = gameRepresentationTableView.getSortOrder().getFirst();
          if (column.equals(tableColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.competition != null ? o.competition.getName() : null));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(vpsTableColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> {
              VpsTable tableById = client.getVpsService().getTableById(o.getVpsTableId());
              if (tableById != null) {
                return tableById.getDisplayName();
              }
              return "";
            }));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(startDateColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.competition != null ? o.competition.getStartDate() : null));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(endDateColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.competition != null ? o.competition.getEndDate() : null));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
        }
        return true;
      }
    });


    try {
      FXMLLoader loader = new FXMLLoader(PlayButtonController.class.getResource("play-btn.fxml"));
      loader.setResources(Messages.getBundle());
      Parent playBtnRoot = loader.load();
      playButtonController = loader.getController();
      playButtonController.setDisable(true);
      toolbar.getItems().add(toolbar.getItems().size() - 1, playBtnRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load play button: " + e.getMessage(), e);
    }

    bindSearchField();

    Frontend frontend = client.getFrontendService().getFrontend();
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    EventManager.getInstance().addListener(this);
  }

  private void refreshContextMenu(ContextMenu ctxMenu, WeeklyCompetitionModel model) {
    ctxMenu.getItems().clear();
    if (model == null) {
      return;
    }
    GameRepresentation game = model.getGame();

    MenuItem dataManagerItem = new MenuItem(Messages.get("tables.context_menu.edit_table_data"));
    dataManagerItem.setGraphic(WidgetFactory.createIcon("mdi2l-lead-pencil"));
    dataManagerItem.setDisable(game == null);
    dataManagerItem.setOnAction(e -> TableDialogs.openTableDataDialog(null, game));
    ctxMenu.getItems().add(dataManagerItem);

    VpsTable vpsTable = client.getVpsService().getTableById(model.getVpsTableId());
    MenuItem downloadItem = new MenuItem(Messages.get("dialog.download_table"));
    downloadItem.setGraphic(WidgetFactory.createIcon("mdi2o-open-in-new"));
    downloadItem.setDisable(vpsTable == null);
    downloadItem.setOnAction(e -> Studio.browse(VPS.getVpsTableUrl(vpsTable.getId())));
    ctxMenu.getItems().add(downloadItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem eventLogItem = new MenuItem(Messages.get("tables.context_menu.event_log"));
    eventLogItem.setGraphic(WidgetFactory.createIcon("mdi2m-message-text-clock-outline"));
    eventLogItem.setDisable(game == null || !game.isEventLogAvailable());
    eventLogItem.setOnAction(e -> TableDialogs.openEventLogDialog(game));
    ctxMenu.getItems().add(eventLogItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    List<GameRepresentation> patchCandidates = getPatchCandidates(true);
    MenuItem wovpPatchItem = new MenuItem(Messages.get("tables.context_menu.wovp_patch"));
    wovpPatchItem.setGraphic(WidgetFactory.createIcon("mdi2c-checkbox-marked-circle-plus-outline"));
    wovpPatchItem.setDisable(patchCandidates.isEmpty());
    wovpPatchItem.setOnAction(e -> runWovpPatch(patchCandidates, true));
    ctxMenu.getItems().add(wovpPatchItem);

    List<GameRepresentation> unpatchCandidates = getPatchCandidates(false);
    MenuItem wovpUnpatchItem = new MenuItem(Messages.get("tables.context_menu.wovp_unpatch"));
    wovpUnpatchItem.setGraphic(WidgetFactory.createIcon("mdi2c-checkbox-marked-circle-minus-outline"));
    wovpUnpatchItem.setDisable(unpatchCandidates.isEmpty());
    wovpUnpatchItem.setOnAction(e -> runWovpPatch(unpatchCandidates, false));
    ctxMenu.getItems().add(wovpUnpatchItem);
  }

  private void runWovpPatch(List<GameRepresentation> games, boolean patch) {
    if (games.isEmpty()) {
      return;
    }
    WovpPatchProgressModel.run(games, patch);
    JFXFuture.supplyAsync(() -> {
      for (GameRepresentation game : games) {
        patchStatus.put(game.getId(), client.getWovpService().isPatched(game.getId()));
      }
      return games;
    }).thenAcceptLater(updated -> {
      tableView.refresh();
      refreshView(getSelection());
    });
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());
      filterCompetitions(this.weeklySubscriptions);
    });
  }

  private void filterCompetitions(List<CompetitionRepresentation> weeklySubscriptions) {
    List<WeeklyCompetitionModel> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();

    List<WeeklyCompetitionModel> models = getWeeklyCompetitionModels(weeklySubscriptions);
    for (WeeklyCompetitionModel model : models) {
      if (!model.getName().toLowerCase().contains(filterValue.toLowerCase())) {
        continue;
      }

      filtered.add(model);
    }
    tableView.setItems(FXCollections.observableList(filtered));
    tableView.refresh();
  }

  private List<WeeklyCompetitionModel> getWeeklyCompetitionModels(List<CompetitionRepresentation> subscriptions) {
    List<WeeklyCompetitionModel> gameModels = new ArrayList<>();
    for (CompetitionRepresentation subscription : subscriptions) {
      WeeklyCompetitionModel model = new WeeklyCompetitionModel(subscription);
      gameModels.add(model);
    }
    return gameModels;
  }

  private void refreshView(Optional<WeeklyCompetitionModel> model) {
    WeeklyCompetitionModel newSelection = null;
    playButtonController.setData(null);

    if (model.isPresent()) {
      newSelection = model.get();
      if (newSelection.getGame() != null) {
        playButtonController.setData(newSelection.getGame());
      }
    }


    eventLogBtn.setDisable(model.isEmpty());
    if (model.isPresent()) {
      GameRepresentation gameRepresentation = model.get().getGame();
      if (gameRepresentation != null && gameRepresentation.isEventLogAvailable()) {
        eventLogBtn.setDisable(false);
      }
      else {
        eventLogBtn.setDisable(true);
      }
    }

    tableNavigateBtn.setDisable(model.isEmpty() || model.get().getGame() == null);
    dataManagerBtn.setDisable(model.isEmpty() || model.get().getGame() == null);
    wovpPatchBtn.setDisable(getPatchCandidates(true).isEmpty());
    wovpUnpatchBtn.setDisable(getPatchCandidates(false).isEmpty());

    competitionsController.setCompetition(model.map(weeklyCompetitionModel -> weeklyCompetitionModel.competition).orElse(null));

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    reloadBtn.setDisable(defaultPlayer == null);

    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 " + Messages.get("competitions.competitions_weekly.no_default_player_set")));
    }
    else {
      tableView.setPlaceholder(new Label(Messages.get("competitions.competitions_weekly.no_weekly_challenge_found")));
    }
  }

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      if (getSelection().isPresent()) {
        WeeklyCompetitionModel competitionModel = getSelection().get();
        if (competitionModel.getGame() != null) {
          TableDialogs.openTableDataDialog(null, competitionModel.game);
        }
      }
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    this.active = true;
    if (this.competitionsController != null) {
      doReload(markDirty);
    }
  }

  @Override
  public void onViewDeactivated() {
    super.onViewDeactivated();
    this.active = false;
  }

  public void setCompetitionsController(CompetitionsController competitionsController) {
    this.competitionsController = competitionsController;
  }

  public Optional<WeeklyCompetitionModel> getSelection() {
    WeeklyCompetitionModel selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  public static String getLabelCss(WeeklyCompetitionModel model) {
    String status = "";
    return status;
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (PreferenceType.competitionSettings.equals(preferenceType)) {
      if (active) {
        Platform.runLater(() -> {
          doReload(true);
        });
      }
      else {
        markDirty = true;
      }
    }
  }

  static class WeeklyCompetitionModel {
    private final CompetitionRepresentation competition;
    private GameRepresentation game;

    public WeeklyCompetitionModel(CompetitionRepresentation competition) {
      this.competition = competition;
      this.game = client.getGameService().getGame(competition.getGameId());
    }

    public CompetitionRepresentation getCompetition() {
      return competition;
    }

    public String getName() {
      return competition.getName();
    }

    public GameRepresentation getGame() {
      return game;
    }

    public String getVpsTableId() {
      return competition.getVpsTableId();
    }

    public String getVpsTableVersionId() {
      return competition.getVpsTableVersionId();
    }

    public String getMode() {
      if (competition.getMode().equals("tournament")) {
        return "KO";
      }
      if (competition.getMode().equals("monthly_contracts")) {
        return Messages.get("dialog.competition_mode_monthly_contracts");
      }
      return StringUtils.capitalize(competition.getMode());
    }
  }
}
