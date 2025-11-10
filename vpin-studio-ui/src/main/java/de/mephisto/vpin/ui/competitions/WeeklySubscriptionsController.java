package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.IScoredGameCellContainer;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
  private BorderPane competitionWidget;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;

  private CompetitionsController competitionsController;
  private List<CompetitionRepresentation> weeklySubscriptions;

  private boolean active = false;
  private boolean markDirty = false;

  // Add a public no-args constructor
  public WeeklySubscriptionsController() {
  }

  @FXML
  private void onOpenTable(ActionEvent e) {
    WeeklyCompetitionModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      List<GameRepresentation> matches = value.getMatches();
      if (!matches.isEmpty()) {
        NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(matches.get(0).getId()));
      }
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    WeeklyCompetitionModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null) {
      List<GameRepresentation> matches = value.getMatches();
      if (!matches.isEmpty()) {
        TableDialogs.openTableDataDialog(null, matches.get(0));
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

    WeeklyCompetitionModel selection = tableView.getSelectionModel().getSelectedItem();
    WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

    if (forceReload) {
//      ProgressDialog.createProgressDialog(new IScoredGameRoomLoadingProgressModel(iScoredSettings.getGameRooms(), true));
    }

    JFXFuture.supplyAsync(() -> {
      if (this.weeklySubscriptions == null || forceReload) {
        client.getCompetitionService().synchronizeWeeklyCompetitions();
        weeklySubscriptions = client.getCompetitionService().getWeeklyCompetitions();
      }
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
    tableView.setPlaceholder(new Label("No weekly subscription found.\nClick the '+' button to join one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: {}", e.getMessage(), e);
    }

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

      if (value.getMatches().isEmpty()) {
        fallbackLabel.setStyle(ERROR_STYLE);
        fallbackLabel.setText("No matching table found.");
        fallbackLabel.setTooltip(new Tooltip("No matching table found. Download and install this table using the download link."));
        return new SimpleObjectProperty<>(fallbackLabel);
      }

//      if (value.competition == null) {
//        fallbackLabel.setStyle(WidgetFactory.OK_STYLE);
//        if (value.iScoredGameRoom.isSynchronize()) {
//          fallbackLabel.setText("This game is not synchronized yet.");
//          fallbackLabel.setTooltip(new Tooltip("Synchronization is enabled but the competition has not been created yet."));
//        }
//        else {
//          fallbackLabel.setText("Table match found, but not subscribed yet.");
//          fallbackLabel.setTooltip(new Tooltip("The subscription can be create by pressing the \"+\" button."));
//        }
//
//        return new SimpleObjectProperty<>(fallbackLabel);
//      }

      return new SimpleObjectProperty(new IScoredGameCellContainer(value.getMatches(), vpsTable, getLabelCss(cellData.getValue())));
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
      WeeklyCompetitionModel value = cellData.getValue();
      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setStyle(getLabelCss(value));

      if (value.competition == null) {
        return new SimpleObjectProperty<>("-");
      }
      fallbackLabel.setText(DateUtil.formatDateTime(value.competition.getCreatedAt()));
      return new SimpleObjectProperty(fallbackLabel);
    });

    endDateColumn.setCellValueFactory(cellData -> {
      WeeklyCompetitionModel value = cellData.getValue();

      VBox vBox = new VBox(3);

      Label endDateLabel = new Label();
      endDateLabel.getStyleClass().add("default-text");
      endDateLabel.setStyle(getLabelCss(value));
      endDateLabel.setText(DateUtil.formatDateTime(value.competition.getCreatedAt()));

      Label durationLabel = new Label();
      durationLabel.getStyleClass().add("default-text");
      durationLabel.setStyle(getLabelCss(value));
      durationLabel.setText(DateUtil.formatDuration(value.competition.getStartDate(), value.competition.getEndDate()));

      vBox.getChildren().add(endDateLabel);
      vBox.getChildren().add(durationLabel);

      Image image = null;
      try {
        image = new Image(new FileInputStream("C:\\Users\\matth\\Downloads\\flags_png\\as.png"));
      }
      catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
      ImageView v = new ImageView(image);
      v.setPreserveRatio(true);
      v.setFitWidth(100);
      vBox.getChildren().add(v);
      return new SimpleObjectProperty(vBox);
    });

    tableView.setPlaceholder(new Label("                          Try weekly subscriptions!\n" +
        "Create new subscriptions by enabling them in the preferences."));
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
    bindSearchField();
    onViewActivated(null);

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
    if (model.isPresent()) {
      newSelection = model.get();
    }

    tableNavigateBtn.setDisable(model.isEmpty() || model.get().getMatches().isEmpty());
    dataManagerBtn.setDisable(model.isEmpty() || model.get().getMatches().isEmpty());

    competitionsController.setCompetition(model.isPresent() ? model.get().competition : null);

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    reloadBtn.setDisable(defaultPlayer == null);

    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 No default player set!\n" +
          "Go to the players section and set the default player for this cabinet!"));
    }
    else {
      tableView.setPlaceholder(new Label("No weekly subscription found.\nClick the '+' button to join one."));
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
        WeeklyCompetitionModel gameRoomGameModel = getSelection().get();
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
    private final List<GameRepresentation> matches = new ArrayList<>();

    public WeeklyCompetitionModel(CompetitionRepresentation competition) {
      this.competition = competition;
    }

    public String getName() {
      return competition.getName();
    }

    public List<GameRepresentation> getMatches() {
      return matches;
    }

    public String getVpsTableId() {
      return competition.getVpsTableId();
    }

    public String getVpsTableVersionId() {
      return competition.getVpsTableVersionId();
    }
  }
}