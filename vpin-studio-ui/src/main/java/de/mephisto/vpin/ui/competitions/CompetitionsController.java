package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.commons.utils.ImageUtil;
import de.mephisto.vpin.commons.utils.ScoreGraphUtil;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreListRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import eu.hansolo.tilesfx.Tile;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

public class CompetitionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  @FXML
  private Label createdAtLabel;

  @FXML
  private Label uuidLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Label channelLabel;

  @FXML
  private HBox serverBox;

  @FXML
  private HBox ownerBox;

  @FXML
  private TitledPane scorePane;

  @FXML
  private TitledPane metaDataPane;

  @FXML
  private TitledPane competitionMembersPane;

  @FXML
  private VBox membersBox;

  @FXML
  private VBox scoreGraphBox;

  @FXML
  private Accordion accordion;


  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController discordController;

  private Tile highscoresGraphTile;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  private Label noPlayersLabel;
  private Label notActiveLabel;
  private Label notActiveGraphLabel;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated() {
    refreshUsers(competition);
    scorePane.setExpanded(true);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      if (t1.intValue() == 0) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
        Optional<CompetitionRepresentation> selection = offlineController.getSelection();
        updateSelection(selection);
        offlineController.onReload();
      }
      else {
        if (discordController != null) {
          NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
          Optional<CompetitionRepresentation> selection = discordController.getSelection();
          updateSelection(selection);
          discordController.onReload();
        }
      }
    });

    loadTabs();
    updateSelection(Optional.empty());

    accordion.setExpandedPane(scorePane);
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    checkTitledPanes(competitionRepresentation);
    refreshScoreGraph(competitionRepresentation);
    refreshUsers(competitionRepresentation);
    refreshMetaData(competitionRepresentation);
    updateForTabSelection(competitionRepresentation);
  }

  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    if (competitionRepresentation.isPresent()) {
      CompetitionRepresentation competition = competitionRepresentation.get();
      if (metaDataPane.isVisible()) {
        uuidLabel.setText(competition.getUuid());
        serverBox.getChildren().removeAll(serverBox.getChildren());
        ownerBox.getChildren().removeAll(ownerBox.getChildren());

        createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));

        DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
        if(discordServer != null) {
          Image image = new Image(discordServer.getAvatarUrl());
          ImageView view = new ImageView(image);
          view.setPreserveRatio(true);
          view.setFitWidth(50);
          view.setFitHeight(50);
          serverBox.getChildren().removeAll(serverBox.getChildren());
          Label label = new Label(discordServer.getName());

          ImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          serverBox.getChildren().addAll(view, label);
        }

        List<DiscordChannel> discordChannels = client.getDiscordChannels(competition.getDiscordServerId());
        Optional<DiscordChannel> first = discordChannels.stream().filter(channel -> channel.getId() == competition.getDiscordChannelId()).findFirst();
        first.ifPresent(discordChannel -> channelLabel.setText(discordChannel.getName()));

        PlayerRepresentation discordPlayer = client.getDiscordPlayer(competition.getDiscordServerId(), Long.valueOf(competition.getOwner()));
        if(discordPlayer != null) {
          HBox hBox = new HBox(6);
          hBox.setAlignment(Pos.CENTER_LEFT);
          hBox = new HBox(6);
          hBox.setAlignment(Pos.CENTER_LEFT);
          Image image = new Image(discordPlayer.getAvatarUrl());
          ImageView view = new ImageView(image);
          view.setPreserveRatio(true);
          view.setFitWidth(50);
          view.setFitHeight(50);
          ownerBox.getChildren().removeAll(ownerBox.getChildren());
          Label label = new Label(discordPlayer.getName());

          ImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
          ownerBox.getChildren().addAll(view, label);
        }

        startLabel.setText(DateFormat.getDateInstance().format(competition.getStartDate()));
        endLabel.setText(DateFormat.getDateInstance().format(competition.getEndDate()));
      }
    }
  }

  private void checkTitledPanes(Optional<CompetitionRepresentation> cp) {
    competitionMembersPane.setVisible(cp.isPresent() && cp.get().getType().equals(CompetitionType.DISCORD.name()));
    metaDataPane.setVisible(cp.isPresent() && cp.get().getType().equals(CompetitionType.DISCORD.name()));
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
    else {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
  }

  private void refreshScoreGraph(Optional<CompetitionRepresentation> cp) {
    try {
      if(scoreGraphBox != null && scoreGraphBox.getChildren() != null) {
        scoreGraphBox.getChildren().removeAll(scoreGraphBox.getChildren());
      }
    } catch (Exception e) {
      LOG.error("Error refreshing score graph: " + e.getMessage()); //TODO dunno
    }

    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();

      if (!competition.isActive()) {
        scoreGraphBox.getChildren().add(getNotActiveGrpahLabel());
        return;
      }

      ScoreListRepresentation competitionScores = client.getCompetitionScoreList(competition.getId());
      if (!competitionScores.getScores().isEmpty()) {
        if (highscoresGraphTile != null) {
          scoreGraphBox.getChildren().remove(highscoresGraphTile);
        }
        highscoresGraphTile = ScoreGraphUtil.createGraph(competitionScores);
        scoreGraphBox.getChildren().add(highscoresGraphTile);
      }
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
    membersBox.getChildren().removeAll(membersBox.getChildren());
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      if (competitionMembersPane.isVisible()) {
        if (!competition.isActive()) {
          membersBox.getChildren().add(getNotActiveLabel());
        }
        else {
          List<PlayerRepresentation> memberList = client.getDiscordCompetitionPlayers(competition.getId());
          if (memberList.isEmpty()) {
            membersBox.getChildren().add(getNoPlayersLabel());
          }
          else {
            for (PlayerRepresentation player : memberList) {
              try {
                FXMLLoader loader = new FXMLLoader(DiscordUserEntryController.class.getResource("discord-user.fxml"));
                Parent playerPanel = loader.load();
                DiscordUserEntryController controller = loader.getController();
                controller.setData(player);
                membersBox.getChildren().add(playerPanel);
              } catch (IOException e) {
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
      Parent offline = loader.load();
      offlineController = loader.getController();
      offlineController.setCompetitionsController(this);
      offlineTab.setContent(offline);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsDiscordController.class.getResource("tab-competitions-discord.fxml"));
      Parent offline = loader.load();
      discordController = loader.getController();
      discordController.setCompetitionsController(this);
      onlineTab.setContent(offline);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }
  }

  private Label getNoPlayersLabel() {
    if (this.noPlayersLabel == null) {
      noPlayersLabel = new Label("No discord members have joined this competition yet.");
      noPlayersLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
    }
    return this.noPlayersLabel;
  }

  private Label getNotActiveLabel() {
    if (this.notActiveLabel == null) {
      notActiveLabel = new Label("The competition is not active.");
      notActiveLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
    }
    return this.notActiveLabel;
  }

  private Label getNotActiveGrpahLabel() {
    if (this.notActiveGraphLabel == null) {
      notActiveGraphLabel = new Label("The graph is only calculated for active competitions.");
      notActiveGraphLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
    }
    return this.notActiveGraphLabel;
  }
}