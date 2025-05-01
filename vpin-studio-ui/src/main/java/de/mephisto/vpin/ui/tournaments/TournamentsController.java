package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.mania.ManiaSettingsController;
import de.mephisto.vpin.ui.mania.util.ManiaAvatarCache;
import de.mephisto.vpin.ui.players.WidgetPlayerScoreController;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.AvatarFactory;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsController.class);

  @FXML
  private BorderPane root;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab maniaTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Label remainingLabel;

  @FXML
  private Label ownerLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label visibilityLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private TitledPane tournamentMembersPane;

  @FXML
  private TitledPane highscoresPane;

  @FXML
  private TitledPane dashboardPane;

  @FXML
  private VBox membersBox;

  @FXML
  private VBox scoreList;

  @FXML
  private Button refreshBtn;

  @FXML
  private Button dashboardBtn;

  @FXML
  private Button dashboardReloadBtn;

  @FXML
  private Button copyTokenBtn;

  @FXML
  private Label dashboardStatusLabel;

  @FXML
  private WebView dashboardWebView;

  @FXML
  private Accordion accordion;

  @FXML
  private Hyperlink dashboardLink;

  @FXML
  private Hyperlink discordLink;

  @FXML
  private Hyperlink websiteLink;

  @FXML
  private VBox avatarPane;

  private TournamentsManiaController maniaController;

  private Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel = Optional.empty();

  private String lastDashboardUrl;

  @FXML
  private Button toggleSidebarBtn;

  @FXML
  private Button tournamentSettingsBtn;

  private boolean sidebarVisible = true;
  private Node sidePanelRoot;


  // Add a public no-args constructor
  public TournamentsController() {
  }

  @FXML
  private void onDiscordLink() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      String link = tournament.getDiscordLink();
      Studio.browse(link);
    }
  }

  @FXML
  private void onDashboardReload() {
    WebEngine webEngine = dashboardWebView.getEngine();
    webEngine.reload();
  }

  @FXML
  private void opnWebsiteOpen() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      String link = tournament.getWebsite();
      if (!StringUtils.isEmpty(link)) {
        Studio.browse(link);
      }
    }
  }

  @FXML
  private void onUserRefresh() {
    this.refreshUsers(this.tournamentTreeModel);
  }

  @FXML
  private void onTokenCopy() {
    if (this.tournamentTreeModel.isPresent()) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(this.tournamentTreeModel.get().getValue().getTournament().getUuid());
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onDashboardOpen() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      String dashboardUrl = tournament.getDashboardUrl();
      if (!StringUtils.isEmpty(dashboardUrl)) {
        Studio.browse(dashboardUrl);
      }
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    maniaController.onViewActivated(options);
    setTournament(this.tournamentTreeModel);
  }

  @FXML
  private void onTournamentSettings() {
    ManiaSettingsController.open("tournament-settings");
  }

  @FXML
  private void toggleSidebar() {
    sidebarVisible = !sidebarVisible;

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    uiSettings.setCompetitionsSidebarVisible(sidebarVisible);
    client.getPreferenceService().setJsonPreference(uiSettings, true);

    setSidebarVisible(sidebarVisible);
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

  public void setTournament(Optional<TreeItem<TournamentTreeModel>> model) {
    this.tournamentTreeModel = model;
    updateSelection(model);
  }

  private void updateSelection(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    checkTitledPanes(tournamentTreeModel);
    refreshUsers(tournamentTreeModel);
    refreshMetaData(tournamentTreeModel);
    refreshDashboard(tournamentTreeModel);
    refreshHighscores(tournamentTreeModel);
  }

  private void refreshHighscores(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    if (!highscoresPane.isExpanded()) {
      return;
    }

    scoreList.getStyleClass().remove("media-container");
    scoreList.getChildren().removeAll(scoreList.getChildren());
    if (tournamentTreeModel.isPresent() && !tournamentTreeModel.get().getValue().isTournamentNode()) {
      TournamentTreeModel value = tournamentTreeModel.get().getValue();
      Tournament tournament = value.getTournament();
      List<TableScoreDetails> highscores = maniaClient.getHighscoreClient().getTournamentScores(tournament.getId());
      LOG.info("Loaded " + highscores.size() + " highscores for tournament " + tournament.getId());
      highscores = highscores.stream().filter(h -> h.getVpsTableId().equals(value.getVpsTable().getId())).collect(Collectors.toList());

      if (highscores.isEmpty()) {
        Label label = new Label("No scores found.");
        label.getStyleClass().add("default-text");
        scoreList.getChildren().add(label);
      }
      else {
        Collections.sort(highscores, (o1, o2) -> (int) (o2.getScore() - o1.getScore()));
        VpsTable vpsTable = client.getVpsService().getTableById(value.getVpsTable().getId());
        int position = 1;
        for (TableScoreDetails highscore : highscores) {
          GameRepresentation game = client.getGameService().getGameByVpsTable(highscore.getVpsTableId(), highscore.getVpsVersionId());
          try {
            FXMLLoader loader = new FXMLLoader(WidgetPlayerScoreController.class.getResource("widget-highscore.fxml"));
            Pane row = loader.load();
            row.setPrefWidth(600);
            WidgetPlayerScoreController controller = loader.getController();
            controller.setData(game, vpsTable, position, highscore);
            scoreList.getChildren().add(row);
            position++;
          }
          catch (IOException e) {
            LOG.error("failed to load score component: " + e.getMessage(), e);
          }
        }

        if (position == 1) {
          Label label = new Label("No scores found.");
          label.getStyleClass().add("default-text");
          scoreList.getChildren().add(label);
        }
        else {
          scoreList.getStyleClass().add("media-container");
        }
      }
    }
    else {
      Label label = new Label("Select a table to view submitted scores.");
      label.getStyleClass().add("default-text");
      scoreList.getChildren().add(label);
    }
  }

  private void refreshMetaData(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    if (!metaDataPane.isExpanded()) {
      return;
    }

    copyTokenBtn.setDisable(tournamentTreeModel.isEmpty());
    discordLink.setText("-");
    websiteLink.setText("-");
    dashboardLink.setText("-");
    uuidLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    remainingLabel.setText("-");
    ownerLabel.setText("-");
    nameLabel.setText("-");
    visibilityLabel.setText("-");
    descriptionLabel.setText("");
    avatarPane.getChildren().removeAll(avatarPane.getChildren());

    if (tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      TournamentMember owner = maniaClient.getTournamentClient().getTournamentOwner(tournament);

      if (owner != null) {
        ownerLabel.setText(owner.getDisplayName());
        ImageView avatarImageView = AvatarFactory.createAvatarImageView(ManiaAvatarCache.getAvatarImage(owner.getAccountUuid()));
        avatarPane.getChildren().add(avatarImageView);
      }

      nameLabel.setText(tournament.getDisplayName());
      visibilityLabel.setText(tournament.getVisibility() != null && tournament.getVisibility().equals(TournamentVisibility.publicTournament) ? "public" : "private");
      uuidLabel.setText(tournament.getUuid());

      createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getCreationDate()));
      startLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getStartDate()));
      endLabel.setText(tournament.getEndDate() != null ? SimpleDateFormat.getDateTimeInstance().format(tournament.getEndDate()) : "-");
      remainingLabel.setText(DateUtil.formatDuration(tournament.getStartDate(), tournament.getEndDate()));
      discordLink.setText(!StringUtils.isEmpty(tournament.getDiscordLink()) ? tournament.getDiscordLink() : "-");
      websiteLink.setText(!StringUtils.isEmpty(tournament.getWebsite()) ? tournament.getWebsite() : "-");
      dashboardLink.setText(!StringUtils.isEmpty(tournament.getDashboardUrl()) ? tournament.getDashboardUrl() : "-");
      descriptionLabel.setText(!StringUtils.isEmpty(tournament.getDescription()) && !tournament.getDescription().equals("null") ? tournament.getDescription() : "");
    }
  }

  private void checkTitledPanes(Optional<TreeItem<TournamentTreeModel>> model) {
    tournamentMembersPane.setDisable(model.isEmpty());
    metaDataPane.setDisable(model.isEmpty());
    highscoresPane.setDisable(model.isEmpty());
    dashboardPane.setDisable(model.isEmpty());
  }

  private void refreshDashboard(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    if (!dashboardPane.isExpanded()) {
      return;
    }

    String dashboardUrl = null;
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      dashboardUrl = tournament.getDashboardUrl();
    }
    dashboardBtn.setDisable(dashboardUrl == null);
    dashboardReloadBtn.setDisable(dashboardUrl == null);
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

  private void refreshUsers(Optional<TreeItem<TournamentTreeModel>> model) {
    if (!tournamentMembersPane.isExpanded()) {
      return;
    }

    refreshBtn.setDisable(model.isEmpty());

    membersBox.getChildren().removeAll(membersBox.getChildren());
    if (model.isPresent()) {
      TournamentTreeModel treeModel = model.get().getValue();
      Tournament tournament = treeModel.getTournament();
      if (!tournament.isActive()) {
        membersBox.getChildren().add(WidgetFactory.createDefaultLabel("The tournament is not active."));
      }
      else {
        List<TournamentMember> memberList = maniaClient.getTournamentClient().getTournamentMembers(tournament.getId());
        if (memberList.isEmpty()) {
          membersBox.getChildren().add(WidgetFactory.createDefaultLabel("No players have joined this tournament yet."));
        }
        else {
          for (TournamentMember player : memberList) {
            try {
              FXMLLoader loader = new FXMLLoader(TournamentPlayerController.class.getResource("tournament-player.fxml"));
              Parent playerPanel = loader.load();
              TournamentPlayerController controller = loader.getController();
              controller.setData(tournament, player);
              membersBox.getChildren().add(playerPanel);
            }
            catch (IOException e) {
              LOG.error("Failed to load tournament player list: " + e.getMessage(), e);
            }
          }
        }
      }
    }
  }

  private void loadTabs() {
    try {
      FXMLLoader loader = new FXMLLoader(TournamentsManiaController.class.getResource("tab-competitions-mania.fxml"));
      Parent parent = loader.load();
      maniaController = loader.getController();
      maniaController.setTournamentsController(this);
      maniaTab.setContent(parent);
    }
    catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    loadTabs();
    updateSelection(Optional.empty());
    accordion.setExpandedPane(metaDataPane);
    dashboardStatusLabel.managedProperty().bindBidirectional(dashboardStatusLabel.visibleProperty());


    sidePanelRoot = root.getRight();
    sidePanelRoot.managedProperty().bindBidirectional(sidePanelRoot.visibleProperty());

    metaDataPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    tournamentMembersPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    highscoresPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    dashboardPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
  }
}