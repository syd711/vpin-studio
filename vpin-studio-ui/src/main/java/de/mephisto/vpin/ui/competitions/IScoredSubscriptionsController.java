package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSavingProgressModel;
import de.mephisto.vpin.ui.competitions.dialogs.GameRoomCellContainer;
import de.mephisto.vpin.ui.competitions.dialogs.IScoredGameCellContainer;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.WaitNProgressModel;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;

public class IScoredSubscriptionsController extends BaseCompetitionController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionsController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> statusColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> visibilityColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> tableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> vpsTableVersionColumn;

  @FXML
  private TableColumn<CompetitionRepresentation, String> gameRoomColumn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button eventLogBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private BorderPane competitionWidget;

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

  private ObservableList<CompetitionRepresentation> data;
  private List<CompetitionRepresentation> competitions;
  private CompetitionsController competitionsController;
  private WaitOverlayController loaderController;

  // Add a public no-args constructor
  public IScoredSubscriptionsController() {
  }

  @FXML
  private void onCompetitionCreate() {
    List<CompetitionRepresentation> result = CompetitionDialogs.openIScoredSubscriptionDialog(this.competitions);
    if (!result.isEmpty()) {
      try {
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Creating Subscriptions", result));
        Platform.runLater(() -> {
          doReload();
          if (!resultModel.getResults().isEmpty()) {
            CompetitionRepresentation competitionRepresentation = (CompetitionRepresentation) resultModel.results.get(0);
            tableView.getSelectionModel().select(competitionRepresentation);
            tableView.scrollTo(competitionRepresentation);
          }
        });
      }
      catch (Exception e) {
        LOG.error("Failed to create iScored subscription: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      doReload();
    }
  }


  @FXML
  private void onEventLog() {
    List<CompetitionRepresentation> selections = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    if (selections.isEmpty()) {
      return;
    }
    CompetitionRepresentation competitionRepresentation = selections.get(0);
    GameRepresentation game = client.getGameService().getGame(competitionRepresentation.getGameId());
    if (game != null && game.isEventLogAvailable()) {
      TableDialogs.openEventLogDialog(game);
    }
  }

  @FXML
  private void onDelete() {
    List<CompetitionRepresentation> selections = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
    if (selections.isEmpty()) {
      return;
    }

    if (selections.size() == 1) {
      CompetitionRepresentation selection = selections.get(0);
      String help = "The subscription will be deleted and none of your highscores will be pushed there anymore.";
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored subscription '" + selection.getName() + "'?",
          help, null, "Delete iScored Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Subscription",
            "Deleting iScored Subscription",
            () -> client.getCompetitionService().deleteCompetition(selection)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        doReload();
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
              client.getCompetitionService().deleteCompetition(selection);
            }));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        doReload();
      }
    }
  }

  @FXML
  public void onReload() {
    IScored.invalidate();
    client.clearWheelCache();
    doReload();
  }
  private void doReload() {
    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();

    JFXFuture.supplyAsync(() -> {
      competitions = client.getCompetitionService().getIScoredSubscriptions();
      return competitions;
    }).thenAcceptLater((competitionRepresentations) -> {
      filterCompetitions(competitions);
      data = FXCollections.observableList(competitions);

      competitionWidget.setVisible(true);
      if (competitions.isEmpty()) {
        competitionWidget.setTop(null);
      }
      else {
        if (competitionWidget.getTop() == null) {
          competitionWidget.setTop(competitionWidgetRoot);
        }
      }

      tableView.setItems(data);
      tableView.refresh();
      if (selection != null && data.contains(selection)) {
        tableView.getSelectionModel().select(selection);
      }
      else if (!data.isEmpty()) {
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
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("         No iScored subscription found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }


    statusColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label();
      if (value.getGameId() == 0) {
        label.setGraphic(WidgetFactory.createExclamationIcon());
      }
      else {
        label.setGraphic(WidgetFactory.createCheckIcon(null));
      }
      return new SimpleObjectProperty<>(label);
    });

    tableColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl());
      VpsTable table = client.getVpsService().getTableById(value.getVpsTableId());
      if (table == null) {
        return new SimpleStringProperty("No matching VPS table found.");
      }

      return new SimpleObjectProperty(new IScoredGameCellContainer(value, table, gameRoom, getLabelCss(cellData.getValue())));
    });

    visibilityColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl());
      IScoredGame gameByVps = gameRoom.getGameByVps(value.getVpsTableId(), value.getVpsTableVersionId());
      Label label = new Label();
      if (gameByVps != null) {
        FontIcon icon = WidgetFactory.createIcon("mdi2e-eye-outline");
        label.setTooltip(new Tooltip("Game is visible"));
        if (gameByVps.isGameHidden()) {
          icon = WidgetFactory.createIcon("mdi2e-eye-off-outline");
          label.setTooltip(new Tooltip("Game is hidden"));
        }
        label.setGraphic(icon);
      }
      return new SimpleObjectProperty<>(label);
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable != null) {
        return new SimpleObjectProperty(new VpsTableContainer(vpsTable, getLabelCss(cellData.getValue())));
      }
      return new SimpleStringProperty("No matching VPS Table found.");
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTableId());
      if (vpsTable == null) {
        return new SimpleStringProperty("No matching VPS Table found.");
      }
      VpsTableVersion vpsTableVersion = vpsTable.getTableVersionById(value.getVpsTableVersionId());
      if (vpsTableVersion == null) {
        return new SimpleStringProperty("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTable, vpsTableVersion, getLabelCss(cellData.getValue()), cellData.getValue().getGameId() == 0));
    });

    gameRoomColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl());
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
      TableRow<CompetitionRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onEdit();
        }
      });
      return row;
    });

    tableView.setSortPolicy(new Callback<TableView<CompetitionRepresentation>, Boolean>() {
      @Override
      public Boolean call(TableView<CompetitionRepresentation> gameRepresentationTableView) {
        CompetitionRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<CompetitionRepresentation, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(tableColumn)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getName()));
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

    validationError.setVisible(false);
    bindSearchField();
    onViewActivated(null);
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      filterCompetitions(this.competitions);
      tableView.setItems(data);
    });
  }

  private void filterCompetitions(List<CompetitionRepresentation> competitions) {
    List<CompetitionRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();
    for (CompetitionRepresentation c : competitions) {
      if (!c.getName().toLowerCase().contains(filterValue.toLowerCase())) {
        continue;
      }

      filtered.add(c);
    }
    data = FXCollections.observableList(filtered);
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {
    validationError.setVisible(false);
    CompetitionRepresentation newSelection = null;
    if (competition.isPresent()) {
      newSelection = competition.get();

      GameRepresentation game = client.getGameService().getGame(newSelection.getGameId());
      eventLogBtn.setDisable(game == null || !game.isEventLogAvailable());
    }

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    deleteBtn.setDisable(defaultPlayer == null);
    reloadBtn.setDisable(defaultPlayer == null);
    addBtn.setDisable(defaultPlayer == null);


    if (defaultPlayer == null) {
      tableView.setPlaceholder(new Label("                                 No default player set!\n" +
          "Go to the players section and set the default player for this cabinet!"));
    }
    else {
      tableView.setPlaceholder(new Label("            No iScored subscription found.\nClick the '+' button to create a new one."));
    }


    if (competition.isPresent()) {
      validationError.setVisible(newSelection.getValidationState().getCode() > 0);

      if (newSelection.getValidationState().getCode() > 0) {
        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection);
        validationErrorLabel.setText(validationResult.getLabel());
        validationErrorText.setText(validationResult.getText());
      }

      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.ISCORED, null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    if (this.competitionsController != null) {
      doReload();
    }
  }

  public void setCompetitionsController(CompetitionsController competitionsController) {
    this.competitionsController = competitionsController;
  }

  public Optional<CompetitionRepresentation> getSelection() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  public static String getLabelCss(CompetitionRepresentation value) {
    String status = "";
    if (value.getValidationState().getCode() > 0) {
      status = ERROR_STYLE;
    }
    else {
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl());
      if (gameRoom != null) {
        IScoredGame gameByVps = gameRoom.getGameByVps(value.getVpsTableId(), value.getVpsTableVersionId());
        if (gameByVps == null) {
          status = ERROR_STYLE;
        }
        else if (gameByVps.isDisabled()) {
          status = WidgetFactory.DISABLED_TEXT_STYLE;
        }
      }
    }
    return status;
  }
}