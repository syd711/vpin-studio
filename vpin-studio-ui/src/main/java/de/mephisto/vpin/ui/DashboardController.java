package de.mephisto.vpin.ui;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, StudioFXController {
  private final static int TILE_WIDTH = 600;
  private final static int TILE_HEIGHT = 500;

  @FXML
  private BorderPane widget1;

  @FXML
  private BorderPane widget2;

  @FXML
  private BorderPane widget3;

  @FXML
  private BorderPane widget4;

  @FXML
  private BorderPane widget5;

  @FXML
  private BorderPane widget6;
  private Tile turnoverTile;

  // Add a public no-args constructor
  public DashboardController() {
  }


  @FXML
  private void onDashboardClick() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

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

    XYChart.Series<String, Number> series3 = new XYChart.Series();
    series3.setName("Outside");
    series3.getData().add(new XYChart.Data("MO", 8));
    series3.getData().add(new XYChart.Data("TU", 5));
    series3.getData().add(new XYChart.Data("WE", 0));
    series3.getData().add(new XYChart.Data("TH", 2));
    series3.getData().add(new XYChart.Data("FR", 4));
    series3.getData().add(new XYChart.Data("SA", 3));
    series3.getData().add(new XYChart.Data("SU", 5));


    Tile graph = TileBuilder.create()
        .skinType(Tile.SkinType.SMOOTHED_CHART)
        .prefSize(TILE_WIDTH, TILE_HEIGHT)
        .title("Weekly Statistic")
        .animated(true)
        .smoothing(false)
        .series(series2, series3)
        .build();
    widget1.setCenter(graph);

    Tile timeTile = TileBuilder.create()
        .skinType(Tile.SkinType.TIME)
        .prefSize(TILE_WIDTH, TILE_HEIGHT)
        .title("Play Time")
        .text("Last Played: Funhouse")
        .duration(LocalTime.of(1, 22))
        .description("Total Play Time")
        .textVisible(true)
        .build();
    widget2.setCenter(timeTile);



    Rank firstRank = new Rank(Ranking.FIRST, Tile.YELLOW_ORANGE);
    turnoverTile = TileBuilder.create().skinType(Tile.SkinType.TURNOVER)
        .prefSize(TILE_WIDTH, TILE_HEIGHT)
        .title("Latest Highscore")
        .text("Gerrit Grunwald")
        .decimals(0)
        .unit("")
        .image(new Image(DashboardController.class.getResourceAsStream("dashboard.png")))
        .animated(true)
        .checkThreshold(true)
        .onTileEvent(e -> {
          if (TileEvent.EventType.THRESHOLD_EXCEEDED == e.getEventType()) {
            turnoverTile.setRank(firstRank);
            turnoverTile.setValueColor(firstRank.getColor());
            turnoverTile.setUnitColor(firstRank.getColor());
          } else if (TileEvent.EventType.THRESHOLD_UNDERRUN == e.getEventType()) {
            turnoverTile.setRank(Rank.DEFAULT);
            turnoverTile.setValueColor(Tile.FOREGROUND);
            turnoverTile.setUnitColor(Tile.FOREGROUND);
          }
        })
        .threshold(70) // triggers the rotation effect
        .build();

    turnoverTile.setValue(100);
    widget3.setCenter(turnoverTile);

    Tile activeChallenge = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .image(new Image(DashboardController.class.getResourceAsStream("dashboard.png")))
        .imageMask(Tile.ImageMask.ROUND)
        .text("Active Challange: Funhouse")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    widget4.setCenter(activeChallenge);

    Tile numberTile = TileBuilder.create()
        .skinType(Tile.SkinType.NUMBER)
        .prefSize(TILE_WIDTH, TILE_HEIGHT)
        .title("Total Games Played")
        .value(13)
        .unit("")
        .description("")
        .decimals(0)
        .textVisible(true)
        .build();
    widget5.setCenter(numberTile);
  }
}