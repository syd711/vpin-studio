package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.ResourceBundle;

public class OfflineCompetitionWidgetController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OfflineCompetitionWidgetController.class);

  @FXML
  private VBox statsWidget;

  @FXML
  private VBox firstPlaceWidget;

  @FXML
  private VBox remainingTimeWidget;

  @FXML
  private BorderPane summaryBorderPane;

  private Tile highscoresGraphTile;
  private Tile countdownTile;
  private Tile turnoverTile;

  private CompetitionRepresentation competition;
  private CompetitionSummaryWidgetController summaryWidgetController;

  public OfflineCompetitionWidgetController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image = new Image(Studio.class.getResourceAsStream("competition-bg-default.png"));

    Rank firstRank = new Rank(Ranking.FIRST, Tile.YELLOW_ORANGE);
    turnoverTile = TileBuilder.create().skinType(Tile.SkinType.TURNOVER)
        .title("#1 Place")
        .prefWidth(Double.MAX_VALUE)
        .customDecimalFormatEnabled(true)
        .customDecimalFormat(new DecimalFormat("###,###,###"))
        .borderWidth(1)
        .borderColor(Color.web("#111111"))
        .text("Gerrit Grunwald")
        .decimals(0)
        .unit("")
        .image(image)
        .text("bubu")
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

    // LineChart Data
    XYChart.Series<String, Number> series2 = new XYChart.Series();
    series2.setName("Inside");
    series2.getData().add(new XYChart.Data("MO", 8));
    series2.getData().add(new XYChart.Data("TU", 5));
    series2.getData().add(new XYChart.Data("WE", 0));
    series2.getData().add(new XYChart.Data("TH", 2));
    series2.getData().add(new XYChart.Data("FR", 4));
    series2.getData().add(new XYChart.Data("SA", 3));
    series2.getData().add(new XYChart.Data("SU", 5));


    //noinspection unchecked
    highscoresGraphTile = TileBuilder.create()
        .skinType(Tile.SkinType.SMOOTHED_CHART)
        .maxWidth(Double.MAX_VALUE)
        .title("New Highscores")
        .chartType(Tile.ChartType.LINE)
        .borderWidth(1)
        .snapToTicks(true)
        .maxValue(10)
        .checkSectionsForValue(true)
        .startFromZero(true)
        .description("bubu")
        .tickLabelsYVisible(true)
        .dataPointsVisible(true)
        .decimals(1)
        .borderColor(Color.web("#111111"))
        .animated(true)
        .smoothing(false)
        .series(series2)
        .build();
    statsWidget.getChildren().add(highscoresGraphTile);


    try {
      FXMLLoader loader = new FXMLLoader(LatestScoresWidgetController.class.getResource("widget-competition-summary.fxml"));
      BorderPane root = loader.load();
      root.setMaxWidth(Double.MAX_VALUE);
      summaryWidgetController = loader.getController();
      summaryBorderPane.setCenter(root);
    } catch (IOException e) {
      LOG.error("Failed to load competition summary widget: " + e.getMessage(), e);
    }

    Platform.runLater(() -> {
      turnoverTile.setValue(Double.valueOf("7000000000"));
    });

  }

  public void setCompetition(CompetitionRepresentation competition) {
    summaryWidgetController.setCompetition(competition);

    if (competition != null) {
      LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      long remainingDays = ChronoUnit.DAYS.between(now, end);
      if(remainingDays < 0) {
        remainingDays = 0;
      }
      turnoverTile.setTitle("-");
      countdownTile.setDescription(String.valueOf(remainingDays));
      countdownTile.setText("Competition End: " + DateFormat.getDateInstance().format(competition.getEndDate()));
    }
    else {
    }
  }
}