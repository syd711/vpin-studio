package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.fx.widgets.WidgetWeeklyCompetitionScoreItemController;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionDiscordDialogController;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  private static final String TAB_OFFLINE = "Offline Competitions";
  private static final String TAB_ONLINE = "Online Competitions";
  private static final String TAB_TABLE_SUBS = "Table Subscriptions";
  private static final String TAB_ISCORED = "iScored Competitions";
  private static final String TAB_WEEKLY = "Weekly Competitions";

  @FXML
  private BorderPane root;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  @FXML
  private Tab tableSubscriptionsTab;

  @FXML
  private Tab iScoredSubscriptionsTab;

  @FXML
  private Tab weeklySubscriptionsTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Label scoreLimitLabel;

  @FXML
  private Label scoreValidationLabel;

  @FXML
  private Label channelLabel;

  @FXML
  private Button copyBtn;

  @FXML
  private HBox serverBox;

  @FXML
  private HBox ownerBox;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private TitledPane competitionMembersPane;

  @FXML
  private TitledPane dashboardPane;

  @FXML
  private VBox dashboardBox;

  @FXML
  private WebView dashboardWebView;

  @FXML
  private VBox scoreBox;

  @FXML
  private Label dashboardStatusLabel;

  @FXML
  private Button dashboardBtn;

  @FXML
  private Button rulesBtn;

  @FXML
  private Button toggleSidebarBtn;

  @FXML
  private Button competitionSettingsBtn;

  @FXML
  private StackPane editorRootStack;

  @FXML
  private VBox membersBox;

  @FXML
  private Accordion accordion;


  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController discordController;
  private TableSubscriptionsController tableSubscriptionsController;
  private IScoredSubscriptionsController iScoredSubscriptionsController;
  private WeeklySubscriptionsController weeklySubscriptionsController;
  private String lastDashboardUrl;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  private boolean sidebarVisible = true;
  private Node sidePanelRoot;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    refreshUsers(competition);
    competitionMembersPane.setExpanded(competition.isPresent() && competition.get().getType().equals(CompetitionType.DISCORD.name()));
    refreshView(tabPane.getSelectionModel().getSelectedItem());
    discordController.onViewActivated(options);
    tableSubscriptionsController.onViewActivated(options);

    if (options != null && options.getModel() != null) {
      if (options.getModel() instanceof CompetitionType) {
        CompetitionType competitionType = (CompetitionType) options.getModel();
        IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
        WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

        if (competitionType.equals(CompetitionType.OFFLINE)) {
          tabPane.getSelectionModel().select(offlineTab);
        }
        else if (competitionType.equals(CompetitionType.DISCORD)) {
          tabPane.getSelectionModel().select(onlineTab);
        }
        else if (competitionType.equals(CompetitionType.SUBSCRIPTION)) {
          tabPane.getSelectionModel().select(tableSubscriptionsTab);
        }
        else if (competitionType.equals(CompetitionType.ISCORED) && iScoredSettings.isEnabled()) {
          tabPane.getSelectionModel().select(iScoredSubscriptionsTab);
        }
        else if (competitionType.equals(CompetitionType.WEEKLY) && wovpSettings.isEnabled() && wovpSettings.isApiKeySet()) {
          tabPane.getSelectionModel().select(weeklySubscriptionsTab);
        }
      }
    }
  }

  @FXML
  private void toggleSidebar() {
    sidebarVisible = !sidebarVisible;

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    uiSettings.setCompetitionsSidebarVisible(sidebarVisible);
    client.getPreferenceService().setJsonPreference(uiSettings, true);

    setSidebarVisible(sidebarVisible);
  }


  @FXML
  private void onCompetitionSettings() {
    Tab selectedTab = getSelectedTab();
    String title = selectedTab.getText();
    switch (title) {
      case TAB_OFFLINE: {
        PreferencesController.open("player_rankings");
        break;
      }
      case TAB_ONLINE: {
        PreferencesController.open("discord_bot");
        break;
      }
      case TAB_TABLE_SUBS: {
        PreferencesController.open("discord_bot");
        break;
      }
      case TAB_ISCORED: {
        PreferencesController.open("iscored");
        break;
      }
      case TAB_WEEKLY: {
        PreferencesController.open("wovp");
        break;
      }
      default: {
        PreferencesController.open("settings_client");
      }
    }
  }

  @FXML
  private void onRulesOpen() {
    if (this.competition.isPresent()) {
      String url = "https://worldofvirtualpinball.com/en/challenge/rules";
      Studio.browse(url);
    }
  }

  @FXML
  private void onDashboardOpen() {
    if (this.competition.isPresent()) {
      String dashboardUrl = competition.get().getUrl();
      Studio.browse(dashboardUrl);
    }
  }

  @FXML
  private void onCopy() {
    String text = uuidLabel.getText();
    Clipboard clipboard = Clipboard.getSystemClipboard();
    ClipboardContent content = new ClipboardContent();
    content.putString(text);
    clipboard.setContent(content);
  }

  @FXML
  private void onDashboardReload() {
    this.refreshDashboard(this.competition);
  }

  public void setSidebarVisible(boolean b) {
    if (b && sidePanelRoot.isVisible()) {
      return;
    }
    if (!b && !sidePanelRoot.isVisible()) {
      return;
    }

    sidebarVisible = b;
    if (!sidebarVisible) {
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, PauseMenuUIDefaults.SCROLL_OFFSET, 612);
      t.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          sidePanelRoot.setVisible(false);
          FontIcon icon = WidgetFactory.createIcon("mdi2a-arrow-expand-left");
          toggleSidebarBtn.setGraphic(icon);
        }
      });
      t.play();
    }
    else {
      sidePanelRoot.setVisible(true);
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, PauseMenuUIDefaults.SCROLL_OFFSET, -612);
      t.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          FontIcon icon = WidgetFactory.createIcon("mdi2a-arrow-expand-right");
          toggleSidebarBtn.setGraphic(icon);
        }
      });
      t.play();
    }
  }

  private Tab getSelectedTab() {
    return tabPane.getSelectionModel().getSelectedItem();
  }

  private void refreshView(Tab tab) {
    String title = tab.getText();
    if (title.equals(TAB_OFFLINE)) {
      NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      Optional<CompetitionRepresentation> selection = offlineController.getSelection();
      updateSelection(selection);
      checkTitledPanes(CompetitionType.OFFLINE);
      offlineController.onReload();
    }
    else if (title.equals(TAB_ONLINE)) {
      if (discordController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
        Optional<CompetitionRepresentation> selection = discordController.getSelection();
        updateSelection(selection);
        checkTitledPanes(CompetitionType.DISCORD);
        discordController.onReload();
      }
    }
    else if (title.equals(TAB_TABLE_SUBS)) {
      if (tableSubscriptionsController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
        Optional<CompetitionRepresentation> selection = tableSubscriptionsController.getSelection();
        updateSelection(selection);
        checkTitledPanes(CompetitionType.SUBSCRIPTION);
        tableSubscriptionsController.onReload();
      }
    }
    else if (title.equals(TAB_ISCORED)) {
      if (iScoredSubscriptionsTab != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        updateSelection(Optional.empty());
        checkTitledPanes(CompetitionType.ISCORED);
      }
    }
    else if (title.equals(TAB_WEEKLY)) {
      if (weeklySubscriptionsTab != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Weekly Challenges"));
        Optional<WeeklySubscriptionsController.WeeklyCompetitionModel> selection = weeklySubscriptionsController.getSelection();
        if (selection.isPresent()) {
          updateSelection(Optional.of(selection.get().getCompetition()));
        }
        else {
          updateSelection(Optional.empty());
        }
        checkTitledPanes(CompetitionType.WEEKLY);
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab id");
    }
  }

  private StudioFXController getControllerForTab(Tab tab) {
    String title = tab.getText();
    if (title.equals(TAB_OFFLINE)) {
      return offlineController;
    }
    if (title.equals(TAB_ONLINE)) {
      return discordController;
    }
    if (title.equals(TAB_TABLE_SUBS)) {
      return tableSubscriptionsController;
    }
    if (title.equals(TAB_ISCORED)) {
      return iScoredSubscriptionsController;
    }
    if (title.equals(TAB_WEEKLY)) {
      return weeklySubscriptionsController;
    }
    throw new UnsupportedOperationException("Failed to find controller for tab " + title);
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    refreshUsers(competitionRepresentation);
    refreshMetaData(competitionRepresentation);
    refreshDashboard(competitionRepresentation);
    updateForTabSelection(competitionRepresentation);
  }

  private void refreshDashboard(Optional<CompetitionRepresentation> competitionRepresentation) {
    dashboardWebView.setVisible(false);
    dashboardStatusLabel.setVisible(false);
    rulesBtn.setVisible(competitionRepresentation.isPresent() && competitionRepresentation.get().getType().equals(CompetitionType.WEEKLY.name()));
    scoreBox.setVisible(false);
    scoreBox.getChildren().removeAll(scoreBox.getChildren());

    if (competitionRepresentation.isPresent()) {
      rulesBtn.setVisible(competitionRepresentation.isPresent() && competitionRepresentation.get().getType().equals(CompetitionType.WEEKLY.name()));
      CompetitionRepresentation competition = competitionRepresentation.get();
      if (competition.getType().equals(CompetitionType.ISCORED.name())) {
        String dashboardUrl = competitionRepresentation.get().getUrl();
        dashboardBtn.setDisable(dashboardUrl == null);
        dashboardWebView.setVisible(dashboardUrl != null);
        dashboardStatusLabel.setText("This competition has no dashboard URL.");
        dashboardStatusLabel.setVisible(dashboardUrl == null);

        if (dashboardUrl != null) {
          WebEngine webEngine = dashboardWebView.getEngine();
          webEngine.setUserStyleSheetLocation(Studio.class.getResource("web-style.css").toString());

          if (lastDashboardUrl == null || !lastDashboardUrl.equals(dashboardUrl)) {
            lastDashboardUrl = dashboardUrl;
            webEngine.load(dashboardUrl);
          }
        }
      }
      else if (competition.getType().equals(CompetitionType.WEEKLY.name())) {
        scoreBox.getChildren().removeAll(scoreBox.getChildren());
        scoreBox.setVisible(false);

        dashboardStatusLabel.setText("Loading Highscores...");
        dashboardStatusLabel.setVisible(true);

        JFXFuture.supplyAsync(() -> {
          List<Pane> children = new ArrayList<>();
          try {
            List<CompetitionScore> weeklyCompetitionScores = client.getCompetitionService().getWeeklyCompetitionScores(competition.getUuid());
            for (CompetitionScore score : weeklyCompetitionScores) {
              FXMLLoader loader = new FXMLLoader(WidgetWeeklyCompetitionScoreItemController.class.getResource("widget-weekly-competition-score-item.fxml"));
              BorderPane row = loader.load();
              WidgetWeeklyCompetitionScoreItemController controller = loader.getController();
              row.setMaxWidth(Double.MAX_VALUE);
              controller.setData(score);
              children.add(row);
            }
          }
          catch (IOException e) {
            LOG.error("Failed to load competition score panel: {}", e.getMessage(), e);
          }
          return children;
        }).thenAcceptLater(children -> {
          dashboardStatusLabel.setVisible(false);
          if (tabPane.getSelectionModel().getSelectedItem().equals(weeklySubscriptionsTab)) {
            scoreBox.getChildren().addAll(children);
            scoreBox.setVisible(!children.isEmpty());
          }
        });
      }
    }
  }


  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    uuidLabel.setText("-");
    copyBtn.setVisible(false);
    startLabel.setText("-");
    endLabel.setText("-");
    scoreValidationLabel.setText("-");
    scoreLimitLabel.setText("-");
    serverBox.getChildren().removeAll(serverBox.getChildren());

    if (competitionRepresentation.isPresent()) {
      String type = competitionRepresentation.get().getType();
      if (type.equals(CompetitionType.DISCORD.name()) || type.equals(CompetitionType.SUBSCRIPTION.name()) || type.equals(CompetitionType.WEEKLY.name())) {
        CompetitionRepresentation competition = competitionRepresentation.get();
        if (metaDataPane.isVisible() && !metaDataPane.isDisabled()) {
          uuidLabel.setText(competition.getUuid());
          copyBtn.setVisible(true);
          serverBox.getChildren().removeAll(serverBox.getChildren());
          ownerBox.getChildren().removeAll(ownerBox.getChildren());

          if (competition.getJoinMode() != null) {
            JoinMode joinMode = JoinMode.valueOf(competition.getJoinMode());
            switch (joinMode) {
              case STRICT: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.STRICT_DESCRIPTION);
                break;
              }
              case CHECKSUM: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.CHECKSUM_DESCRIPTION);
                break;
              }
              case ROM_ONLY: {
                scoreValidationLabel.setText(CompetitionDiscordDialogController.ROM_DESCRIPTION);
                break;
              }
            }

          }

          if (competition.getScoreLimit() == 0) {
            scoreLimitLabel.setText("Table Defaults");
          }
          else {
            scoreLimitLabel.setText(String.valueOf(competition.getScoreLimit()));
          }


          createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));

          //TODO mpf
          if (type.equals(CompetitionType.DISCORD.name()) || type.equals(CompetitionType.SUBSCRIPTION.name())) {
            DiscordServer discordServer = client.getDiscordService().getDiscordServer(competition.getDiscordServerId());
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
              serverBox.getChildren().removeAll(serverBox.getChildren());
              Label label = new Label(discordServer.getName());
              label.setStyle("-fx-font-size: 14px;");

              CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
              serverBox.getChildren().addAll(view, label);
            }

            List<DiscordChannel> discordChannels = client.getDiscordService().getDiscordChannels(competition.getDiscordServerId());
            Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == competition.getDiscordChannelId()).findFirst();
            first.ifPresent(discordChannel -> channelLabel.setText(discordChannel.getName()));


            PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(competition.getDiscordServerId(), Long.valueOf(competition.getOwner()));
            if (discordPlayer != null) {
              HBox hBox = new HBox(6);
              hBox.setAlignment(Pos.CENTER_LEFT);
              hBox = new HBox(6);
              hBox.setAlignment(Pos.CENTER_LEFT);
              Image image = new Image(client.getCachedUrlImage(discordPlayer.getAvatarUrl()));
              ImageView view = new ImageView(image);
              view.setPreserveRatio(true);
              view.setFitWidth(50);
              view.setFitHeight(50);
              ownerBox.getChildren().removeAll(ownerBox.getChildren());
              Label label = new Label(discordPlayer.getName());
              label.setStyle("-fx-font-size: 14px;");

              CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
              ownerBox.getChildren().addAll(view, label);
            }
          }


          if (competition.getStartDate() != null) {
            startLabel.setText(DateFormat.getDateInstance().format(competition.getStartDate()));
            endLabel.setText(DateFormat.getDateInstance().format(competition.getEndDate()));
          }
        }
      }
    }
  }

  private void checkTitledPanes(CompetitionType competitionType) {
    switch (competitionType) {
      case DISCORD: {
        competitionMembersPane.setDisable(false);
        competitionMembersPane.setExpanded(true);
        metaDataPane.setDisable(false);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(true);
        break;
      }
      case SUBSCRIPTION: {
        competitionMembersPane.setDisable(false);
        competitionMembersPane.setExpanded(true);
        metaDataPane.setDisable(false);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(true);
        break;
      }
      case OFFLINE: {
        competitionMembersPane.setDisable(true);
        competitionMembersPane.setExpanded(false);
        metaDataPane.setDisable(true);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(true);
        break;
      }
      case ISCORED: {
        competitionMembersPane.setDisable(true);
        competitionMembersPane.setExpanded(false);
        metaDataPane.setDisable(true);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(false);
        dashboardPane.setExpanded(true);

        break;
      }
      case WEEKLY: {
        competitionMembersPane.setDisable(true);
        competitionMembersPane.setExpanded(false);
        metaDataPane.setDisable(false);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(false);
        dashboardPane.setExpanded(true);

        break;
      }
      default: {
        throw new UnsupportedOperationException("Competition type " + competitionType + " is not mapped.");
      }
    }
  }

  private void updateForTabSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    Tab tab = tabPane.getSelectionModel().getSelectedItem();
    String title = tab.getText();
    if (title.equals(TAB_OFFLINE)) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      }
    }
    else if (title.equals(TAB_ONLINE)) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
    else if (title.equals(TAB_TABLE_SUBS)) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
      }
    }
    else if (title.equals(TAB_ISCORED)) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
      }
    }
    else if (title.equals(TAB_WEEKLY)) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Weekly Challenges", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Weekly Challenges"));
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab.");
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
    membersBox.getChildren().removeAll(membersBox.getChildren());
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      if (competitionMembersPane.isVisible() && !competitionMembersPane.isDisabled()) {
        if (!competition.isActive()) {
          membersBox.getChildren().add(WidgetFactory.createDefaultLabel("The competition is not active."));
        }
        else {
          List<PlayerRepresentation> memberList = client.getCompetitionService().getDiscordCompetitionPlayers(competition.getId());
          if (memberList.isEmpty()) {
            membersBox.getChildren().add(WidgetFactory.createDefaultLabel("No discord members have joined this competition yet."));
          }
          else {
            for (PlayerRepresentation player : memberList) {
              try {
                FXMLLoader loader = new FXMLLoader(DiscordUserEntryController.class.getResource("discord-user.fxml"));
                Parent playerPanel = loader.load();
                DiscordUserEntryController controller = loader.getController();
                controller.setData(player);
                membersBox.getChildren().add(playerPanel);
              }
              catch (IOException e) {
                LOG.error("Failed to load discord player list: " + e.getMessage(), e);
              }
            }
          }
        }
      }
    }
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
      Parent parent = loader.load();
      offlineController = loader.getController();
      offlineController.setCompetitionsController(this);
      offlineTab.setContent(parent);
      offlineTab.setText(TAB_OFFLINE);
    }
    catch (IOException e) {
      LOG.error("failed to load offline: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
      Parent parent = loader.load();
      discordController = loader.getController();
      discordController.setCompetitionsController(this);
      onlineTab.setContent(parent);
      onlineTab.setText(TAB_ONLINE);
    }
    catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableSubscriptionsController.class.getResource("tab-competitions-subscriptions.fxml"));
      Parent parent = loader.load();
      tableSubscriptionsController = loader.getController();
      tableSubscriptionsController.setCompetitionsController(this);
      tableSubscriptionsTab.setContent(parent);
      tableSubscriptionsTab.setText(TAB_TABLE_SUBS);
    }
    catch (IOException e) {
      LOG.error("failed to load subscriptions: " + e.getMessage(), e);
    }

    if (Features.ISCORED_ENABLED) {
      try {
        FXMLLoader loader = new FXMLLoader(IScoredSubscriptionsController.class.getResource("tab-competitions-iscored.fxml"));
        Parent parent = loader.load();
        iScoredSubscriptionsController = loader.getController();
        iScoredSubscriptionsController.setCompetitionsController(this);
        iScoredSubscriptionsTab.setContent(parent);
        iScoredSubscriptionsTab.setText(TAB_ISCORED);
      }
      catch (IOException e) {
        LOG.error("failed to load subscriptions: " + e.getMessage(), e);
      }

      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      if (!iScoredSettings.isEnabled()) {
        tabPane.getTabs().remove(iScoredSubscriptionsTab);
      }
    }
    else {
      tabPane.getTabs().remove(iScoredSubscriptionsTab);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WeeklySubscriptionsController.class.getResource("tab-competitions-weekly.fxml"));
      Parent parent = loader.load();
      weeklySubscriptionsController = loader.getController();
      weeklySubscriptionsController.setCompetitionsController(this);
      weeklySubscriptionsTab.setContent(parent);
      weeklySubscriptionsTab.setText(TAB_WEEKLY);

      WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      if (!wovpSettings.isEnabled() || !wovpSettings.isApiKeySet()) {
        tabPane.getTabs().remove(weeklySubscriptionsTab);
      }
    }
    catch (IOException e) {
      LOG.error("failed to load weekly: " + e.getMessage(), e);
    }
  }


  private StudioFXController getActiveController() {
    Tab selectedTab = getSelectedTab();
    String title = selectedTab.getText();
    switch (title) {
      case TAB_OFFLINE: {
        return offlineController;
      }
      case TAB_ONLINE: {
        return discordController;
      }
      case TAB_TABLE_SUBS: {
        return tableSubscriptionsController;
      }
      case TAB_ISCORED: {
        return iScoredSubscriptionsController;
      }
      case TAB_WEEKLY: {
        return weeklySubscriptionsController;
      }
    }
    return null;
  }

  @Override
  public void onKeyEvent(KeyEvent event) {
    StudioFXController activeController = getActiveController();
    if (activeController != null) {
      activeController.onKeyEvent(event);
    }
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.ISCORED_SETTINGS.equals(key) || PreferenceNames.WOVP_SETTINGS.equals(key)) {
      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

      Platform.runLater(() -> {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (iScoredSettings.isEnabled()) {
          if (!tabPane.getTabs().contains(iScoredSubscriptionsTab)) {
            tabPane.getTabs().add(iScoredSubscriptionsTab);
          }
        }
        else {
          tabPane.getTabs().remove(iScoredSubscriptionsTab);
          selectedTab = offlineTab;
        }

        if (wovpSettings.isEnabled() && wovpSettings.isApiKeySet()) {
          if (!tabPane.getTabs().contains(weeklySubscriptionsTab)) {
            tabPane.getTabs().add(weeklySubscriptionsTab);
          }
        }
        else {
          tabPane.getTabs().remove(weeklySubscriptionsTab);
          selectedTab = offlineTab;
        }

        tabPane.getSelectionModel().select(selectedTab);
      });
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    loadTabs();
    sidePanelRoot = root.getRight();
    sidePanelRoot.managedProperty().bindBidirectional(sidePanelRoot.visibleProperty());

    dashboardWebView.managedProperty().bindBidirectional(dashboardWebView.visibleProperty());
    dashboardStatusLabel.managedProperty().bindBidirectional(dashboardStatusLabel.visibleProperty());
    rulesBtn.managedProperty().bindBidirectional(rulesBtn.visibleProperty());
    copyBtn.managedProperty().bindBidirectional(copyBtn.visibleProperty());

    updateSelection(Optional.empty());
    tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTabIndex, newTabIndex) -> {
      getControllerForTab(oldTabIndex).onViewDeactivated();
      refreshView(newTabIndex);
      getControllerForTab(newTabIndex).onViewActivated(null);
    });
    dashboardStatusLabel.managedProperty().bindBidirectional(dashboardStatusLabel.visibleProperty());
    checkTitledPanes(CompetitionType.OFFLINE);

    Platform.runLater(() -> {
      Studio.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        public void handle(KeyEvent ke) {
          if (ke.getCode() == KeyCode.F3) {
            toggleSidebar();
          }
        }
      });
    });

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    if (!uiSettings.isCompetitionsSidebarVisible()) {
      toggleSidebar();
    }

    client.getPreferenceService().addListener(this);
  }
}