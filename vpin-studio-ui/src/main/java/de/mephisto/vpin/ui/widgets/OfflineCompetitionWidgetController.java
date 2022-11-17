package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ImageUtil;
import eu.hansolo.tilesfx.Demo;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class OfflineCompetitionWidgetController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OfflineCompetitionWidgetController.class);

  @FXML
  private Label competitionLabel;

  @FXML
  private BorderPane tilesBorderPane;

  @FXML
  private VBox firstPlaceWidget;

  @FXML
  private BorderPane root;

  private Tile highscoresGraphTile;

  // Add a public no-args constructor
  public OfflineCompetitionWidgetController() {
  }


  private Tile turnoverTile;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
    image = ImageUtil.createAvatar("MFA");

    Rank firstRank = new Rank(Ranking.FIRST, Tile.YELLOW_ORANGE);
    turnoverTile = TileBuilder.create().skinType(Tile.SkinType.TURNOVER)
        .title("#1 Place")
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
        .title("New Highscores")
        .chartType(Tile.ChartType.LINE)
        .borderWidth(1)
        .snapToTicks(true)
        .maxValue(10)
        .checkSectionsForValue(true)
        .startFromZero(true)
        .tickLabelsYVisible(true)
        .dataPointsVisible(true)
        .decimals(1)
        .borderColor(Color.web("#111111"))
        .animated(true)
        .smoothing(false)
        .series(series2)
        .build();
    tilesBorderPane.setCenter(highscoresGraphTile);


    Platform.runLater(() -> {
      turnoverTile.setValue(Double.valueOf("7000000000"));

    });

  }
}