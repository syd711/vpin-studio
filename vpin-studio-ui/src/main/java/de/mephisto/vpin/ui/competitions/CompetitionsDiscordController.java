package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSavingProgressModel;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionSyncProgressModel;
import de.mephisto.vpin.ui.competitions.validation.CompetitionValidationTexts;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsDiscordController extends BaseCompetitionController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsDiscordController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStatus;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnCompetitionOwner;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnServer;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnChannel;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnStartDate;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnEndDate;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnWinner;

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button finishBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button clearCacheBtn;

  @FXML
  private SplitMenuButton validateBtn;

  @FXML
  private MenuItem validateAllBtn;

  @FXML
  private Button joinBtn;

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

  // Add a public no-args constructor
  public CompetitionsDiscordController() {
  }

  @FXML
  private void onCompetitionValidate() {
    CompetitionRepresentation selectedItem = this.tableView.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize Competition", "This will re-check your local highscores against the Discord server data.");
      if (result.get().equals(ButtonType.OK)) {
        client.getDiscordService().clearCache();
        client.getDiscordService().checkCompetition(selectedItem);
        this.onReload();
      }
    }
  }

  @FXML
  private void onCompetitionValidateAll() {
    List<CompetitionRepresentation> competitionRepresentations = client.getCompetitionService().getDiscordCompetitions().stream().filter(d -> !d.isFinished()).collect(Collectors.toList());
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Synchronize " + competitionRepresentations.size() + " Competitions?", "This will re-check your local highscores against the Discord server data.");
    if (result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new CompetitionSyncProgressModel("Synchronizing Competition", competitionRepresentations));
      this.onReload();
    }
  }

  @FXML
  private void onCompetitionCreate() {
    client.getDiscordService().clearCache();
    CompetitionRepresentation c = CompetitionDialogs.openDiscordCompetitionDialog(this.competitions, null);
    if (c != null) {
      try {
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Creating Competition", Arrays.asList(c)));
        Platform.runLater(() -> {
          Platform.runLater(() -> {
            onReload();
            tableView.getSelectionModel().select((CompetitionRepresentation) resultModel.results.get(0));
          });
        });
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
  }

  @FXML
  private void onDuplicate() {
    client.getDiscordService().clearCache();
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      CompetitionRepresentation clone = selection.cloneCompetition();
      CompetitionRepresentation c = CompetitionDialogs.openDiscordCompetitionDialog(this.competitions, clone);
      if (c != null) {
        try {
          ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Creating Competition", Arrays.asList(c)));
          Platform.runLater(() -> {
            onReload();
            tableView.getSelectionModel().select((CompetitionRepresentation) resultModel.results.get(0));
          });
        }
        catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, e.getMessage());
        }
      }
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
  private void onJoin() {
    client.clearDiscordCache();
    CompetitionRepresentation c = CompetitionDialogs.openDiscordJoinCompetitionDialog();
    if (c != null) {
      try {
        ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new CompetitionSavingProgressModel("Joining Competition", Arrays.asList(c)));
        onReload();
        tableView.getSelectionModel().select((CompetitionRepresentation) resultModel.results.get(0));
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
  private void onEdit() {
    client.getDiscordService().clearCache();
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      CompetitionRepresentation c = CompetitionDialogs.openDiscordCompetitionDialog(this.competitions, selection);
      if (c != null) {
        try {
          CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(c);
          onReload();
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
  }

  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      boolean isOwner = selection.getOwner().equals(String.valueOf(client.getDiscordService().getDiscordStatus(selection.getDiscordServerId()).getBotId()));

      String help = null;
      String help2 = null;
      String remainingDayMsg = selection.remainingDays() == 1 ? "The competition is active for another day." :
          "The competition is still active for another " + selection.remainingDays() + " days.";

      if (isOwner && selection.isActive()) {
        help = remainingDayMsg;
        help2 = "This will cancel the competition, no winner will be announced.";
      }
      else if (!isOwner && selection.isActive()) {
        help = "You are a member of this competition. The competition information will be removed from your VPin.";
        help2 = remainingDayMsg;
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Competition '" + selection.getName() + "'?",
          help, help2);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        ProgressDialog.createProgressDialog(new WaitProgressModel<>("Delete Competition", 
          "Deleting Competition " + selection.getName(), 
          () -> client.getCompetitionService().deleteCompetition(selection)));
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
        onReload();
      }
    }
  }

  @FXML
  private void onFinish() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null && selection.isActive()) {
      String helpText1 = "The competition is active for another " + selection.remainingDays() + " days.";
      String helpText2 = "Finishing the competition will set the current leader as winner.";

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Finish Competition '" + selection.getName() + "'?", helpText1, helpText2);
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getCompetitionService().finishCompetition(selection);
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    DiscordBotStatus discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (!discordStatus.isValid()) {
      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      editBtn.setDisable(true);
      validateBtn.setDisable(true);
      deleteBtn.setDisable(true);
      duplicateBtn.setDisable(true);
      finishBtn.setDisable(true);
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
      competitions = client.getCompetitionService().getDiscordCompetitions();
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
        if (selection != null) {
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
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(value.getName());
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });


    columnTable.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRepresentation game = client.getGame(value.getGameId());
      Label label = new Label("- not available anymore -");
      if (game != null) {
        label = new Label(game.getGameDisplayName());
      }
      label.setStyle(getLabelCss(value));

      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);

      InputStream gameMediaItem = ServerFX.client.getGameMediaItem(value.getGameId(), VPinScreen.Wheel);
      if (gameMediaItem == null) {
        gameMediaItem = Studio.class.getResourceAsStream("avatar-blank.png");
      }

      Image image = new Image(gameMediaItem);
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setSmooth(true);
      view.setFitWidth(60);
      view.setFitHeight(60);
      hBox.getChildren().addAll(view, label);

      return new SimpleObjectProperty(hBox);
    });

    columnServer.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      DiscordServer discordServer = client.getDiscordServer(value.getDiscordServerId());
      if (discordServer != null) {
        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);

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
        label.setStyle(getLabelCss(value));
        hBox.getChildren().addAll(view, label);

        return new SimpleObjectProperty(hBox);
      }

      Label label = new Label("- Not Found -");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnChannel.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(value.getDiscordServerId());
      Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == value.getDiscordChannelId()).findFirst();
      String status = "- Not Found -";
      if (first.isPresent()) {
        status = first.get().getName();
      }
      Label label = new Label(status);
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnCompetitionOwner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();

      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);
      PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(value.getDiscordServerId(), Long.valueOf(value.getOwner()));
      if (discordPlayer != null) {
        Image image = new Image(client.getCachedUrlImage(discordPlayer.getAvatarUrl()));
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(50);
        view.setFitHeight(50);
        CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));

        Label label = new Label(discordPlayer.getName());
        label.setStyle(getLabelCss(value));
        hBox.getChildren().addAll(view, label);
      }

      return new SimpleObjectProperty(hBox);
    });

    columnStatus.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String status = "FINISHED";
      if (value.getValidationState().getCode() > 0) {
        status = "INVALID";
      }
      else if (value.isActive()) {
        status = "ACTIVE";
      }
      else if (value.isPlanned()) {
        status = "PLANNED";
      }

      Label label = new Label(status);
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnStartDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getStartDate()));
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnEndDate.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(DateFormat.getDateTimeInstance().format(value.getEndDate()));
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty(label);
    });

    columnWinner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      String winner = "-";

      if (!StringUtils.isEmpty(value.getWinnerInitials())) {
        winner = value.getWinnerInitials();
        PlayerRepresentation player = client.getPlayerService().getPlayer(value.getDiscordServerId(), value.getWinnerInitials());
        if (player != null) {
          winner = player.getName();
        }
      }
      return new SimpleObjectProperty(winner);
    });

    tableView.setPlaceholder(new Label("            Mmmh, not up for a challenge yet?\n" +
        "Create a new competition by pressing the '+' button."));
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
        if (event.getClickCount() == 2 && (!row.isEmpty()) && !editBtn.isDisabled()) {
          onEdit();
        }
      });
      return row;
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

  private String getLabelCss(CompetitionRepresentation value) {
    String status = "";
    if (value.getValidationState().getCode() > 0) {
      status = ERROR_STYLE;
    }
    else if (value.isActive()) {
      status = "-fx-font-color: #33CC00;-fx-text-fill:#33CC00;";
    }
    else if (value.isPlanned()) {
      status = "";
    }
    return status;
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {
    validationError.setVisible(false);
    CompetitionRepresentation newSelection = null;
    if (competition.isPresent()) {
      newSelection = competition.get();
    }

    boolean disable = newSelection == null;
    boolean isOwner = newSelection != null && newSelection.getOwner().equals(String.valueOf(this.discordBotId));
    editBtn.setDisable(disable || !isOwner || newSelection.isFinished());
    validateBtn.setDisable(competition.isEmpty() || competition.get().isFinished());
    finishBtn.setDisable(disable || !isOwner || !newSelection.isActive());
    deleteBtn.setDisable(disable);
    duplicateBtn.setDisable(disable || !isOwner);
    reloadBtn.setDisable(this.discordBotId <= 0);
    addBtn.setDisable(this.discordBotId <= 0);
    joinBtn.setDisable(this.discordBotId <= 0);

    if (competition.isPresent()) {
      if (!editBtn.isDisabled()) {
        editBtn.setDisable(newSelection.getValidationState().getCode() > 0);
      }
      if (!validateBtn.isDisabled()) {
        validateBtn.setDisable(newSelection.getValidationState().getCode() > 0);
      }

      validationError.setVisible(newSelection.getValidationState().getCode() > 0);
      if (newSelection.getValidationState().getCode() > 0) {
        LocalizedValidation validationResult = CompetitionValidationTexts.getValidationResult(newSelection);
        validationErrorLabel.setText(validationResult.getLabel());
        validationErrorText.setText(validationResult.getText());
      }

      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.DISCORD, competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.DISCORD, null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    JFXFuture.runAsync(() -> {
      long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
      this.discordBotId = client.getDiscordService().getDiscordStatus(guildId).getBotId();
    }).thenLater(() -> {
      if (this.competitionsController != null) {
        refreshView(Optional.empty());
      }
    });
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
}