package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.fx.widgets.WidgetWeeklyCompetitionScoreItemController;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.JoinMode;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.mania.ManiaSettingsController;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class CompetitionsController implements Initializable, StudioFXController, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);
  private final static double SCORE_SCROLL_OFFSET = 200;

  @FXML
  private BorderPane root;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab iScoredSubscriptionsTab;

  @FXML
  private Tab weeklySubscriptionsTab;

  @FXML
  private Tab maniaChallengesTab;

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
  private TitledPane metaDataPane;

  @FXML
  private TitledPane dashboardPane;

  @FXML
  private VBox dashboardBox;

  @FXML
  private WebView dashboardWebView;

  @FXML
  private VBox scoreBox;

  @FXML
  private ScrollPane dashboardScrollPane;

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
  private Accordion accordion;


  private CompetitionsOfflineController offlineController;
  private IScoredSubscriptionsController iScoredSubscriptionsController;
  private WeeklySubscriptionsController weeklySubscriptionsController;
  private ManiaChallengesController maniaChallengesController;
  private String lastDashboardUrl;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  private boolean sidebarVisible = true;
  private Node sidePanelRoot;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    refreshView(tabPane.getSelectionModel().getSelectedItem());
    refreshManiaChallengesTab(options != null && CompetitionType.MANIA_CHALLENGE.equals(options.getModel()));

    if (options != null && options.getModel() != null) {
      if (options.getModel() instanceof CompetitionType) {
        CompetitionType competitionType = (CompetitionType) options.getModel();
        IScoredSettings iScoredSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
        WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);

        if (competitionType.equals(CompetitionType.OFFLINE)) {
          tabPane.getSelectionModel().select(offlineTab);
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
    if (selectedTab == offlineTab) {
      PreferencesController.open("player_rankings");
    }
    else if (selectedTab == iScoredSubscriptionsTab) {
      PreferencesController.open("iscored");
    }
    else if (selectedTab == weeklySubscriptionsTab) {
      PreferencesController.open("wovp");
    }
    else if (selectedTab == maniaChallengesTab) {
      ManiaSettingsController.open("mania-discord-settings.fxml", "discordSettingsBtn");
    }
    else {
      PreferencesController.open("settings_client");
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
    if (tab == offlineTab) {
      NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.offline_competitions")));
      Optional<CompetitionRepresentation> selection = offlineController.getSelection();
      updateSelection(selection);
      checkTitledPanes(CompetitionType.OFFLINE);
      offlineController.onReload();
    }
    else if (tab == iScoredSubscriptionsTab) {
      NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.iscored_subscriptions")));
      updateSelection(Optional.empty());
      checkTitledPanes(CompetitionType.ISCORED);
    }
    else if (tab == weeklySubscriptionsTab) {
      NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.wovp_challenges")));
      Optional<WeeklySubscriptionsController.WeeklyCompetitionModel> selection = weeklySubscriptionsController.getSelection();
      if (selection.isPresent()) {
        updateSelection(Optional.of(selection.get().getCompetition()));
      }
      else {
        updateSelection(Optional.empty());
      }
      checkTitledPanes(CompetitionType.WEEKLY);
    }
    else if (tab == maniaChallengesTab) {
      NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.mania_challenges")));
      Optional<ManiaChallengesController.ManiaChallengeModel> selection = maniaChallengesController.getSelection();
      updateSelection(selection.map(ManiaChallengesController.ManiaChallengeModel::getCompetition));
      checkTitledPanes(CompetitionType.MANIA_CHALLENGE);
    }
    else {
      throw new UnsupportedOperationException("Invalid tab id");
    }
  }

  private StudioFXController getControllerForTab(Tab tab) {
    if (tab == offlineTab) {
      return offlineController;
    }
    if (tab == iScoredSubscriptionsTab) {
      return iScoredSubscriptionsController;
    }
    if (tab == weeklySubscriptionsTab) {
      return weeklySubscriptionsController;
    }
    if (tab == maniaChallengesTab) {
      return maniaChallengesController;
    }
    throw new UnsupportedOperationException("Failed to find controller for tab " + tab.getText());
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
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
//        Optional<IScoredGameRoom> selectedGameRoom = iScoredSubscriptionsController.getSelectedGameRoom();
//        if(selectedGameRoom.isPresent()) {
//          String dashboardUrl = selectedGameRoom.get().getUrl();
//          dashboardBtn.setDisable(dashboardUrl == null);
//          dashboardWebView.setVisible(dashboardUrl != null);
//          dashboardStatusLabel.setText("This competition has no dashboard URL.");
//          dashboardStatusLabel.setVisible(dashboardUrl == null);
//
//          if (dashboardUrl != null) {
//            WebEngine webEngine = dashboardWebView.getEngine();
//            webEngine.setUserStyleSheetLocation(Studio.class.getResource("web-style.css").toString());
//
//            if (lastDashboardUrl == null || !lastDashboardUrl.equals(dashboardUrl)) {
//              lastDashboardUrl = dashboardUrl;
//              webEngine.load(dashboardUrl);
//            }
//          }
//        }
        String dashboardUrl = competitionRepresentation.get().getUrl();
        dashboardBtn.setDisable(dashboardUrl == null);
        dashboardWebView.setVisible(dashboardUrl != null);
        dashboardStatusLabel.setText(Messages.get("competitions.competitions.this_competition_has_no_dashboard_url"));
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

        dashboardStatusLabel.setText(Messages.get("competitions.competitions.loading_highscores"));
        dashboardStatusLabel.setVisible(true);

        JFXFuture.supplyAsync(() -> {
          List<Pane> children = new ArrayList<>();
          try {
            List<CompetitionScore> weeklyCompetitionScores = client.getCompetitionService().getWeeklyCompetitionScores(competition.getUuid());
            for (CompetitionScore score : weeklyCompetitionScores) {
              FXMLLoader loader = new FXMLLoader(WidgetWeeklyCompetitionScoreItemController.class.getResource("widget-weekly-competition-score-item.fxml"));
              loader.setResources(Messages.getBundle());
              BorderPane row = loader.load();
              WidgetWeeklyCompetitionScoreItemController controller = loader.getController();
              row.setMaxWidth(Double.MAX_VALUE);
              row.setUserData(score);
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
            scoreBox.getChildren().removeAll(scoreBox.getChildren());
            scoreBox.getChildren().addAll(children);
            scoreBox.setVisible(!children.isEmpty());

            focusMyScore();
          }
        });
      }
      else if (competition.getType().equals(CompetitionType.MANIA_CHALLENGE.name())) {
        dashboardBtn.setDisable(competition.getUrl() == null);
        Optional<ManiaChallengesController.ManiaChallengeModel> selection = maniaChallengesController.getSelection();
        if (selection.isPresent()) {
          ManiaChallengesController.ManiaChallengeModel model = selection.get();
          String position = model.getChallenge().getPosition() != null ? String.valueOf(model.getChallenge().getPosition()) : "-";
          dashboardStatusLabel.setText(Messages.get("competitions.competitions_mania.dashboard_summary",
              model.getScoreText(), position, model.getChallenge().getParticipants(), model.getStatusText()));
          dashboardStatusLabel.setVisible(true);
        }
      }
    }
  }

  private void focusMyScore() {
    // wait for the layout pass, so that the score rows have their final bounds
    Platform.runLater(() -> {
      Optional<Node> myScorePanel = scoreBox.getChildren().stream()
        .filter(s -> s.getUserData() instanceof CompetitionScore && ((CompetitionScore) s.getUserData()).isMyScore())
        .findFirst();
      if (myScorePanel.isEmpty()) {
        return;
      }

      dashboardScrollPane.applyCss();
      dashboardScrollPane.layout();

      double contentHeight = scoreBox.getBoundsInLocal().getHeight();
      double viewportHeight = dashboardScrollPane.getViewportBounds().getHeight();
      double scrollableHeight = contentHeight - viewportHeight;
      if (scrollableHeight <= 0) {
        return;
      }

      double targetY = Math.max(0, myScorePanel.get().getBoundsInParent().getMinY() - SCORE_SCROLL_OFFSET);
      dashboardScrollPane.setVvalue(Math.min(1, targetY / scrollableHeight));
    });
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
      if (type.equals(CompetitionType.WEEKLY.name()) || type.equals(CompetitionType.MANIA_CHALLENGE.name())) {
        CompetitionRepresentation competition = competitionRepresentation.get();
        if (metaDataPane.isVisible() && !metaDataPane.isDisabled()) {
          uuidLabel.setText(competition.getUuid());
          copyBtn.setVisible(true);
          serverBox.getChildren().removeAll(serverBox.getChildren());

          if (competition.getJoinMode() != null) {
            JoinMode joinMode = JoinMode.valueOf(competition.getJoinMode());
            switch (joinMode) {
              case STRICT: {
                scoreValidationLabel.setText(Messages.get("competitions.discord_competition_edit.rom_name_and_vpx_file_size_check"));
                break;
              }
              case CHECKSUM: {
                scoreValidationLabel.setText(Messages.get("competitions.discord_competition_edit.rom_name_and_vpx_script_checksum_check"));
                break;
              }
              case ROM_ONLY: {
                scoreValidationLabel.setText(Messages.get("competitions.discord_competition_edit.rom_name_check"));
                break;
              }
            }

          }

          if (type.equals(CompetitionType.MANIA_CHALLENGE.name())) {
            scoreLimitLabel.setText("-");
          }
          else if (competition.getScoreLimit() == 0) {
            scoreLimitLabel.setText(Messages.get("competitions.competitions.table_defaults"));
          }
          else {
            scoreLimitLabel.setText(String.valueOf(competition.getScoreLimit()));
          }


          if (competition.getCreatedAt() != null) {
            createdAtLabel.setText(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(competition.getCreatedAt().atZone(ZoneId.systemDefault())));
          }

          if (type.equals(CompetitionType.MANIA_CHALLENGE.name())) {
            maniaChallengesController.getSelection().ifPresent(this::refreshManiaChallengeMetaData);
          }

          if (competition.getStartDate() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
            startLabel.setText(fmt.format(competition.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate()));
            if (competition.getEndDate() != null) {
              endLabel.setText(fmt.format(competition.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate()));
            }
            else if (type.equals(CompetitionType.MANIA_CHALLENGE.name())) {
              endLabel.setText(Messages.get("competitions.competitions_mania.open_ended"));
            }
          }
        }
      }
    }
  }

  private void refreshManiaChallengeMetaData(ManiaChallengesController.ManiaChallengeModel model) {
    channelLabel.setText("#" + model.getChannelName());

    Image image;
    if (model.getServerIconUrl() == null) {
      image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
    }
    else {
      image = new Image(client.getCachedUrlImage(model.getServerIconUrl()));
    }
    ImageView view = new ImageView(image);
    view.setPreserveRatio(true);
    view.setFitWidth(50);
    view.setFitHeight(50);
    CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));

    Label label = new Label(model.getServerName());
    label.setStyle("-fx-font-size: 14px;");
    serverBox.getChildren().addAll(view, label);
  }

  private void checkTitledPanes(CompetitionType competitionType) {
    switch (competitionType) {
      case OFFLINE: {
        metaDataPane.setDisable(true);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(true);
        break;
      }
      case ISCORED: {
        metaDataPane.setDisable(true);
        metaDataPane.setExpanded(false);

        dashboardPane.setDisable(false);
        dashboardPane.setExpanded(true);

        break;
      }
      case WEEKLY:
      case MANIA_CHALLENGE: {
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
    if (tab == offlineTab) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.offline_competitions"), competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.offline_competitions")));
      }
    }
    else if (tab == iScoredSubscriptionsTab) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.iscored_subscriptions"), competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.iscored_subscriptions")));
      }
    }
    else if (tab == weeklySubscriptionsTab) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.wovp_challenges"), competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.wovp_challenges")));
      }
    }
    else if (tab == maniaChallengesTab) {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.mania_challenges"), competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.competitions"), Messages.get("competitions.competitions.mania_challenges")));
      }
    }
    else {
      throw new UnsupportedOperationException("Invalid tab.");
    }
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
      loader.setResources(Messages.getBundle());
      Parent parent = loader.load();
      offlineController = loader.getController();
      offlineController.setCompetitionsController(this);
      offlineTab.setContent(parent);
    }
    catch (IOException e) {
      LOG.error("failed to load offline: " + e.getMessage(), e);
    }

    if (Features.ISCORED_ENABLED) {
      try {
        FXMLLoader loader = new FXMLLoader(IScoredSubscriptionsController.class.getResource("tab-competitions-iscored.fxml"));
        loader.setResources(Messages.getBundle());
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

    try {
      FXMLLoader loader = new FXMLLoader(WeeklySubscriptionsController.class.getResource("tab-competitions-weekly.fxml"));
      loader.setResources(Messages.getBundle());
      Parent parent = loader.load();
      weeklySubscriptionsController = loader.getController();
      weeklySubscriptionsController.setCompetitionsController(this);
      weeklySubscriptionsTab.setContent(parent);

      Image image = new Image(Studio.class.getResourceAsStream("wovp.png"));
      ImageView view = new ImageView(image);
      view.setFitWidth(18);
      view.setFitHeight(18);
      WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      weeklySubscriptionsTab.setGraphic(view);
      if (!wovpSettings.isEnabled() || !wovpSettings.isApiKeySet()) {
        tabPane.getTabs().remove(weeklySubscriptionsTab);
      }
    }
    catch (IOException e) {
      LOG.error("failed to load weekly: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(ManiaChallengesController.class.getResource("tab-competitions-mania-challenges.fxml"));
      loader.setResources(Messages.getBundle());
      Parent parent = loader.load();
      maniaChallengesController = loader.getController();
      maniaChallengesController.setCompetitionsController(this);
      maniaChallengesTab.setContent(parent);

      Image image = new Image(Studio.class.getResourceAsStream("mania.png"));
      ImageView view = new ImageView(image);
      view.setFitWidth(18);
      view.setFitHeight(18);
      maniaChallengesTab.setGraphic(view);
    }
    catch (IOException e) {
      LOG.error("failed to load mania challenges: " + e.getMessage(), e);
    }
    // only shown once the VPin Mania registration and the Discord link of the user have been checked
    tabPane.getTabs().remove(maniaChallengesTab);
  }

  /**
   * Shows the VPin Mania challenges tab if the cabinet is registered to VPin Mania and the user account is linked to Discord.
   * The Discord account is linked on the VPin Mania website, so this is checked again every time the view is activated.
   */
  private void refreshManiaChallengesTab(boolean select) {
    JFXFuture.supplyAsync(ManiaChallengesController::isAvailable).thenAcceptLater(available -> {
      if (available) {
        if (!tabPane.getTabs().contains(maniaChallengesTab)) {
          tabPane.getTabs().add(maniaChallengesTab);
        }
        if (select) {
          tabPane.getSelectionModel().select(maniaChallengesTab);
        }
      }
      else if (tabPane.getTabs().contains(maniaChallengesTab)) {
        if (getSelectedTab() == maniaChallengesTab) {
          tabPane.getSelectionModel().select(offlineTab);
        }
        tabPane.getTabs().remove(maniaChallengesTab);
      }
    });
  }


  private StudioFXController getActiveController() {
    Tab selectedTab = getSelectedTab();
    if (selectedTab == offlineTab) {
      return offlineController;
    }
    if (selectedTab == iScoredSubscriptionsTab) {
      return iScoredSubscriptionsController;
    }
    if (selectedTab == weeklySubscriptionsTab) {
      return weeklySubscriptionsController;
    }
    if (selectedTab == maniaChallengesTab) {
      return maniaChallengesController;
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
    if (PreferenceNames.MANIA_SETTINGS.equals(key)) {
      Platform.runLater(() -> {
        refreshManiaChallengesTab(false);
        if (tabPane.getTabs().contains(maniaChallengesTab)) {
          maniaChallengesController.doReload();
        }
      });
    }
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
    refreshManiaChallengesTab(false);
  }
}
