package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
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
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.util.FrontendUtil;
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
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;

public class WeeklySubscriptionsController extends BaseCompetitionController implements Initializable, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(WeeklySubscriptionsController.class);

  @FXML
  private TableView<WeeklyCompetitionModel> tableView;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> tableColumn;

  @FXML
  private TableColumn<WeeklyCompetitionModel, Object> typeColumn;

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
  private StackPane tableStack;

  @FXML
  private Button eventLogBtn;

  @FXML
  private ToolBar toolbar;

  private Parent loadingOverlay;


  private PlayButtonController playButtonController;
  private CompetitionsController competitionsController;
  private List<CompetitionRepresentation> weeklySubscriptions;

  private boolean active = false;
  private boolean markDirty = false;

  // Add a public no-args constructor
  public WeeklySubscriptionsController() {
  }

  @FXML
  public void onStop() {
    Frontend frontend = client.getFrontendService().getFrontendCached();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        FrontendUtil.replaceNames("Stop all emulators and [Frontend] processes?", frontend, null));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      JFXFuture.supplyAsync(() -> {
        return client.getFrontendService().terminateFrontend();
      }).thenAcceptLater((requestResult) -> {
        LOG.info("Kill frontend request finished.");
      });
    }
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
      return weeklySubscriptions;
    }).thenAcceptLater((weeklySubscriptions) -> {
      filterCompetitions(weeklySubscriptions);

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
    NavigationController.setBreadCrumb(List.of("Competitions"));
    tableView.setPlaceholder(new Label("No weekly challenge found.\nClick the '+' button to join one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
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

    tableColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();

      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setStyle(getLabelCss(value));
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText("No matching VPS table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }

      if (value.getGame() == null) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText("No matching table found.");
        fallbackLabel.setTooltip(new Tooltip("No matching table found. Download and install this table using the download link."));
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
      return new SimpleStringProperty("No matching VPS Table found.");
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();

      Label fallbackLabel = new Label();
      fallbackLabel.setStyle(getLabelCss(value));

      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        fallbackLabel.getStyleClass().add("default-text");
        fallbackLabel.setText("No matching VPS Table found.");
        return new SimpleObjectProperty<>(fallbackLabel);
      }
      VpsTableVersion vpsTableVersion = vpsTable.getTableVersionById(value.getVpsTableVersionId());
      if (vpsTableVersion == null) {
        fallbackLabel.getStyleClass().add("default-text");
        fallbackLabel.setText("All versions allowed.");
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
      dateLabel.setText(DateUtil.formatDateTime(value.competition.getCreatedAt()));

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
      durationLabel.setText("Duration:");
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
      timeRemainingLabel.setText("Remaining:");
      Label timeRemainingValueLabel = new Label();
      timeRemainingValueLabel.getStyleClass().add("default-text");
      timeRemainingValueLabel.setStyle(getLabelCss(value));
      timeRemainingValueLabel.setText(DateUtil.formatDuration(new Date(), value.competition.getEndDate()));
      remainingBox.getChildren().addAll(timeRemainingLabel, timeRemainingValueLabel);

      vBox.getChildren().addAll(endDateLabel, durationBox, remainingBox);
      return new SimpleObjectProperty(vBox);
    });

    tableView.setPlaceholder(new Label("                          Try weekly challenges!\n" +
        "Join new challenges by enabling them in the preferences."));
    tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });

    tableView.setRowFactory(tv -> {
      TableRow<WeeklyCompetitionModel> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onEdit();
        }
      });
      return row;
    });

    tableView.setSortPolicy(new Callback<TableView<WeeklyCompetitionModel>, Boolean>() {
      @Override
      public Boolean call(TableView<WeeklyCompetitionModel> gameRepresentationTableView) {
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<WeeklyCompetitionModel, ?> column = gameRepresentationTableView.getSortOrder().get(0);
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

    competitionsController.setCompetition(model.isPresent() ? model.get().competition : null);

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    reloadBtn.setDisable(defaultPlayer == null);

    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 No default player set!\n" +
          "Go to the players section and set the default player for this cabinet!"));
    }
    else {
      tableView.setPlaceholder(new Label("No weekly challenge found.\nClick the '+' button to join one."));
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
      return StringUtils.capitalize(competition.getMode());
    }
  }
}