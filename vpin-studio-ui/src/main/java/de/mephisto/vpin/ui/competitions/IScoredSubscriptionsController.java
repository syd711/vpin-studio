package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
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
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.GameRoomCellContainer;
import de.mephisto.vpin.ui.competitions.dialogs.IScoredGameCellContainer;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.tournaments.dialogs.IScoredGameRoomProgressModel;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.WaitNProgressModel;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
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

public class IScoredSubscriptionsController extends BaseCompetitionController implements Initializable, ChangeListener<IScoredGameRoom> {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionsController.class);

  @FXML
  private TableView<IScoredGameRoomGameModel> tableView;

  @FXML
  private TableColumn<IScoredGameRoomGameModel, Label> syncColumn;

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
  private StackPane tableStack;

  @FXML
  private Label validationErrorLabel;

  @FXML
  private Label validationErrorText;

  @FXML
  private Node validationError;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;

  private CompetitionsController competitionsController;
  private List<CompetitionRepresentation> iScoredSubscriptions;

  // Add a public no-args constructor
  public IScoredSubscriptionsController() {
  }

  @FXML
  private void onGameRooms() {
    PreferencesController.open("iscored");
  }

  @FXML
  private void onSync() {

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
//    IScoredGameRoom iScoredGameRoom = gameRoomsCombo.getValue();
//    if (iScoredGameRoom == null) {
//      return;
//    }
//
//    List<CompetitionRepresentation> result = CompetitionDialogs.openIScoredSubscriptionDialog(iScoredGameRoom, this.data);
//    if (!result.isEmpty()) {
//      try {
//        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Creating Subscriptions", result));
//        Platform.runLater(() -> {
//          doReload();
//          if (!resultModel.getResults().isEmpty()) {
//            IScoredGameRoomGameModel competitionRepresentation = (IScoredGameRoomGameModel) resultModel.results.get(0);
//            tableView.getSelectionModel().select(competitionRepresentation);
//            tableView.scrollTo(competitionRepresentation);
//          }
//        });
//      }
//      catch (Exception e) {
//        LOG.error("Failed to create iScored subscription: " + e.getMessage(), e);
//        WidgetFactory.showAlert(Studio.stage, e.getMessage());
//      }
//      doReload();
//    }
  }

  @FXML
  private void onDelete() {
    List<IScoredGameRoomGameModel> selectedItems = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    List<IScoredGameRoomGameModel> selections = selectedItems.stream().filter(s -> s.competition != null).collect(Collectors.toList());
    if (selections.isEmpty()) {
      return;
    }

    if (selections.size() == 1) {
      IScoredGameRoomGameModel selection = selections.get(0);
      String help = "The subscription will be deleted and none of your highscores will be pushed there anymore.";
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored subscription '" + selection.competition.getName() + "'?",
          help, null, "Delete iScored Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Subscription",
            "Deleting iScored Subscription",
            () -> client.getCompetitionService().deleteCompetition(selection.competition)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        doReload(false);
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
        doReload(false);
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    doReload(true);
  }

  private void doReload(boolean forceReload) {
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
      iScoredSubscriptions = client.getCompetitionService().getIScoredSubscriptions();

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

      syncBtn.setDisable(validGameRooms.isEmpty());

      PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
      addBtn.setDisable(validGameRooms.isEmpty() || defaultPlayer == null);

      filterCompetitions(iScoredSubscriptions);

      competitionWidget.setVisible(true);
      if (tableView.getItems().isEmpty()) {
        competitionWidget.setTop(null);
      }
      else {
        if (competitionWidget.getTop() == null && selection != null && selection.competition != null) {
          competitionWidget.setTop(competitionWidgetRoot);
        }
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

  private void refreshGameRoomsCombo(List<IScoredGameRoom> validGameRooms) {
    gameRoomsCombo.valueProperty().removeListener(this);

    IScoredGameRoom value = gameRoomsCombo.getValue();
    List<IScoredGameRoom> gameRoomsComboValues = new ArrayList<>(validGameRooms);
    gameRoomsComboValues.add(0, null);
    gameRoomsCombo.setItems(FXCollections.observableList(gameRoomsComboValues));
    gameRoomsCombo.setValue(value);
    gameRoomsCombo.setDisable(validGameRooms.isEmpty());

    gameRoomsCombo.valueProperty().addListener(this);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    NavigationController.setBreadCrumb(List.of("Competitions"));
    tableView.setPlaceholder(new Label("         No iScored subscription found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: {}", e.getMessage(), e);
    }


    syncColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();
      boolean synchronize = value.iScoredGameRoom.isSynchronize();
      Label label = new Label();
      if (synchronize) {
        label.setTooltip(new Tooltip("The competition for this game is automatically created and destroyed."));
        label.setGraphic(WidgetFactory.createCheckIcon(null));
      }
      return new SimpleObjectProperty<>(label);
    });

    settingsColumn.setCellValueFactory(cellData -> {
      IScoredGameRoomGameModel value = cellData.getValue();

      VBox vBox = new VBox();
      vBox.setAlignment(Pos.CENTER);
      HBox result = new HBox(6);
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
        label.setTooltip(new Tooltip("This game is a public game room game and not hidden."));
        label.setGraphic(WidgetFactory.createIcon("mdi2e-eye-outline"));
        result.getChildren().add(label);
      }

      if (value.game.isGameLocked() || value.game.isDisabled()) {
        Label label = new Label();
        label.setTooltip(new Tooltip("This game is currently locked/disabled. No highscores will be accepted."));
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
      fallbackLabel.setStyle(getLabelCss(value));
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.setText("No matching VPS table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      List<GameRepresentation> matches = new ArrayList<>();
      if (value.game.isAllVersionsEnabled()) {
        matches.addAll(client.getGameService().getGamesByVpsTable(value.getVpsTableId(), value.getVpsTableVersionId()));
      }
      else {
        matches.addAll(client.getGameService().getGamesByVpsTable(value.getVpsTableId(), null));
      }

      if (matches.isEmpty()) {
        fallbackLabel.setText("No matching table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      return new SimpleObjectProperty(new IScoredGameCellContainer(matches, vpsTable, getLabelCss(cellData.getValue())));
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
      return new SimpleObjectProperty(new GameRoomCellContainer(gameRoom, getLabelCss(cellData.getValue())));
    });

    tableView.setPlaceholder(new Label("                      Try iScored subscriptions!\n" +
        "Create a new subscription by pressing the '+' button."));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);

      competitionWidgetRoot.managedProperty().bindBidirectional(competitionWidget.visibleProperty());
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
        IScoredGameRoomGameModel selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<IScoredGameRoomGameModel, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(tableColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.game.getName()));
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
        }
        return true;
      }
    });

    gameRoomsCombo.valueProperty().addListener(this);

    validationError.setVisible(false);
    bindSearchField();
    onViewActivated(null);
  }

  @Override
  public void changed(ObservableValue<? extends IScoredGameRoom> observable, IScoredGameRoom oldValue, IScoredGameRoom newValue) {
    filterCompetitions(iScoredSubscriptions);
    syncBtn.setDisable(newValue == null);
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
      List<IScoredGame> gameRoomGames = gr.getTaggedGames();
      for (IScoredGame gameRoomGame : gameRoomGames) {
        Optional<CompetitionRepresentation> comp = iScoredSubscriptions.stream().filter(c -> gameRoomGame.matches(c.getVpsTableId(), c.getVpsTableVersionId())).findFirst();
        IScoredGameRoomGameModel model = new IScoredGameRoomGameModel(gameRoom, gr, gameRoomGame, comp.orElse(null));
        gameModels.add(model);
      }
    }

    return gameModels;
  }

  private void refreshView(Optional<IScoredGameRoomGameModel> model) {
    validationError.setVisible(false);
    IScoredGameRoomGameModel newSelection = null;
    if (model.isPresent()) {
      newSelection = model.get();
    }

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    deleteBtn.setDisable(defaultPlayer == null || this.gameRoomsCombo.isDisabled() || model.isEmpty());
    reloadBtn.setDisable(defaultPlayer == null);
    addBtn.setDisable(defaultPlayer == null || this.gameRoomsCombo.isDisabled() || model.isEmpty());


    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 No default player set!\n" +
          "Go to the players section and set the default player for this cabinet!"));
    }
    else {
      tableView.setPlaceholder(new Label("            No iScored subscription found.\nClick the '+' button to create a new one."));
    }


    if (model.isPresent()) {
      validationError.setVisible(newSelection.competition != null && newSelection.competition.getValidationState().getCode() > 0);

      if (newSelection.competition != null && newSelection.competition.getValidationState().getCode() > 0) {
        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection.competition);
        validationErrorLabel.setText(validationResult.getLabel());
        validationErrorText.setText(validationResult.getText());
      }

      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, newSelection.competition);
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, null);
      competitionsController.setCompetition(null);
    }

  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    if (this.competitionsController != null) {
      doReload(false);
    }
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
    if (model.competition != null && model.competition.getValidationState().getCode() > 0) {
      status = ERROR_STYLE;
    }
    else {
      IScoredGame gameByVps = model.game;
      if (gameByVps == null) {
        status = ERROR_STYLE;
      }
      else if (gameByVps.isDisabled() || gameByVps.isGameLocked()) {
        status = WidgetFactory.DISABLED_TEXT_STYLE;
      }
    }
    return status;
  }

  static class IScoredGameRoomGameModel {
    private final IScoredGameRoom iScoredGameRoom;
    private final GameRoom gameRoom;
    private final IScoredGame game;
    private final CompetitionRepresentation competition;

    public IScoredGameRoomGameModel(IScoredGameRoom iScoredGameRoom, GameRoom gameRoom, IScoredGame game, CompetitionRepresentation competition) {
      this.iScoredGameRoom = iScoredGameRoom;
      this.gameRoom = gameRoom;
      this.game = game;
      this.competition = competition;
    }

    public String getVpsTableId() {
      return game.getVpsTableId();
    }

    public String getVpsTableVersionId() {
      return game.getVpsTableVersionId();
    }
  }
}