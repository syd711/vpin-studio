package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSavingProgressModel;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSyncProgressModel;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.WaitProgressModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;

public class TableSubscriptionsController extends BaseCompetitionController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableSubscriptionsController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnCompetitionOwner;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnServer;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button joinBtn;

  @FXML
  private SplitMenuButton validateBtn;

  @FXML
  private MenuItem validateAllBtn;

  @FXML
  private Button clearCacheBtn;

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
  public TableSubscriptionsController() {
  }

  @FXML
  private void onCompetitionValidate() {
    CompetitionRepresentation selectedItem = this.tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize Subscription", "This will re-check your local highscores against the Discord server data.");
      if (result.get().equals(ButtonType.OK)) {
        client.getDiscordService().clearCache();
        client.getDiscordService().checkCompetition(selectedItem);
        this.onReload();
      }
    }
  }

  @FXML
  private void onCompetitionValidateAll() {
    List<CompetitionRepresentation> subscriptions = client.getCompetitionService().getSubscriptions();
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize " + subscriptions.size() + " Subscription(s)?", "This will re-check your local highscores against the Discord server data.");
    if (result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new CompetitionSyncProgressModel("Synchronizing Table Subscriptions", subscriptions));
      this.onReload();
    }
  }

  @FXML
  private void onCompetitionCreate() {
    long guildId = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (discordStatus.getServerId() == 0 || discordStatus.getCategoryId() == 0) {
      WidgetFactory.showAlert(Studio.stage, "Invalid Discord Configuration", "No default Discord server and category for subscriptions found.", "Open the Bot Settings in the preferences to configure the subscription settings.");
      return;
    }

    CompetitionRepresentation c = CompetitionDialogs.openSubscriptionDialog(this.competitions, null);
    if (c != null) {
      CompetitionRepresentation newCmp = null;
      try {
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Creating Subscription", Arrays.asList(c)));
        Platform.runLater(() -> {
          onReload();
          tableView.getSelectionModel().select((CompetitionRepresentation) resultModel.results.get(0));
        });
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
      tableView.getSelectionModel().select(newCmp);
    }
  }

  @FXML
  private void onJoin() {
    client.clearDiscordCache();
    CompetitionRepresentation c = CompetitionDialogs.openJoinSubscriptionDialog(this.competitions);
    if (c != null) {
      try {
        CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(c);
        onReload();
        GameRepresentation game = client.getGameService().getGame(c.getGameId());
        if (game != null) {
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        }
        tableView.getSelectionModel().select(newCmp);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
    else {
      onReload();
    }
  }

  @FXML
  private void onClearCache() {
    clearCacheBtn.setDisable(true);
    client.getDiscordService().clearCache();
    clearCacheBtn.setDisable(false);

    onReload();
  }


  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      boolean isOwner = selection.getOwner().equals(String.valueOf(client.getDiscordService().getDiscordStatus(selection.getDiscordServerId()).getBotId()));
      String help = "You are the owner of this subscription.";
      String help2 = "The subscription and the corresponding channel will be deleted.";

      if (!isOwner) {
        help = "You are not the owner of this subscription.";
        help2 = "The subscription will be deleted and none of your highscores will be pushed there anymore.";
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Subscription '" + selection.getName() + "'?",
          help, help2, "Delete Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Subscription",
            "Deleting Subscription " + selection.getName(),
            () -> client.getCompetitionService().deleteCompetition(selection)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    client.getFrontendService().clearCache();

    tableView.setVisible(false);

    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    long guildId = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (discordStatus.getError() != null) {
      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      deleteBtn.setDisable(true);
      validateBtn.setDisable(true);
      reloadBtn.setDisable(true);
      joinBtn.setDisable(true);

      tableView.setPlaceholder(new Label("                                         No Discord bot found.\nCreate a Discord bot and add it in the preference section \"Discord Preferences\"."));
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
      competitions = client.getCompetitionService().getSubscriptions();
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
    super.initialize();
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      try {
        CompetitionRepresentation value = cellData.getValue();
        Label label = new Label(value.getName());
        label.getStyleClass().add("default-text");
        label.setStyle(getLabelCss(value));
        return new SimpleObjectProperty(label);
      }
      catch (Exception e) {
        LOG.error("Failed to render table column: {}", e.getMessage(), e);
        return new SimpleObjectProperty(new Label("Error: " + e.getMessage()));
      }
    });


    columnTable.setCellValueFactory(cellData -> {
      try {
        CompetitionRepresentation value = cellData.getValue();
        GameRepresentation game = client.getGameService().getGameCached(value.getGameId());
        Label label = new Label("- not available anymore -");
        label.getStyleClass().add("default-text");
        label.setStyle(getLabelCss(value));

        Image image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        if (game != null) {
          label = new Label(game.getGameDisplayName());
          label.getStyleClass().add("default-text");
          ByteArrayInputStream gameMediaItem = ServerFX.client.getWheelIcon(game.getId(), true);
          if (gameMediaItem != null) {
            image = new Image(gameMediaItem);
          }
        }

        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);

        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setFitWidth(60);
        view.setFitHeight(60);
        hBox.getChildren().addAll(view, label);

        return new SimpleObjectProperty(hBox);
      }
      catch (Exception e) {
        LOG.error("Failed to render table column: {}", e.getMessage(), e);
        return new SimpleObjectProperty(new Label("Error: " + e.getMessage()));
      }
    });

    columnServer.setCellValueFactory(cellData -> {
      try {
        CompetitionRepresentation value = cellData.getValue();

        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);

        DiscordServer discordServer = client.getDiscordService().getDiscordServer(value.getDiscordServerId());
        if (discordServer != null) {
          String avatarUrl = discordServer.getAvatarUrl();
          Image image = null;
          if (avatarUrl == null) {
            image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
          }
          else {
            image = new Image(avatarUrl);
          }
          ImageView view = new ImageView(image);
          view.setPreserveRatio(true);
          view.setFitWidth(50);
          view.setFitHeight(50);

          CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          Label label = new Label(discordServer.getName());
          label.getStyleClass().add("default-text");
          label.setStyle(getLabelCss(value));
          hBox.getChildren().addAll(view, label);
        }

        return new SimpleObjectProperty(hBox);
      }
      catch (Exception e) {
        LOG.error("Failed to render table column: {}", e.getMessage(), e);
        return new SimpleObjectProperty(new Label("Error: " + e.getMessage()));
      }
    });

    columnCompetitionOwner.setCellValueFactory(cellData -> {
      try {
        CompetitionRepresentation value = cellData.getValue();

        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);
        PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(value.getDiscordServerId(), Long.valueOf(value.getOwner()));
        if (discordPlayer != null) {
          InputStream cachedUrlImage = client.getCachedUrlImage(discordPlayer.getAvatarUrl());
          if (cachedUrlImage == null) {
            cachedUrlImage = Studio.class.getResourceAsStream("avatar-blank.png");
          }
          Image image = new Image(cachedUrlImage);
          ImageView view = new ImageView();
          view.setPreserveRatio(true);
          view.setFitWidth(50);
          view.setFitHeight(50);
          CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));

          Label label = new Label(discordPlayer.getName());
          label.getStyleClass().add("default-text");
          label.setStyle(getLabelCss(value));
          hBox.getChildren().addAll(view, label);
        }

        return new SimpleObjectProperty(hBox);
      }
      catch (Exception e) {
        LOG.error("Failed to render table column: {}", e.getMessage(), e);
        return new SimpleObjectProperty(new Label("Error: " + e.getMessage()));
      }
    });

    tableView.setPlaceholder(new Label("                      Try table subscriptions!\n" +
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

    columnServer.setSortable(false);
    columnCompetitionOwner.setSortable(false);

    tableView.setSortPolicy(new Callback<TableView<CompetitionRepresentation>, Boolean>() {
      @Override
      public Boolean call(TableView<CompetitionRepresentation> gameRepresentationTableView) {
        CompetitionRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
        if (!gameRepresentationTableView.getSortOrder().isEmpty()) {
          TableColumn<CompetitionRepresentation, ?> column = gameRepresentationTableView.getSortOrder().get(0);
          if (column.equals(columnName)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getName()));
            if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
              Collections.reverse(tableView.getItems());
            }
            return true;
          }
          else if (column.equals(columnTable)) {
            Collections.sort(tableView.getItems(), Comparator.comparing(o -> client.getGameService().getGameCached(o.getGameId()).getGameDisplayName()));
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
    }

    validateBtn.setDisable(competition.isEmpty() || this.discordBotId <= 0);

    boolean disable = newSelection == null;
//    boolean isOwner = newSelection != null && newSelection.getOwner().equals(String.valueOf(discordStatus.getBotId()));
    deleteBtn.setDisable(disable);
    reloadBtn.setDisable(this.discordBotId <= 0);
    addBtn.setDisable(this.discordBotId <= 0);
    joinBtn.setDisable(this.discordBotId <= 0);

    if (competition.isPresent()) {
      validationError.setVisible(newSelection.getValidationState().getCode() > 0);

      if (!validateBtn.isDisabled()) {
        validateBtn.setDisable(newSelection.getValidationState().getCode() > 0);
      }

      if (newSelection.getValidationState().getCode() > 0) {
        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection);
        validationErrorLabel.setText(validationResult.getLabel());
        validationErrorText.setText(validationResult.getText());
      }

      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.SUBSCRIPTION, competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.SUBSCRIPTION, null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    long guildId = client.getPreferenceService().getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
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
      status = ERROR_STYLE;
    }
    return status;
  }
}