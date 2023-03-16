package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.ScoreGraphUtil;
import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.restclient.util.DateUtil;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetCompetitionController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetCompetitionController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private VBox statsWidget;

  @FXML
  private VBox firstPlaceWidget;

  @FXML
  private VBox remainingTimeWidget;

  @FXML
  private BorderPane root;

  @FXML
  private BorderPane summaryBorderPane;

  private Tile highscoresGraphTile;
  private Tile countdownTile;
  private Tile turnoverTile;

  private CompetitionType competitionType = CompetitionType.OFFLINE;

  @FXML
  private StackPane viewStack;

  private Parent loadingOverlay;

  private WidgetCompetitionSummaryController summaryWidgetController;

  public WidgetCompetitionController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image = new Image(OverlayWindowFX.class.getResourceAsStream("avatar-blank.png"));

    Rank firstRank = new Rank(Ranking.FIRST, Tile.YELLOW_ORANGE);
    turnoverTile = TileBuilder.create().skinType(Tile.SkinType.TURNOVER)
        .title("#1 Place")
        .prefWidth(Double.MAX_VALUE)
        .customDecimalFormatEnabled(true)
        .customDecimalFormat(new DecimalFormat("###,###,###"))
        .borderWidth(1)
        .borderColor(Color.web("#111111"))
        .text("")
        .decimals(0)
        .value(0)
        .unit("")
        .image(image)
        .text("#1 Place")
        .animated(true)
        .checkThreshold(true)
        .onTileEvent(e -> {
          if (TileEvent.EventType.THRESHOLD_EXCEEDED == e.getEventType()) {
            turnoverTile.setRank(firstRank);
            turnoverTile.setValueColor(firstRank.getColor());
            turnoverTile.setUnitColor(firstRank.getColor());
          }
          else if (TileEvent.EventType.THRESHOLD_UNDERRUN == e.getEventType()) {
            turnoverTile.setRank(Rank.DEFAULT);
            turnoverTile.setValueColor(Tile.FOREGROUND);
            turnoverTile.setUnitColor(Tile.FOREGROUND);
          }
        })
        .threshold(70) // triggers the rotation effect
        .build();


    firstPlaceWidget.getChildren().add(turnoverTile);


    countdownTile = TileBuilder.create().skinType(Tile.SkinType.CHARACTER)
        .title("Remaining Days")
        .borderWidth(1)
        .borderColor(Color.web("#111111"))
        .borderColor(Color.web("#111111"))
        .titleAlignment(TextAlignment.CENTER)
        .build();

    remainingTimeWidget.getChildren().add(countdownTile);

    try {
      FXMLLoader loader = new FXMLLoader(WidgetLatestScoresController.class.getResource("widget-competition-summary.fxml"));
      BorderPane root = loader.load();
      root.setMaxWidth(Double.MAX_VALUE);
      summaryWidgetController = loader.getController();
      summaryBorderPane.setCenter(root);
    } catch (IOException e) {
      LOG.error("Failed to load competition summary widget: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Competition...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  private void setCompetition(CompetitionRepresentation competition) {
    summaryWidgetController.setCompetition(competitionType, competition);
    root.setVisible(competition != null);

    if (competition != null) {
      LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate now = DateUtil.today().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      long remainingDays = ChronoUnit.DAYS.between(now, end);
      if (remainingDays < 0) {
        remainingDays = 0;
      }
      countdownTile.setDescription(String.valueOf(remainingDays));
      countdownTile.setText("Competition End: " + DateFormat.getDateInstance().format(competition.getEndDate()));

      if(competition.isActive()) {
        ScoreListRepresentation competitionScores = OverlayWindowFX.client.getCompetitionScoreList(competition.getId());
        if (!competitionScores.getScores().isEmpty()) {
          if (highscoresGraphTile != null) {
            statsWidget.getChildren().remove(highscoresGraphTile);
          }

          highscoresGraphTile = ScoreGraphUtil.createGraph(competitionScores);
          statsWidget.getChildren().add(highscoresGraphTile);
        }

        if (competitionScores.getLatestScore() != null) {
          ScoreSummaryRepresentation latestScore = competitionScores.getLatestScore();
          List<ScoreRepresentation> scores = latestScore.getScores();
          if(!scores.isEmpty()) {
            ScoreRepresentation currentScore = scores.get(0);
            Platform.runLater(() -> {
              turnoverTile.setTitle("#1 Place");
              turnoverTile.setValue(currentScore.getNumericScore());

              if (currentScore.getPlayer() != null) {
                turnoverTile.setText(currentScore.getPlayer().getName());
                String avatarUrl = currentScore.getPlayer().getAvatarUrl();
                if (!StringUtils.isEmpty(avatarUrl)) {
                  Image image = new Image(OverlayWindowFX.client.getCachedUrlImage(avatarUrl));
                  turnoverTile.setImage(new Image(avatarUrl));
                }
                else if (currentScore.getPlayer().getAvatar() != null) {
                  AssetRepresentation avatar = currentScore.getPlayer().getAvatar();
                  turnoverTile.setImage(new Image(OverlayWindowFX.client.getAsset(AssetType.AVATAR, avatar.getUuid())));
                }
              }
              else {
                turnoverTile.setText(currentScore.getPlayerInitials());
              }
            });
          }
        }
      }
    }

  }

  public void refresh(CompetitionRepresentation competition) {
    viewStack.getChildren().add(loadingOverlay);
    new Thread(() -> {
      Platform.runLater(() -> {
        setCompetition(competition);
        root.setVisible(true);
        if (competition != null) {
          if (competition.getType().equals(CompetitionType.DISCORD.name())) {
            DiscordServer discordServer = OverlayWindowFX.client.getDiscordServer(competition.getDiscordServerId());
            if(discordServer != null) {
              titleLabel.setText("Discord: " + discordServer.getName());
            }
            else {
              titleLabel.setText("Discord: - invalid server id -");
            }
          }
          else {
            titleLabel.setText("Offline: " + competition.getName());
          }
        }
        else {
          if (competitionType.equals(CompetitionType.DISCORD)) {
            titleLabel.setText("- No Discord Competition Found - ");
          }
          else {
            titleLabel.setText("- No Offline Competition Found - ");
          }
        }

        viewStack.getChildren().remove(loadingOverlay);

      });
    }).start();
  }

  public void setCompetitionType(CompetitionType competitionType) {
    this.competitionType = competitionType;
  }

  public void setCompact() {
//    viewStack.setVisible(false);
//    root.setPrefHeight(346);
  }
}