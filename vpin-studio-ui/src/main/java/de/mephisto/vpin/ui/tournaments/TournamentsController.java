package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentMember;
import de.mephisto.vpin.connectors.mania.model.TournamentVisibility;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.util.AvatarFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsController.class);

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
  private VBox avatarPane;

  private TournamentsManiaController maniaController;

  private Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel = Optional.empty();

  private String lastDashboardUrl;

  // Add a public no-args constructor
  public TournamentsController() {
  }

  @FXML
  private void onDiscordLink() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      String link = tournament.getDiscordLink();
      if (!StringUtils.isEmpty(link)) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(link));
          } catch (Exception e) {
            LOG.error("Failed to open dashboard link: " + e.getMessage(), e);
          }
        }
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
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(dashboardUrl));
          } catch (Exception e) {
            LOG.error("Failed to open dashboard link: " + e.getMessage(), e);
          }
        }
      }
    }
  }

  @Override
  public void onViewActivated() {
    maniaController.onViewActivated();
    setTournament(this.tournamentTreeModel);
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
    if(!metaDataPane.isExpanded()) {
      return;
    }

    scoreList.getChildren().removeAll(scoreList.getChildren());
    if (tournamentTreeModel.isPresent() && tournamentTreeModel.get().isLeaf()) {
      TournamentTreeModel value = tournamentTreeModel.get().getValue();
      VpsTable vpsTable = value.getVpsTable();
      Tournament tournament = value.getTournament();
      List<TableScore> highscores = maniaClient.getHighscoreClient().getHighscores(tournament.getId());
      if (highscores.isEmpty()) {
        Label label = new Label("No scores found.");
        label.getStyleClass().add("default-text");
        scoreList.getChildren().add(label);
      }
      else {
        Label label = new Label("Highscores for \"" + vpsTable.getDisplayName() + "\"");
        label.getStyleClass().add("default-text");
        scoreList.getChildren().add(label);
        for (TableScore highscore : highscores) {

        }
      }
    }
  }

  private void refreshMetaData(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    if(!metaDataPane.isExpanded()) {
      return;
    }

    copyTokenBtn.setDisable(tournamentTreeModel.isEmpty());
    discordLink.setText("-");
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

      if(owner != null) {
        ownerLabel.setText(owner.getDisplayName());
        avatarPane.getChildren().add(AvatarFactory.create(client.getCachedUrlImage(maniaClient.getAccountClient().getAvatarUrl(owner.getAccountUuid()))));
      }

      nameLabel.setText(tournament.getDisplayName());
      visibilityLabel.setText(tournament.getVisibility() != null && tournament.getVisibility().equals(TournamentVisibility.publicTournament) ? "public" : "private");
      uuidLabel.setText(tournament.getUuid());

      createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getCreationDate()));
      startLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getStartDate()));
      endLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getEndDate()));
      remainingLabel.setText(DateUtil.formatDuration(tournament.getStartDate(), tournament.getEndDate()));
      discordLink.setText(!StringUtils.isEmpty(tournament.getDiscordLink()) ? tournament.getDiscordLink() : "-");
      dashboardLink.setText(!StringUtils.isEmpty(tournament.getDashboardUrl()) ? tournament.getDashboardUrl() : "-");
      descriptionLabel.setText(tournament.getDescription());
    }
  }

  private void checkTitledPanes(Optional<TreeItem<TournamentTreeModel>> model) {
    tournamentMembersPane.setDisable(model.isEmpty());
    metaDataPane.setDisable(model.isEmpty());
    highscoresPane.setDisable(model.isEmpty());
    dashboardPane.setDisable(model.isEmpty());
  }

  private void refreshDashboard(Optional<TreeItem<TournamentTreeModel>> tournamentTreeModel) {
    if(!metaDataPane.isExpanded()) {
      return;
    }

    String dashboardUrl = null;
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get().getValue();
      Tournament tournament = treeModel.getTournament();
      dashboardUrl = tournament.getDashboardUrl();
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

  private void refreshUsers(Optional<TreeItem<TournamentTreeModel>> model) {
    if(!tournamentMembersPane.isExpanded()) {
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
            } catch (IOException e) {
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
    } catch (IOException e) {
      LOG.error("failed to load online: " + e.getMessage(), e);
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    loadTabs();
    updateSelection(Optional.empty());
    accordion.setExpandedPane(metaDataPane);
    dashboardStatusLabel.managedProperty().bindBidirectional(dashboardStatusLabel.visibleProperty());

    metaDataPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    tournamentMembersPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    highscoresPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
    dashboardPane.expandedProperty().addListener((observable, oldValue, newValue) -> updateSelection(tournamentTreeModel));
  }
}