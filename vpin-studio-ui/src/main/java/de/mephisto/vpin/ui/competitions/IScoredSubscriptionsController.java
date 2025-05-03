package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.GameRoomCellContainer;
import de.mephisto.vpin.ui.competitions.dialogs.IScoredGameCellContainer;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.WaitNProgressModel;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;

public class IScoredSubscriptionsController extends BaseCompetitionController implements Initializable, ChangeListener<IScoredGameRoom>, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionsController.class);

  @FXML
  private TableView<IScoredGameRoomGameModel> tableView;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, Object> settingsColumn;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, Object> tableColumn;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, String> vpsTableColumn;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, Object> vpsTableVersionColumn;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, String> gameRoomColumn;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, String> creationDateColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button syncBtn;

  @FXML
  private BorderPane competitionWidget;

  @FXML
  private ComboBox<IScoredGameRoom> gameRoomsCombo;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button editBtn;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;

  private CompetitionsController competitionsController;
  private List<CompetitionRepresentation> iScoredSubscriptions;

  private boolean active = false;
  private boolean markDirty = false;

  // Add a public no-args constructor
  public IScoredSubscriptionsController() {
  }

  @FXML
  private void onOpenTable(ActionEvent e) {
    IScoredGameRoomGameModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      List<GameRepresentation> matches = value.getMatches();
      if (!matches.isEmpty()) {
        NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(matches.get(0).getId()));
      }
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    IScoredGameRoomGameModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      List<GameRepresentation> matches = value.getMatches();
      if (!matches.isEmpty()) {
        TableDialogs.openTableDataDialog(null, matches.get(0));
      }
    }
  }

  @FXML
  private void onGameRooms() {
    PreferencesController.open("iscored");
  }

  @FXML
  private void onSync() {
    IScoredGameRoom value = gameRoomsCombo.getValue();
    if (value != null) {
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl(), false);
      ProgressDialog.createProgressDialog(new IScoredGameRoomGamesSynchronizationProgressModel(value, gameRoom.getGames(), false));
      this.iScoredSubscriptions = null;
      doReload(false);
    }
  }


  @FXML
  private void onEdit() {
    IScoredGameRoom value = gameRoomsCombo.getValue();
    if (value != null) {
      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      boolean result = TableDialogs.openIScoredGameRoomDialog(iScoredSettings, value);
      if (result) {
        doReload(false);
      }
    }
  }

  @FXML
  private void onCompetitionCreate() {
    IScoredGameRoomGameModel selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      ProgressDialog.createProgressDialog(new IScoredGameRoomGamesSynchronizationProgressModel(selectedItem.iScoredGameRoom, Arrays.asList(selectedItem.game), true));
      this.iScoredSubscriptions = null;
      doReload(false);

      List<GameRepresentation> matches = selectedItem.getMatches();
      for (GameRepresentation match : matches) {
        EventManager.getInstance().notifyTableChange(match.getId(), null);
      }
    }
  }

  @FXML
  private void onDelete() {
    List<IScoredGameRoomGameModel> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    List<IScoredGameRoomGameModel> selections = selectedItems.stream().filter(s -> s.competition != null).collect(Collectors.toList());
    if (selections.isEmpty()) {
      return;
    }

    List<Integer> gameIds = new ArrayList<>();
    for (IScoredGameRoomGameModel gameRoomGameModel : selections) {
      gameRoomGameModel.getMatches().stream().forEach(g -> gameIds.add(g.getId()));
    }

    if (selections.size() == 1) {
      IScoredGameRoomGameModel selection = selections.get(0);
      String help2 = null;
      if (selection.iScoredGameRoom.isSynchronize()) {
        help2 = "IMPORTANT: The synchronization for this game room is enabled. So the competition will be re-created during the next synchronization!";
      }

      String help = "The subscription will be deleted and none of your highscores will be pushed there anymore.";
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete \"" + selection.competition.getName() + "\"?",
          help, help2, "Delete iScored Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Subscription",
            "Deleting iScored Subscription",
            () -> client.getCompetitionService().deleteCompetition(selection.competition)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        this.iScoredSubscriptions = null;
        doReload(true);

        for (Integer gameId : gameIds) {
          EventManager.getInstance().notifyTableChange(gameId, null);
        }
      }
    }

    if (selections.size() > 1) {
      String help = "The selected subscriptions will be deleted and none of your highscores will be pushed there anymore.";
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete selected iScored subscriptions?",
          help, null, "Delete iScored Subscriptions");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitNProgressModel<>("Delete Subscriptions", selections,
            selection -> "Deleting iScored Subscription",
            selection -> {
              client.getCompetitionService().deleteCompetition(selection.competition);
            }));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        doReload(true);

        for (Integer gameId : gameIds) {
          EventManager.getInstance().notifyTableChange(gameId, null);
        }
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    doReload(true);
  }

  private void doReload(boolean forceReload) {
    markDirty = false;
    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    IScoredGameRoomGameModel selection = tableView.getSelectionModel().getSelectedItem();
    IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);

    if (forceReload) {
      ProgressDialog.createProgressDialog(new IScoredGameRoomProgressModel(iScoredSettings.getGameRooms(), true));
    }

    List<IScoredGameRoom> validGameRooms = new ArrayList<>();
    JFXFuture.supplyAsync(() -> {
      if (this.iScoredSubscriptions == null || forceReload) {
        iScoredSubscriptions = client.getCompetitionService().getIScoredSubscriptions();
      }

      List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
      for (IScoredGameRoom gameRoom : gameRooms) {
        GameRoom gr = IScored.getGameRoom(gameRoom.getUrl(), false);
        if (gr != null) {
          validGameRooms.add(gameRoom);
        }
      }
      return iScoredSubscriptions;
    }).thenAcceptLater((iScoredSubscriptions) -> {

      refreshGameRoomsCombo(validGameRooms);

      PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
      addBtn.setDisable(validGameRooms.isEmpty() || defaultPlayer == null);

      filterCompetitions(iScoredSubscriptions);

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

  private void refreshGameRoomsCombo(List<IScoredGameRoom> validGameRooms) {
    gameRoomsCombo.valueProperty().removeListener(this);
    editBtn.setDisable(true);

    IScoredGameRoom value = gameRoomsCombo.getValue();
    List<IScoredGameRoom> gameRoomsComboValues = new ArrayList<>(validGameRooms);
    gameRoomsComboValues.add(0, null);
    gameRoomsCombo.setItems(FXCollections.observableList(gameRoomsComboValues));
    gameRoomsCombo.setValue(value);
    gameRoomsCombo.setDisable(validGameRooms.isEmpty());
    if (gameRoomsComboValues.size() > 1) {
      gameRoomsCombo.setValue(validGameRooms.get(0));
    }

    syncBtn.setDisable(gameRoomsCombo.getValue() == null);
    editBtn.setDisable(gameRoomsCombo.getValue() == null);

    gameRoomsCombo.valueProperty().addListener(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    NavigationController.setBreadCrumb(List.of("Competitions"));
    tableView.setPlaceholder(new Label("         No iScored subscription found.\nClick the '+' button to create a new one."));

    this.editBtn.setDisable(true);

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: {}", e.getMessage(), e);
    }

    settingsColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();

      VBox vBox = new VBox();
      vBox.setAlignment(Pos.CENTER);
      HBox result = new HBox(6);
      result.setAlignment(Pos.CENTER);
      vBox.getChildren().add(result);

      if (value.game.isAllVersionsEnabled()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("Highscores from all versions of the table can be submitted. The scoring is not limited to one table version."));
        label.setGraphic(WidgetFactory.createIcon("mdi2c-check-all"));
        result.getChildren().add(label);
      }
      else {
        Label label = new Label();
        label.setTooltip(new Tooltip("Only highscores for this table version can be submitted. Scores from other versions of this table will be ignored."));
        label.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
        result.getChildren().add(label);
      }

      if (value.game.isGameHidden()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("This game is hidden from the public game room."));
        label.setGraphic(WidgetFactory.createIcon("mdi2e-eye-off"));
        result.getChildren().add(label);
      }
      else {
        Label label = new Label();
        label.setTooltip(new Tooltip("This game is a public and visible on the game room dashboard."));
        label.setGraphic(WidgetFactory.createIcon("mdi2e-eye-outline"));
        result.getChildren().add(label);
      }

      if (value.game.isGameLocked()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("This game is currently locked. No highscores will be accepted."));
        label.setGraphic(WidgetFactory.createIcon("mdi2l-lock"));
        result.getChildren().add(label);
      }
      else if (value.game.isDisabled()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("This game is currently disabled. No highscores will be accepted."));
        label.setGraphic(WidgetFactory.createIcon("mdi2l-lock"));
        result.getChildren().add(label);
      }

      if (value.game.isSingleScore()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("Only one score per player can be submitted. All subsequent scores will be ignored, even higher scores."));
        label.setGraphic(WidgetFactory.createIcon("mdi2n-numeric-1-box"));
        result.getChildren().add(label);
      }

      if (value.game.isMultiScore()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("All scores of the players will be submitted."));
        label.setGraphic(WidgetFactory.createIcon("mdi2n-numeric-9-plus-box"));
        result.getChildren().add(label);
      }
      return new SimpleObjectProperty<>(vBox);
    });

    tableColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();

      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setStyle(getLabelCss(value));
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText("No matching VPS table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      if (value.getMatches().isEmpty()) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText("No matching table found.");
        fallbackLabel.setTooltip(new Tooltip("No matching table found. Download and install this table using the download link."));
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      if (value.competition == null) {
        fallbackLabel.setStyle(WidgetFactory.OK_STYLE);
        if (value.iScoredGameRoom.isSynchronize()) {
          fallbackLabel.setText("This game is not synchronized yet.");
          fallbackLabel.setTooltip(new Tooltip("Synchronization is enabled but the competition has not been created yet."));
        }
        else {
          fallbackLabel.setText("Table match found, but not subscribed yet.");
          fallbackLabel.setTooltip(new Tooltip("The subscription can be create by pressing the \"+\" button."));
        }

        return new SimpleObjectProperty<>(fallbackLabel);
      }

      return new SimpleObjectProperty(new IScoredGameCellContainer(value.getMatches(), vpsTable, getLabelCss(cellData.getValue())));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable != null) {
        return new SimpleObjectProperty(new VpsTableContainer(vpsTable, getLabelCss(value)));
      }
      return new SimpleStringProperty("No matching VPS Table found.");
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();

      Label fallbackLabel = new Label();
      fallbackLabel.setStyle(getLabelCss(value));
      if (value.game.isAllVersionsEnabled()) {
        fallbackLabel.setText("All versions allowed.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.setText("No matching VPS Table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }
      VpsTableVersion vpsTableVersion = vpsTable.getTableVersionById(value.getVpsTableVersionId());
      if (vpsTableVersion == null) {
        fallbackLabel.setText("All versions allowed.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      boolean downloadAction = client.getGameService().getGamesByVpsTable(value.getVpsTableId(), value.getVpsTableVersionId()).isEmpty();
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTable, vpsTableVersion, getLabelCss(cellData.getValue()), downloadAction));
    });

    gameRoomColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();
      GameRoom gameRoom = value.gameRoom;
      if (gameRoom == null) {
        return new SimpleObjectProperty<>("Invalid Game Room URL");
      }
      return new SimpleObjectProperty(new GameRoomCellContainer(gameRoom, value.game, getLabelCss(cellData.getValue())));
    });

    creationDateColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();
      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setStyle(getLabelCss(value));

      if (value.competition == null) {
        return new SimpleObjectProperty<>("-");
      }
      fallbackLabel.setText(DateUtil.formatDateTime(value.competition.getCreatedAt()));
      return new SimpleObjectProperty(fallbackLabel);
    });

    tableView.setPlaceholder(new Label("                          Try iScored subscriptions!\n" +
        "Create new subscriptions adding game rooms in the iScored preferences."));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);
      competitionWidget.setTop(competitionWidgetRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load c-widget: " + e.getMessage(), e);
    }

    tableView.setRowFactory(tv -> {
      TableRow<IScoredGameRoomGameModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onEdit();
        }
      });
      return row;
    });

    tableView.setSortPolicy(new Callback<TableView<IScoredGameRoomGameModel>, Boolean>() {
      @Override
      public Boolean call(TableView<IScoredGameRoomGameModel> gameRepresentationTableView) {
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<IScoredGameRoomGameModel, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(tableColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.competition != null ? o.competition.getName() : null));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(gameRoomColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.gameRoom.getName()));
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
          else if (column.equals(creationDateColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.competition != null ? o.competition.getCreatedAt() : null));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
        }
        return true;
      }
    });

    gameRoomsCombo.valueProperty().addListener(this);
    bindSearchField();
    onViewActivated(null);

    client.getPreferenceService().addListener(this);
  }

  @Override
  public void changed(ObservableValue<? extends IScoredGameRoom> observable, IScoredGameRoom oldValue, IScoredGameRoom newValue) {
    filterCompetitions(iScoredSubscriptions);
    syncBtn.setDisable(newValue == null);
    editBtn.setDisable(newValue == null);
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());
      filterCompetitions(this.iScoredSubscriptions);
    });
  }

  private void filterCompetitions(List<CompetitionRepresentation> iScoredSubscriptions) {
    List<IScoredGameRoomGameModel> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();

    List<IScoredGameRoomGameModel> collectedGameRoomGames = getGameRoomGameModels(iScoredSubscriptions);
    for (IScoredGameRoomGameModel model : collectedGameRoomGames) {
      if (!model.game.getName().toLowerCase().contains(filterValue.toLowerCase())) {
        continue;
      }

      filtered.add(model);
    }
    tableView.setItems(FXCollections.observableList(filtered));
    tableView.refresh();
  }

  private List<IScoredGameRoomGameModel> getGameRoomGameModels(List<CompetitionRepresentation> iScoredSubscriptions) {
    IScoredGameRoom selectedGameRoom = gameRoomsCombo.getValue();
    List<IScoredGameRoom> gameRooms = new ArrayList<>();
    if (selectedGameRoom != null) {
      gameRooms.add(selectedGameRoom);
    }
    else {
      ObservableList<IScoredGameRoom> items = gameRoomsCombo.getItems();
      for (IScoredGameRoom item : items) {
        if (item != null) {
          gameRooms.add(item);
        }
      }
    }

    List<IScoredGameRoomGameModel> gameModels = new ArrayList<>();
    for (IScoredGameRoom gameRoom : gameRooms) {
      GameRoom gr = IScored.getGameRoom(gameRoom.getUrl(), false);
      if (gr != null) {
        List<IScoredGame> gameRoomGames = gr.getTaggedGames();
        for (IScoredGame gameRoomGame : gameRoomGames) {
          Optional<CompetitionRepresentation> comp = iScoredSubscriptions.stream().filter(c -> gameRoomGame.matches(c.getVpsTableId(), c.getVpsTableVersionId())).findFirst();
          IScoredGameRoomGameModel model = new IScoredGameRoomGameModel(gameRoom, gr, gameRoomGame, comp.orElse(null));
          gameModels.add(model);
        }
      }
    }

    return gameModels;
  }

  private void refreshView(Optional<IScoredGameRoomGameModel> model) {
    IScoredGameRoomGameModel newSelection = null;
    if (model.isPresent()) {
      newSelection = model.get();
    }

    tableNavigateBtn.setDisable(model.isEmpty() || model.get().getMatches().isEmpty());
    dataManagerBtn.setDisable(model.isEmpty() || model.get().getMatches().isEmpty());

    competitionsController.setCompetition(model.isPresent() ? model.get().competition : null);

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    deleteBtn.setDisable(defaultPlayer == null || this.gameRoomsCombo.isDisabled() || model.isEmpty() || newSelection.competition == null);
    reloadBtn.setDisable(defaultPlayer == null);
    addBtn.setDisable(defaultPlayer == null || this.gameRoomsCombo.isDisabled() || model.isEmpty() || newSelection.competition != null || newSelection.getMatches().isEmpty());


    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 No default player set!\n" +
          "Go to the players section and set the default player for this cabinet!"));
    }
    else {
      tableView.setPlaceholder(new Label("            No iScored subscription found.\nClick the '+' button to create a new one."));
    }

    if (model.isPresent() && model.get().competition != null) {
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, newSelection.competition);
      competitionsController.setCompetition(newSelection.competition);
    }
    else {
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, null);
      competitionsController.setCompetition(null);
    }
  }

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      if (getSelection().isPresent()) {
        IScoredGameRoomGameModel gameRoomGameModel = getSelection().get();
        if (!gameRoomGameModel.getMatches().isEmpty()) {
          TableDialogs.openTableDataDialog(null, gameRoomGameModel.getMatches().get(0));
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

  public Optional<IScoredGameRoomGameModel> getSelection() {
    IScoredGameRoomGameModel selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  public static String getLabelCss(IScoredGameRoomGameModel model) {
    String status = "";
//    if (model.competition != null && model.competition.getValidationState().getCode() > 0) {
//      status = ERROR_STYLE;
//    }
//    else {
    IScoredGame gameByVps = model.game;
    if (gameByVps == null) {
      status = ERROR_STYLE;
    }
    else if (gameByVps.isDisabled() || gameByVps.isGameLocked()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
//    }
    return status;
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.ISCORED_SETTINGS.equalsIgnoreCase(key)) {
      if (active) {
        doReload(true);
      }
      else {
        markDirty = true;
      }
    }
  }

  static class IScoredGameRoomGameModel {
    private final IScoredGameRoom iScoredGameRoom;
    private final GameRoom gameRoom;
    private final IScoredGame game;
    private final CompetitionRepresentation competition;
    private final List<GameRepresentation> matches = new ArrayList<>();

    public IScoredGameRoomGameModel(IScoredGameRoom iScoredGameRoom, GameRoom gameRoom, IScoredGame iScoredGame, CompetitionRepresentation competition) {
      this.iScoredGameRoom = iScoredGameRoom;
      this.gameRoom = gameRoom;
      this.game = iScoredGame;
      this.competition = competition;

      if (iScoredGame.isAllVersionsEnabled()) {
        matches.addAll(client.getGameService().getGamesByVpsTable(iScoredGame.getVpsTableId(), null));
      }
      else {
        matches.addAll(client.getGameService().getGamesByVpsTable(iScoredGame.getVpsTableId(), iScoredGame.getVpsTableVersionId()));
      }
    }

    public List<GameRepresentation> getMatches() {
      return matches;
    }

    public String getVpsTableId() {
      return game.getVpsTableId();
    }

    public String getVpsTableVersionId() {
      return game.getVpsTableVersionId();
    }
  }
}