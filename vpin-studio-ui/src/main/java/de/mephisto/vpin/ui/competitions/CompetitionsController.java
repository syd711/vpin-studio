package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.discord.DiscordUserEntryController;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
  private VBox scoresBox;


  private CompetitionsOfflineController offlineController;
  private CompetitionsDiscordController discordController;

  private Tile highscoresGraphTile;

  private Optional<CompetitionRepresentation> competition = Optional.empty();

  private Label noPlayersLabel;

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
        if(discordController != null) {
          NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
          Optional<CompetitionRepresentation> selection = discordController.getSelection();
          updateSelection(selection);
          discordController.onReload();
        }
      }
    });

    updateSelection(Optional.empty());
    scorePane.setExpanded(true);
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = Optional.ofNullable(competition);
    updateSelection(Optional.ofNullable(competition));
  }

  private void updateSelection(Optional<CompetitionRepresentation> competitionRepresentation) {
    checkTabs();
    checkTitledPanes(competitionRepresentation);

    refreshScores(competitionRepresentation);
    refreshUsers(competitionRepresentation);
    refreshMetaData(competitionRepresentation);
    updateForTabSelection(competitionRepresentation);
  }

  private void refreshMetaData(Optional<CompetitionRepresentation> competitionRepresentation) {
    if (competitionRepresentation.isPresent()) {
      CompetitionRepresentation competition = competitionRepresentation.get();
      if (metaDataPane.isVisible()) {
        uuidLabel.setText(competition.getUuid());
        createdAtLabel.setText(SimpleDateFormat.getDateTimeInstance().format(competition.getCreatedAt()));

        DiscordServer discordServer = client.getDiscordServer(competition.getDiscordServerId());
        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Image image = new Image(discordServer.getAvatarUrl());
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(50);
        view.setFitHeight(50);
        serverBox.getChildren().removeAll(serverBox.getChildren());
        Label label = new Label(discordServer.getName());
        serverBox.getChildren().addAll(view, label);

        PlayerRepresentation discordPlayer = client.getDiscordPlayer(competition.getDiscordServerId(), Long.valueOf(competition.getOwner()));
        hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER_LEFT);
        image = new Image(discordPlayer.getAvatarUrl());
        view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(50);
        view.setFitHeight(50);
        ownerBox.getChildren().removeAll(ownerBox.getChildren());
        label = new Label(discordPlayer.getName());
        ownerBox.getChildren().addAll(view, label);

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
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
    else {
      if (competitionRepresentation.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions", competitionRepresentation.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
      }
    }
  }

  private void refreshScores(Optional<CompetitionRepresentation> cp) {
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      scoresBox.getChildren().removeAll(scoresBox.getChildren());

      ScoreListRepresentation competitionScores = client.getCompetitionScoreList(competition.getId());
      if (!competitionScores.getScores().isEmpty()) {
        XYChart.Series<String, Number> scoreGraph1 = new XYChart.Series();
        scoreGraph1.setName("#1");
        XYChart.Series<String, Number> scoreGraph2 = new XYChart.Series();
        scoreGraph2.setName("#2");
        XYChart.Series<String, Number> scoreGraph3 = new XYChart.Series();
        scoreGraph3.setName("#3");

        //every summary is one history version
        List<ScoreSummaryRepresentation> scores = competitionScores.getScores();

        for (ScoreSummaryRepresentation score : scores) {
          if (score.getScores().size() >= 3) {
            ScoreRepresentation s = score.getScores().get(0);
            scoreGraph1.getData().add(new XYChart.Data(SimpleDateFormat.getDateInstance().format(score.getCreatedAt()), s.getNumericScore()));
            s = score.getScores().get(1);
            scoreGraph2.getData().add(new XYChart.Data(SimpleDateFormat.getDateInstance().format(score.getCreatedAt()), s.getNumericScore()));
            s = score.getScores().get(2);
            scoreGraph3.getData().add(new XYChart.Data(SimpleDateFormat.getDateInstance().format(score.getCreatedAt()), s.getNumericScore()));
          }
        }


        if (highscoresGraphTile != null) {
          scoresBox.getChildren().remove(highscoresGraphTile);
        }

        //noinspection unchecked
        highscoresGraphTile = TileBuilder.create()
            .skinType(Tile.SkinType.SMOOTHED_CHART)
            .maxWidth(Double.MAX_VALUE)
            .textSize(Tile.TextSize.BIGGER)
            .chartType(Tile.ChartType.LINE)
            .borderWidth(1)
            .snapToTicks(true)
            .maxValue(10)
            .checkSectionsForValue(true)
            .startFromZero(true)
            .description("")
            .tickLabelsYVisible(true)
            .dataPointsVisible(true)
            .decimals(1)
            .borderColor(Color.web("#111111"))
            .animated(true)
            .smoothing(false)
            .series(scoreGraph1, scoreGraph2, scoreGraph3)
            .build();
        scoresBox.getChildren().add(highscoresGraphTile);
      }
    }
  }

  private void refreshUsers(Optional<CompetitionRepresentation> cp) {
    if (cp.isPresent()) {
      CompetitionRepresentation competition = cp.get();
      if (competitionMembersPane.isVisible()) {
        membersBox.getChildren().removeAll(membersBox.getChildren());
        List<PlayerRepresentation> memberList = client.getDiscordCompetitionPlayers(competition.getId());
        if(memberList.isEmpty()) {
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
    else {
      membersBox.getChildren().removeAll(membersBox.getChildren());
    }
  }

  private void checkTabs() {
    if (offlineController == null) {
      try {
        FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
        Parent offline = loader.load();
        offlineController = loader.getController();
        offlineController.setCompetitionsController(this);
        offlineTab.setContent(offline);
      } catch (IOException e) {
        LOG.error("failed to load buildIn players: " + e.getMessage(), e);
      }
    }

    boolean isDiscordBotAvailable = client.isDiscordBotAvailable();
    if (isDiscordBotAvailable) {
      if (discordController == null) {
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
    }
    else if (onlineTab.getContent() == null) {
      VBox content = new VBox();
      content.setSpacing(3);
      content.setPadding(new Insets(12, 12, 12, 12));
      Label title = new Label("No Discord bot configured.");
      title.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
      Label description = new Label("Open the Discord preferences and check how to create and configure a Discord bot.");
      description.setStyle("-fx-font-size: 14px;");
      content.getChildren().addAll(title, description);
      onlineTab.setContent(content);
    }
  }

  private Label getNoPlayersLabel() {
    if(this.noPlayersLabel == null) {
      noPlayersLabel = new Label("No discord members have joined this competition yet.");
      noPlayersLabel.setStyle("-fx-font-weight: bold;-fx-font-size: 14px;");
    }
    return this.noPlayersLabel;
  }
}