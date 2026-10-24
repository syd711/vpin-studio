package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengeOptOut;
import de.mephisto.vpin.connectors.mania.model.DiscordChallengesResponse;
import de.mephisto.vpin.connectors.mania.model.DiscordChannel;
import de.mephisto.vpin.connectors.mania.model.DiscordServer;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.VpsGameCellContainer;
import de.mephisto.vpin.ui.mania.ManiaSettingsController;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.vps.VpsTableContainer;
import javafx.beans.property.SimpleObjectProperty;
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
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.*;

import static de.mephisto.vpin.commons.utils.WidgetFactory.DISABLED_TEXT_STYLE;
import static de.mephisto.vpin.commons.utils.WidgetFactory.ERROR_STYLE;
import static de.mephisto.vpin.commons.utils.WidgetFactory.OK_STYLE;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

/**
 * Tab for the VPin Mania Discord challenges the user takes part in.
 * Challenges are created on Discord servers with the VPin Mania bot, so this tab is only available
 * for cabinets registered to VPin Mania whose user account is linked to Discord.
 */
public class ManiaChallengesController extends BaseCompetitionController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaChallengesController.class);

  @FXML
  private TableView<ManiaChallengeModel> tableView;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> tableColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> statusColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> vpsTableColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> channelColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> scoreColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> startDateColumn;

  @FXML
  private TableColumn<ManiaChallengeModel, Object> endDateColumn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button stopBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button eventLogBtn;

  @FXML
  private MenuButton participationBtn;

  @FXML
  private MenuItem optOutChannelItem;

  @FXML
  private MenuItem optOutServerItem;

  @FXML
  private MenuItem optInChannelItem;

  @FXML
  private MenuItem optInServerItem;

  @FXML
  private StackPane tableStack;

  @FXML
  private ToolBar toolbar;

  @FXML
  private HBox warningBox;

  @FXML
  private Label submitScoresWarningLabel;

  @FXML
  private Label participationWarningLabel;

  private Parent loadingOverlay;

  private PlayButtonController playButtonController;
  private CompetitionsController competitionsController;
  private List<ManiaChallengeModel> challenges = new ArrayList<>();

  // Add a public no-args constructor
  public ManiaChallengesController() {
  }

  /**
   * The challenges tab is only available if the cabinet is registered to VPin Mania and the user account is linked to Discord.
   * Executes REST calls, so do not call this on the FX thread.
   */
  public static boolean isAvailable() {
    try {
      if (ManiaHelper.isRegistered()) {
        User currentUser = maniaClient.getUserClient().getCurrentUser();
        return currentUser != null && currentUser.isDiscordLinked();
      }
    }
    catch (Exception e) {
      LOG.warn("VPin Mania Discord link check failed: {}", e.getMessage());
    }
    return false;
  }

  @FXML
  public void onStop() {
    Dialogs.killFrontend();
  }

  @FXML
  private void onEventLog(ActionEvent e) {
    ManiaChallengeModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null && value.getGame() != null && value.getGame().isEventLogAvailable()) {
      TableDialogs.openEventLogDialog(value.getGame());
    }
  }

  @FXML
  private void onOpenTable(ActionEvent e) {
    ManiaChallengeModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null && value.getGame() != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(value.getGame().getId()));
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    ManiaChallengeModel value = tableView.getSelectionModel().getSelectedItem();
    if (value != null && value.getGame() != null) {
      TableDialogs.openTableDataDialog(null, value.getGame());
    }
  }

  @FXML
  private void onOptOutChannel() {
    getSelection().ifPresent(model -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
          Messages.get("competitions.competitions_mania.opt_out_channel_confirm", model.getChannelName()),
          Messages.get("competitions.competitions_mania.opt_out_help"),
          Messages.get("competitions.competitions_mania.opt_out_help2"));
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        updateParticipation(model.getChallenge().getGuildId(), model.getChallenge().getChannelId(), true);
      }
    });
  }

  @FXML
  private void onOptOutServer() {
    getSelection().ifPresent(model -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
          Messages.get("competitions.competitions_mania.opt_out_server_confirm", model.getServerName()),
          Messages.get("competitions.competitions_mania.opt_out_help"),
          Messages.get("competitions.competitions_mania.opt_out_help2"));
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        updateParticipation(model.getChallenge().getGuildId(), null, true);
      }
    });
  }

  @FXML
  private void onOptInChannel() {
    getSelection().ifPresent(model -> updateParticipation(model.getChallenge().getGuildId(), model.getChallenge().getChannelId(), false));
  }

  @FXML
  private void onOptInServer() {
    getSelection().ifPresent(model -> updateParticipation(model.getChallenge().getGuildId(), null, false));
  }

  private void updateParticipation(@NonNull String guildId, @Nullable String channelId, boolean optOut) {
    participationBtn.setDisable(true);
    JFXFuture.supplyAsync(() -> {
      try {
        if (optOut) {
          return maniaClient.getDiscordChallengeClient().optOut(guildId, channelId);
        }
        return maniaClient.getDiscordChallengeClient().optIn(guildId, channelId);
      }
      catch (Exception e) {
        throw new JFXFuture.JFXRuntimeException(e.getMessage(), e);
      }
    }).thenAcceptLater(response -> {
      LOG.info("Updated VPin Mania challenge participation for guild {}, channel {}: opt-out={}", guildId, channelId, optOut);
      doReload();
    }).onErrorLater(e -> {
      LOG.error("Failed to update challenge participation: {}", e.getMessage(), e);
      participationBtn.setDisable(false);
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("competitions.competitions_mania.participation_update_failed"), e.getMessage());
    });
  }

  @FXML
  private void onOpenSettings() {
    // the challenge participation is part of the Discord settings, the score submission of the privacy settings
    if (participationWarningLabel.isVisible()) {
      ManiaSettingsController.open("mania-discord-settings.fxml", "discordSettingsBtn");
    }
    else {
      ManiaSettingsController.open("mania-privacy-settings.fxml", "privacySettingsBtn");
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();
    doReload();
  }

  public void doReload() {
    tableView.setVisible(false);
    if (!tableStack.getChildren().contains(loadingOverlay)) {
      tableStack.getChildren().add(loadingOverlay);
    }

    ManiaChallengeModel selection = tableView.getSelectionModel().getSelectedItem();
    JFXFuture.supplyAsync(this::loadChallenges)
        .thenAcceptLater(result -> {
          List<ManiaChallengeModel> models = result.models();
          this.challenges = models;
          filterChallenges();
          refreshWarnings(result.submitAllScores(), result.participating());

          Optional<ManiaChallengeModel> reselect = selection != null ? models.stream().filter(m -> m.getId() == selection.getId()).findFirst() : Optional.empty();
          if (reselect.isPresent()) {
            tableView.getSelectionModel().select(reselect.get());
          }
          else if (!tableView.getItems().isEmpty()) {
            tableView.getSelectionModel().select(0);
          }
          else {
            refreshView(Optional.empty());
          }

          tableStack.getChildren().remove(loadingOverlay);
          tableView.setVisible(true);
        })
        .onErrorLater(e -> {
          LOG.error("Failed to load VPin Mania challenges: {}", e.getMessage(), e);
          this.challenges = new ArrayList<>();
          filterChallenges();
          refreshView(Optional.empty());
          tableStack.getChildren().remove(loadingOverlay);
          tableView.setVisible(true);
        });
  }

  private void refreshWarnings(boolean submitAllScores, boolean participating) {
    submitScoresWarningLabel.setVisible(!submitAllScores);
    submitScoresWarningLabel.setManaged(!submitAllScores);
    participationWarningLabel.setVisible(!participating);
    participationWarningLabel.setManaged(!participating);
    warningBox.setVisible(!submitAllScores || !participating);
    warningBox.setManaged(warningBox.isVisible());
  }

  private ChallengesLoadResult loadChallenges() {
    // scores are only submitted to VPin Mania (and so to the challenges) if all highscores are submitted
    ManiaSettings maniaSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
    boolean submitAllScores = maniaSettings.isSubmitAllScores();

    // the participation in all challenges can be disabled in the user settings on the VPin Mania website
    boolean participating = true;
    try {
      User currentUser = maniaClient.getUserClient().getCurrentUser();
      participating = currentUser == null || currentUser.isParticipatingInDiscordChallenges();
    }
    catch (Exception e) {
      LOG.warn("Failed to load VPin Mania user settings: {}", e.getMessage());
    }

    DiscordChallengesResponse response = maniaClient.getDiscordChallengeClient().getChallenges();

    List<DiscordServer> servers = Collections.emptyList();
    try {
      servers = maniaClient.getDiscordChallengeClient().getServers();
    }
    catch (Exception e) {
      // only used to resolve the server and channel names
      LOG.warn("Failed to load Discord servers of VPin Mania user: {}", e.getMessage());
    }

    List<ManiaChallengeModel> models = new ArrayList<>();
    for (DiscordChallenge challenge : response.getChallenges()) {
      models.add(new ManiaChallengeModel(challenge, servers, response.getOptOuts(), !participating));
    }
    return new ChallengesLoadResult(models, submitAllScores, participating);
  }

  private record ChallengesLoadResult(List<ManiaChallengeModel> models, boolean submitAllScores, boolean participating) {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    tableView.setPlaceholder(new Label(Messages.get("competitions.competitions_mania.no_challenges_found")));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loader.setResources(Messages.getBundle());
      loadingOverlay = loader.load();
      WaitOverlayController loaderController = loader.getController();
      loaderController.setLoadingMessage(Messages.get("competitions.competitions_mania.loading_challenges"));
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: {}", e.getMessage(), e);
    }

    tableColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      VpsTable vpsTable = value.getVpsTableId() != null ? client.getVpsService().getTableById(value.getVpsTableId()) : null;
      if (value.getGame() != null && vpsTable != null) {
        VpsGameCellContainer gameCell = new VpsGameCellContainer(Arrays.asList(value.getGame()), vpsTable, getLabelCss(value));
        if (!value.isVersionMismatch()) {
          return new SimpleObjectProperty<>(gameCell);
        }

        // the challenge is bound to a table version, scores of other versions are ignored by VPin Mania
        Label mismatchLabel = new Label(Messages.get("competitions.competitions_mania.table_version_mismatch"));
        mismatchLabel.getStyleClass().add("default-text");
        // no text wrapping inside the VBox, it would blow up the row height, the full text is shown as tooltip
        mismatchLabel.setTooltip(new Tooltip(mismatchLabel.getText()));
        mismatchLabel.setStyle(ERROR_STYLE);
        return new SimpleObjectProperty<>(new VBox(3, gameCell, mismatchLabel));
      }

      Label fallbackLabel = new Label();
      fallbackLabel.getStyleClass().add("default-text");
      fallbackLabel.setWrapText(true);
      fallbackLabel.setPrefWidth(300);
      fallbackLabel.setStyle(ERROR_STYLE);
      if (vpsTable == null) {
        fallbackLabel.setText(Messages.get("competitions.competitions_weekly.no_matching_vps_table_found"));
      }
      else {
        fallbackLabel.setText(Messages.get("competitions.competitions_iscored.no_matching_table_found"));
      }
      return new SimpleObjectProperty<>(fallbackLabel);
    });

    statusColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      Label label = new Label(value.getStatusText());
      label.getStyleClass().add("default-text");
      if (value.isOptedOut()) {
        label.setStyle(ERROR_STYLE);
        if (value.isParticipationDisabled()) {
          // kept on a single line to not increase the row height, the reason is shown as tooltip
          label.setTooltip(new Tooltip(Messages.get("competitions.competitions_mania.warning_participation_disabled")));
        }
      }
      else if (value.getChallenge().isRunning()) {
        label.setStyle(OK_STYLE);
      }
      else {
        label.setStyle(DISABLED_TEXT_STYLE);
      }
      return new SimpleObjectProperty<>(label);
    });

    vpsTableColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      VpsTable vpsTable = value.getVpsTableId() != null ? client.getVpsService().getTableById(value.getVpsTableId()) : null;
      Label versionLabel = new Label(StringUtils.defaultIfEmpty(value.getChallenge().getVersionLabel(),
          value.getChallenge().getVpsVersionId() != null ? "-" : Messages.get("competitions.competitions_mania.any_table_version")));
      versionLabel.getStyleClass().add("default-text");
      versionLabel.setTooltip(new Tooltip(versionLabel.getText()));
      versionLabel.setStyle(value.isVersionMismatch() ? ERROR_STYLE : getLabelCss(value));
      if (vpsTable != null) {
        return new SimpleObjectProperty<>(new VBox(3, new VpsTableContainer(vpsTable, getLabelCss(value)), versionLabel));
      }
      Label label = new Label(StringUtils.defaultString(value.getChallenge().getTableName(), "-"));
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return new SimpleObjectProperty<>(new VBox(3, label, versionLabel));
    });

    channelColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      VBox vBox = new VBox(3);
      Label serverLabel = new Label(value.getServerName());
      serverLabel.getStyleClass().add("default-text");
      serverLabel.setStyle(getLabelCss(value));
      Label channelLabel = new Label("#" + value.getChannelName());
      channelLabel.getStyleClass().add("default-text");
      channelLabel.setStyle(getLabelCss(value));
      vBox.getChildren().addAll(serverLabel, channelLabel);
      return new SimpleObjectProperty<>(vBox);
    });

    scoreColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      VBox vBox = new VBox(3);
      vBox.setAlignment(Pos.CENTER);
      DiscordChallenge challenge = value.getChallenge();

      Label scoreLabel = new Label(value.getScoreText());
      scoreLabel.getStyleClass().add("default-text");
      scoreLabel.setStyle(getLabelCss(value));
      vBox.getChildren().add(scoreLabel);

      Label positionLabel = new Label(challenge.getPosition() != null
          ? Messages.get("competitions.competitions_mania.position", challenge.getPosition(), challenge.getParticipants())
          : Messages.get("competitions.competitions_mania.participants", challenge.getParticipants()));
      positionLabel.getStyleClass().add("default-text");
      positionLabel.setStyle(getLabelCss(value));
      vBox.getChildren().add(positionLabel);
      return new SimpleObjectProperty<>(vBox);
    });

    startDateColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      Label dateLabel = new Label(value.getStartDate() != null ? DateUtil.formatDateTime(value.getStartDate()) : "-");
      dateLabel.getStyleClass().add("default-text");
      dateLabel.setStyle(getLabelCss(value));
      return new SimpleObjectProperty<>(dateLabel);
    });

    endDateColumn.setCellValueFactory(cellData -> {
      ManiaChallengeModel value = cellData.getValue();
      VBox vBox = new VBox(3);

      Label endDateLabel = new Label();
      endDateLabel.getStyleClass().add("default-text");
      endDateLabel.setStyle(getLabelCss(value));
      vBox.getChildren().add(endDateLabel);

      if (value.getEndDate() == null) {
        endDateLabel.setText(Messages.get("competitions.competitions_mania.open_ended"));
        return new SimpleObjectProperty<>(vBox);
      }
      endDateLabel.setText(DateUtil.formatDateTime(value.getEndDate()));

      if (value.getChallenge().isRunning()) {
        Label timeRemainingLabel = new Label(Messages.get("competitions.competitions_weekly.remaining"));
        timeRemainingLabel.getStyleClass().add("default-text");
        timeRemainingLabel.setStyle(getLabelCss(value));
        Label timeRemainingValueLabel = new Label(DateUtil.formatDuration(Instant.now(), value.getEndDate()));
        timeRemainingValueLabel.getStyleClass().add("default-text");
        timeRemainingValueLabel.setStyle(getLabelCss(value));
        vBox.getChildren().addAll(timeRemainingLabel, timeRemainingValueLabel);
      }
      return new SimpleObjectProperty<>(vBox);
    });

    tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
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

    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());
      filterChallenges();
    });

    Frontend frontend = client.getFrontendService().getFrontend();
    FrontendUtil.replaceName(stopBtn.getTooltip(), frontend);

    refreshView(Optional.empty());
  }

  private void filterChallenges() {
    String filterValue = textfieldSearch.getText().toLowerCase();
    List<ManiaChallengeModel> filtered = new ArrayList<>();
    for (ManiaChallengeModel model : challenges) {
      if (model.getName().toLowerCase().contains(filterValue)
          || model.getServerName().toLowerCase().contains(filterValue)
          || model.getChannelName().toLowerCase().contains(filterValue)) {
        filtered.add(model);
      }
    }
    tableView.setItems(FXCollections.observableList(filtered));
    tableView.refresh();
  }

  private void refreshView(Optional<ManiaChallengeModel> model) {
    GameRepresentation game = model.map(ManiaChallengeModel::getGame).orElse(null);
    playButtonController.setData(game);

    eventLogBtn.setDisable(game == null || !game.isEventLogAvailable());
    tableNavigateBtn.setDisable(game == null);
    dataManagerBtn.setDisable(game == null);

    participationBtn.setDisable(model.isEmpty());
    if (model.isPresent()) {
      ManiaChallengeModel m = model.get();
      optOutChannelItem.setText(Messages.get("competitions.competitions_mania.opt_out_channel", m.getChannelName()));
      optOutServerItem.setText(Messages.get("competitions.competitions_mania.opt_out_server", m.getServerName()));
      optInChannelItem.setText(Messages.get("competitions.competitions_mania.opt_in_channel", m.getChannelName()));
      optInServerItem.setText(Messages.get("competitions.competitions_mania.opt_in_server", m.getServerName()));

      optOutChannelItem.setVisible(!m.isChannelOptedOut() && !m.isServerOptedOut());
      optInChannelItem.setVisible(m.isChannelOptedOut());
      optOutServerItem.setVisible(!m.isServerOptedOut());
      optInServerItem.setVisible(m.isServerOptedOut());
    }

    if (competitionsController != null) {
      competitionsController.setCompetition(model.map(ManiaChallengeModel::getCompetition).orElse(null));
    }
  }

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      getSelection().ifPresent(model -> {
        if (model.getGame() != null) {
          TableDialogs.openTableDataDialog(null, model.getGame());
        }
      });
    }
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

  public Optional<ManiaChallengeModel> getSelection() {
    return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
  }

  public static String getLabelCss(ManiaChallengeModel model) {
    if (model.isOptedOut()) {
      return DISABLED_TEXT_STYLE;
    }
    return "";
  }

  static class ManiaChallengeModel {
    private final DiscordChallenge challenge;
    private final CompetitionRepresentation competition;
    private final GameRepresentation game;
    private String serverName;
    private String serverIconUrl;
    private String channelName;
    private final boolean serverOptedOut;
    private final boolean channelOptedOut;
    private final boolean participationDisabled;
    private final boolean versionMismatch;

    ManiaChallengeModel(@NonNull DiscordChallenge challenge, @NonNull List<DiscordServer> servers, @NonNull List<DiscordChallengeOptOut> optOuts, boolean participationDisabled) {
      this.challenge = challenge;
      this.participationDisabled = participationDisabled;

      for (DiscordServer server : servers) {
        if (server.getGuildId().equals(challenge.getGuildId())) {
          this.serverName = server.getName();
          this.serverIconUrl = server.getIconUrl();
          for (DiscordChannel channel : server.getChannels()) {
            if (channel.getChannelId().equals(challenge.getChannelId())) {
              this.channelName = channel.getName();
            }
          }
        }
      }

      this.serverOptedOut = optOuts.stream().anyMatch(o -> challenge.getGuildId().equals(o.getGuildId()) && o.isServerWide());
      this.channelOptedOut = optOuts.stream().anyMatch(o -> challenge.getGuildId().equals(o.getGuildId()) && challenge.getChannelId().equals(o.getChannelId()));

      GameRepresentation match = null;
      if (!StringUtils.isEmpty(challenge.getVpsTableId())) {
        List<GameRepresentation> games = client.getGameService().getGamesByVpsTable(challenge.getVpsTableId(), challenge.getVpsVersionId());
        if (games.isEmpty() && challenge.getVpsVersionId() != null) {
          games = client.getGameService().getGamesByVpsTable(challenge.getVpsTableId(), null);
        }
        match = games.isEmpty() ? null : games.getFirst();
      }
      this.game = match;
      // a game of another table version is only matched as fallback, its scores do not count for the challenge
      this.versionMismatch = match != null && challenge.getVpsVersionId() != null && !challenge.getVpsVersionId().equals(match.getExtTableVersionId());

      // the sidebar works on competitions, so the challenge is mapped to one
      competition = new CompetitionRepresentation();
      competition.setType(CompetitionType.MANIA_CHALLENGE.name());
      competition.setUuid(String.valueOf(challenge.getId()));
      competition.setName(challenge.getTableName());
      competition.setUrl(challenge.getMessageUrl());
      competition.setVpsTableId(challenge.getVpsTableId());
      competition.setVpsTableVersionId(challenge.getVpsVersionId());
      competition.setStartDate(getStartDate());
      competition.setEndDate(getEndDate());
      competition.setCreatedAt(getStartDate());
      if (game != null) {
        competition.setGameId(game.getId());
      }
    }

    public long getId() {
      return challenge.getId();
    }

    public DiscordChallenge getChallenge() {
      return challenge;
    }

    public CompetitionRepresentation getCompetition() {
      return competition;
    }

    public GameRepresentation getGame() {
      return game;
    }

    public String getVpsTableId() {
      return challenge.getVpsTableId();
    }

    public String getName() {
      return StringUtils.defaultString(challenge.getTableName());
    }

    public String getServerName() {
      return serverName != null ? serverName : Messages.get("competitions.competitions_mania.unknown_server");
    }

    public String getServerIconUrl() {
      return serverIconUrl;
    }

    public String getChannelName() {
      return channelName != null ? channelName : Messages.get("competitions.competitions_mania.unknown_channel");
    }

    public boolean isServerOptedOut() {
      return serverOptedOut;
    }

    public boolean isChannelOptedOut() {
      return channelOptedOut;
    }

    /**
     * True if the user disabled the participation in all Discord challenges in the VPin Mania user settings.
     */
    public boolean isParticipationDisabled() {
      return participationDisabled;
    }

    /**
     * True if the matched game has another table version than the one the challenge is bound to.
     */
    public boolean isVersionMismatch() {
      return versionMismatch;
    }

    public boolean isOptedOut() {
      return participationDisabled || challenge.isOptedOut() || serverOptedOut || channelOptedOut;
    }

    @Nullable
    public Instant getStartDate() {
      return challenge.getStartsAt() != null ? challenge.getStartsAt().toInstant() : null;
    }

    @Nullable
    public Instant getEndDate() {
      return challenge.getEndsAt() != null ? challenge.getEndsAt().toInstant() : null;
    }

    public String getScoreText() {
      if (!StringUtils.isEmpty(challenge.getScoreText())) {
        return challenge.getScoreText();
      }
      if (challenge.getScore() != null) {
        return String.format("%,d", challenge.getScore());
      }
      return Messages.get("competitions.competitions_mania.no_score");
    }

    public String getStatusText() {
      if (isOptedOut()) {
        return Messages.get("competitions.competitions_mania.status_opted_out");
      }
      if (challenge.isRunning()) {
        return Messages.get("competitions.competitions_mania.status_running");
      }
      if (DiscordChallenge.STATUS_CANCELED.equals(challenge.getStatus())) {
        return Messages.get("competitions.competitions_mania.status_canceled");
      }
      return Messages.get("competitions.competitions_mania.status_finished");
    }
  }
}
