package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSavingProgressModel;
import de.mephisto.vpin.ui.competitions.dialogs.GameRoomCellContainer;
import de.mephisto.vpin.ui.competitions.dialogs.IScoredGameCellContainer;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
import de.mephisto.vpin.ui.tournaments.VpsTableContainer;
import de.mephisto.vpin.ui.tournaments.VpsVersionContainer;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredSubscriptionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredSubscriptionsController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, Label> statusColumn;

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
  private Button reloadBtn;

  @FXML
  private TextField textfieldSearch;

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

  private long discordBotId;
  private DiscordBotStatus discordStatus;

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
          onReload();
          if (!resultModel.getResults().isEmpty()) {
            CompetitionRepresentation competitionRepresentation = (CompetitionRepresentation) resultModel.results.get(0);
            tableView.getSelectionModel().select(competitionRepresentation);
            tableView.scrollTo(competitionRepresentation);
          }
        });
      } catch (Exception e) {
        LOG.error("Failed to create iScored subscription: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
    }
  }


  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      String help = "The subscription will be deleted and none of your highscores will be pushed there anymore.";

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete iScored Subscription '" + selection.getName() + "'?",
        help, null, "Delete iScored Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        client.getCompetitionService().deleteCompetition(selection);
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    IScored.invalidate();
    client.clearWheelCache();

    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (!discordStatus.isValid()) {
      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      deleteBtn.setDisable(true);
      reloadBtn.setDisable(true);
      tableView.setVisible(true);
      tableStack.getChildren().remove(loadingOverlay);

      if (competitionWidget != null) {
        competitionWidget.setVisible(false);
      }

      tableView.setItems(FXCollections.emptyObservableList());
      tableView.refresh();
      return;
    }

    new Thread(() -> {
      CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      competitions = client.getCompetitionService().getIScoredSubscriptions();
      filterCompetitions(competitions);
      data = FXCollections.observableList(competitions);

      Platform.runLater(() -> {
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
    }).start();

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("            No IScored subscription found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    } catch (IOException e) {
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
      return new SimpleObjectProperty(new IScoredGameCellContainer(value, getLabelCss(cellData.getValue())));
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      VpsTable vpsTable = value.getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable, getLabelCss(cellData.getValue())));
    });

    vpsTableVersionColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion, getLabelCss(cellData.getValue()), cellData.getValue().getGameId() == 0));
    });

    gameRoomColumn.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRoom gameRoom = IScored.getGameRoom(value.getUrl());
      if(gameRoom == null) {
        return new SimpleObjectProperty<>("Invalid Game Room URL");
      }
      return new SimpleObjectProperty(new GameRoomCellContainer(gameRoom,  getLabelCss(cellData.getValue())));
    });

    tableView.setPlaceholder(new Label("                      Try iScored subscriptions!\n" +
      "Create a new subscription by pressing the '+' button."));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);

      competitionWidgetRoot.managedProperty().bindBidirectional(competitionWidget.visibleProperty());
    } catch (IOException e) {
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
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getVpsTable().getDisplayName()));
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
    onViewActivated();
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
    }

    boolean disable = newSelection == null;
//    boolean isOwner = newSelection != null && newSelection.getOwner().equals(String.valueOf(discordStatus.getBotId()));
    deleteBtn.setDisable(disable);
    reloadBtn.setDisable(this.discordBotId <= 0);
    addBtn.setDisable(this.discordBotId <= 0);

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
  public void onViewActivated() {
    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    this.discordBotId = client.getDiscordService().getDiscordStatus(guildId).getBotId();
    if (this.competitionsController != null) {
      refreshView(Optional.empty());
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
      status = "-fx-font-color: #FF3333;-fx-text-fill:#FF3333;";
    }
    return status;
  }
}