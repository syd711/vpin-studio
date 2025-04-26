package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.competitions.dialogs.CompetitionDiscordDialogController;
import de.mephisto.vpin.ui.util.ProgressDialog;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  private static final int TAB_OFFLINE = 0;
  private static final int TAB_ONLINE = 1;
  private static final int TAB_TABLE_SUBS = 2;
  private static final int TAB_ISCORED = 3;

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
  private WebView dashboardWebView;

  @FXML
  private Label dashboardStatusLabel;

  @FXML
  private Button dashboardBtn;

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
    refreshView(tabPane.getSelectionModel().selectedIndexProperty().get());
//    discordController.onReload();
//    tableSubscriptionsController.onReload();

//    offlineController.onViewActivated();
    discordController.onViewActivated(options);
    tableSubscriptionsController.onViewActivated(options);
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
    int selectedTab = getSelectedTab();
    switch (selectedTab) {
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
      default: {
        PreferencesController.open("settings_client");
      }
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
  private void onDashboardReload() {
    WebEngine webEngine = dashboardWebView.getEngine();
    webEngine.reload();
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

  private int getSelectedTab() {
    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
    return getSelectedTab(selectedIndex);
  }


  /**
   * Convert the selected tab index into TAB id, managing invisible tabs
   */
  private int getSelectedTab(int index) {
    int cnt = 0;
    if (tabPane.getTabs().contains(offlineTab) && cnt++ == index) {
      return TAB_OFFLINE;
    }
    if (tabPane.getTabs().contains(onlineTab) && cnt++ == index) {
      return TAB_ONLINE;
    }
    if (tabPane.getTabs().contains(tableSubscriptionsTab) && cnt++ == index) {
      return TAB_TABLE_SUBS;
    }
    if (tabPane.getTabs().contains(iScoredSubscriptionsTab) && cnt++ == index) {
      return TAB_ISCORED;
    }
    // should not happen
    return -1;
  }

  private void refreshView(Number t1) {
    if (t1.intValue() == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      Optional<CompetitionRepresentation> selection = offlineController.getSelection();
      updateSelection(selection);
      checkTitledPanes(CompetitionType.OFFLINE);
      offlineController.onReload();
    }
    else if (t1.intValue() == 1) {
      if (discordController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
        Optional<CompetitionRepresentation> selection = discordController.getSelection();
        updateSelection(selection);
        checkTitledPanes(CompetitionType.DISCORD);
        discordController.onReload();
      }
    }
    else if (t1.intValue() == 2) {
      if (tableSubscriptionsController != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
        Optional<CompetitionRepresentation> selection = tableSubscriptionsController.getSelection();
        updateSelection(selection);
        checkTitledPanes(CompetitionType.SUBSCRIPTION);
        tableSubscriptionsController.onReload();
      }
    }
    else if (t1.intValue() == 3) {
      if (iScoredSubscriptionsTab != null) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
        Optional<IScoredSubscriptionsController.IScoredGameRoomGameModel> selection = iScoredSubscriptionsController.getSelection();
        updateSelection(Optional.empty());
        checkTitledPanes(CompetitionType.ISCORED);
        iScoredSubscriptionsController.onViewActivated(NavigationOptions.empty());
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab id");
    }
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
    String dashboardUrl = null;
    if (competitionRepresentation.isPresent()) {
      dashboardUrl = competitionRepresentation.get().getUrl();
    }
    dashboardBtn.setDisable(dashboardUrl == null);
    dashboardWebView.setVisible(dashboardUrl != null);
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


  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    uuidLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    scoreValidationLabel.setText("-");
    scoreLimitLabel.setText("-");

    if (competitionRepresentation.isPresent()) {
      String type = competitionRepresentation.get().getType();
      if (type.equals(CompetitionType.DISCORD.name()) || type.equals(CompetitionType.SUBSCRIPTION.name())) {
        CompetitionRepresentation competition = competitionRepresentation.get();
        if (metaDataPane.isVisible() && metaDataPane.isDisabled()) {
          uuidLabel.setText(competition.getUuid());
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

          DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
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
      default: {
        throw new UnsupportedOperationException("Competition type " + competitionType + " is not mapped.");
      }
    }
  }

  private void updateForTabSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
      }
    }
    else if (index == 1) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
    else if (index == 2) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Table Subscriptions"));
      }
    }
    else if (index == 3) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "iScored Subscriptions"));
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
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    loadTabs();
    sidePanelRoot = root.getRight();
    sidePanelRoot.managedProperty().bindBidirectional(sidePanelRoot.visibleProperty());

    updateSelection(Optional.empty());
    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshView(t1);
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

    client.getPreferenceService().addListener(this);
  }


  private StudioFXController getActiveController() {
    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
    switch (selectedIndex) {
      case 0: {
        return offlineController;
      }
      case 1: {
        return discordController;
      }
      case 2: {
        return tableSubscriptionsController;
      }
      case 3: {
        return iScoredSubscriptionsController;
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
    if (PreferenceNames.ISCORED_SETTINGS.equals(key)) {
      int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
      if (selectedIndex == 3) {
        tabPane.getSelectionModel().select(0);
      }

      IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
      if (iScoredSettings.isEnabled()) {
        if (!tabPane.getTabs().contains(iScoredSubscriptionsTab)) {
          tabPane.getTabs().add(iScoredSubscriptionsTab);
        }

        Platform.runLater(() -> {
          List<IScoredGameRoom> gameRooms = iScoredSettings.getGameRooms();
          ProgressDialog.createProgressDialog(new IScoredGameRoomSynchronizationProgressModel(gameRooms));
        });
      }
      else {
        tabPane.getTabs().remove(iScoredSubscriptionsTab);
      }

    }
  }
}