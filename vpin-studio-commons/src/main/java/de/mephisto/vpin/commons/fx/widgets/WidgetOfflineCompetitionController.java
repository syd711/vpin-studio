package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.representations.*;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetOfflineCompetitionController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetOfflineCompetitionController.class);

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

  private WidgetCompetitionSummaryController summaryWidgetController;

  public WidgetOfflineCompetitionController() {
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


    //noinspection unchecked
    highscoresGraphTile = TileBuilder.create()
        .skinType(Tile.SkinType.SMOOTHED_CHART)
        .maxWidth(Double.MAX_VALUE)
        .title("Competition Scores")
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
        .build();
    statsWidget.getChildren().add(highscoresGraphTile);

    try {
      FXMLLoader loader = new FXMLLoader(WidgetLatestScoresController.class.getResource("widget-competition-summary.fxml"));
      BorderPane root = loader.load();
      root.setMaxWidth(Double.MAX_VALUE);
      summaryWidgetController = loader.getController();
      summaryBorderPane.setCenter(root);
    } catch (IOException e) {
      LOG.error("Failed to load competition summary widget: " + e.getMessage(), e);
    }
  }

  public void setCompetition(CompetitionRepresentation competition) {
    summaryWidgetController.setCompetition(competition);
    root.setVisible(competition != null);

    if (competition != null) {
      LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      long remainingDays = ChronoUnit.DAYS.between(now, end);
      if (remainingDays < 0) {
        remainingDays = 0;
      }
      countdownTile.setDescription(String.valueOf(remainingDays));
      countdownTile.setText("Competition End: " + DateFormat.getDateInstance().format(competition.getEndDate()));

      ScoreListRepresentation competitionScores = OverlayWindowFX.client.getCompetitionScores(Math.toIntExact(competition.getId()));
      if (!competitionScores.getScores().isEmpty()) {
        XYChart.Series<String, Number> scoreGraph1 = new XYChart.Series();
        scoreGraph1.setName("#1");
        XYChart.Series<String, Number> scoreGraph2 = new XYChart.Series();
        scoreGraph1.setName("#2");
        XYChart.Series<String, Number> scoreGraph3 = new XYChart.Series();
        scoreGraph1.setName("#3");

        //every summary is one history version
        List<ScoreSummaryRepresentation> scores = competitionScores.getScores();
        for (ScoreSummaryRepresentation score : scores) {
          ScoreRepresentation s = score.getScores().get(0);
          scoreGraph1.getData().add(new XYChart.Data(SimpleDateFormat.getDateTimeInstance().format(score.getCreatedAt()), s.getNumericScore()));
          s = score.getScores().get(1);
          scoreGraph2.getData().add(new XYChart.Data(SimpleDateFormat.getDateTimeInstance().format(score.getCreatedAt()), s.getNumericScore()));
          s = score.getScores().get(2);
          scoreGraph3.getData().add(new XYChart.Data(SimpleDateFormat.getDateTimeInstance().format(score.getCreatedAt()), s.getNumericScore()));
        }
        highscoresGraphTile.setSeries(scoreGraph1, scoreGraph2, scoreGraph3);
      }

      if (competitionScores.getLatestScore() != null) {
        ScoreSummaryRepresentation latestScore = competitionScores.getLatestScore();
        ScoreRepresentation currentScore = latestScore.getScores().get(0);

        Platform.runLater(() -> {
          turnoverTile.setTitle("#1 Place");
          turnoverTile.setValue(currentScore.getNumericScore());

          if (currentScore.getPlayer() != null) {
            turnoverTile.setText(currentScore.getPlayer().getName());
            String avatarUrl = currentScore.getPlayer().getAvatarUrl();
            if (!StringUtils.isEmpty(avatarUrl)) {
              turnoverTile.setImage(new Image(avatarUrl));
            }
            else if (currentScore.getPlayer().getAvatar() != null) {
              AssetRepresentation avatar = currentScore.getPlayer().getAvatar();
              turnoverTile.setImage(new Image(OverlayWindowFX.client.getAsset(avatar.getUuid())));
            }
          }
          else {
            turnoverTile.setText(currentScore.getPlayerInitials());
          }
        });
      }
    }
    else {
    }
  }
}