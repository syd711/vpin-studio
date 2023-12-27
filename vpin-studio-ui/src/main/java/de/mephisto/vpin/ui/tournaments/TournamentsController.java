package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentPlayer;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
  private Label descriptionLabel;

  @FXML
  private HBox serverBox;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private TitledPane tournamentMembersPane;

  @FXML
  private TitledPane dashboardPane;

  @FXML
  private VBox membersBox;

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
  private Hyperlink discordLink;

  @FXML
  private ImageView bannerImageView;

  private TournamentsManiaController maniaController;

  private Optional<TournamentTreeModel> tournamentTreeModel = Optional.empty();

  private String lastDashboardUrl;

  // Add a public no-args constructor
  public TournamentsController() {
  }

  @FXML
  private void onDiscordLink() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
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
      String vpsTableUrl = VPS.getVpsTableUrl(this.tournamentTreeModel.get().getTournament().getUuid());
      content.putString(vpsTableUrl);
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onDashboardOpen() {
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
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

  public void setTournament(Optional<TournamentTreeModel> model) {
    if (!model.isEmpty()) {
      TournamentTreeModel treeModel = model.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
      if (this.tournamentTreeModel.isPresent() && this.tournamentTreeModel.get().getTournament().getUuid().equals(tournament.getUuid())) {
        return;
      }
    }

    this.tournamentTreeModel = model;
    updateSelection(model);
  }

  private void updateSelection(Optional<TournamentTreeModel> tournamentTreeModel) {
    checkTitledPanes(tournamentTreeModel);
    refreshUsers(tournamentTreeModel);
    refreshMetaData(tournamentTreeModel);
    refreshDashboard(tournamentTreeModel);
  }

  private void refreshMetaData(Optional<TournamentTreeModel> tournamentTreeModel) {
    copyTokenBtn.setDisable(tournamentTreeModel.isEmpty());
    discordLink.setText("-");
    uuidLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    descriptionLabel.setText("");
    bannerImageView.setImage(null);

    if (tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
      uuidLabel.setText(tournament.getUuid());
      bannerImageView.setImage(new Image(maniaClient.getTournamentClient().getBadgeUrl(tournament)));
      serverBox.getChildren().removeAll(serverBox.getChildren());
      createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getCreationDate()));
      startLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getStartDate()));
      endLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tournament.getEndDate()));
      discordLink.setText(!StringUtils.isEmpty(tournament.getDiscordLink()) ? tournament.getDiscordLink()  : "-");
      descriptionLabel.setText(tournament.getDescription());
    }
  }

  private void checkTitledPanes(Optional<TournamentTreeModel> model) {
    membersBox.setDisable(model.isEmpty());
    metaDataPane.setDisable(model.isEmpty());
    dashboardPane.setDisable(model.isEmpty());
  }

  private void refreshDashboard(Optional<TournamentTreeModel> tournamentTreeModel) {
    String dashboardUrl = null;
    if (this.tournamentTreeModel.isPresent()) {
      TournamentTreeModel treeModel = tournamentTreeModel.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
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

  private void refreshUsers(Optional<TournamentTreeModel> model) {
    refreshBtn.setDisable(model.isEmpty());
    tournamentMembersPane.setDisable(model.isEmpty());

    membersBox.getChildren().removeAll(membersBox.getChildren());
    if (model.isPresent()) {
      TournamentTreeModel treeModel = model.get();
      ManiaTournamentRepresentation tournament = treeModel.getTournament();
      if (!tournament.isActive()) {
        membersBox.getChildren().add(WidgetFactory.createDefaultLabel("The tournament is not active."));
      }
      else {
        List<ManiaTournamentPlayer> memberList = maniaClient.getTournamentClient().getTournamentPlayers(tournament.getUuid());
        if (memberList.isEmpty()) {
          membersBox.getChildren().add(WidgetFactory.createDefaultLabel("No players have joined this tournament yet."));
        }
        else {
          for (ManiaTournamentPlayer player : memberList) {
            try {
              FXMLLoader loader = new FXMLLoader(TournamentPlayerController.class.getResource("discord-user.fxml"));
              Parent playerPanel = loader.load();
              TournamentPlayerController controller = loader.getController();
              controller.setData(player);
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
  }
}