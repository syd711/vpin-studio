package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FxUtil;
import de.mephisto.vpin.ui.util.ImageUtil;
import eu.hansolo.tilesfx.Demo;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.skins.TileSkin;
import eu.hansolo.tilesfx.skins.TurnoverTileSkin;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class OfflineCompetitionWidgetController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OfflineCompetitionWidgetController.class);
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

  @FXML
  private Label competitionLabel;

  @FXML
  private StackPane competitionStack;

  @FXML
  private BorderPane tilesBorderPane;

  @FXML
  private VBox firstPlaceWidget;

  @FXML
  private BorderPane root;

  @FXML
  private ImageView competitionWheelImage;

  @FXML
  private Label tableNameLabel;

  @FXML
  private HBox topBox;

  @FXML
  private Label durationLabel;

  private Tile highscoresGraphTile;
  private CompetitionRepresentation competition;

  // Add a public no-args constructor
  public OfflineCompetitionWidgetController() {
  }


  private Tile turnoverTile;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image = new Image(Studio.class.getResourceAsStream("competition-bg-default.png"));

    competitionStack.setStyle(" -fx-border-radius: 6 6 6 6;\n" +
        "    -fx-border-style: solid solid solid solid;\n" +
        "    -fx-border-color: #111111;\n" +
        "    -fx-background-color: #111111;\n" +
        "    -fx-background-radius: 6;" +
        "    -fx-border-width: 1;");
    BackgroundImage myBI= new BackgroundImage(image,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
        BackgroundSize.DEFAULT);
    topBox.setBackground(new Background(myBI));

    image = ImageUtil.createAvatar("MFA");

    Rank firstRank = new Rank(Ranking.FIRST, Tile.YELLOW_ORANGE);
    turnoverTile = TileBuilder.create().skinType(Tile.SkinType.TURNOVER)
        .title("#1 Place")
        .prefWidth(Double.MAX_VALUE)
        .customDecimalFormatEnabled(true)
        .customDecimalFormat(new DecimalFormat("###,###,###"))
        .borderWidth(2)
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
        .maxWidth(Double.MAX_VALUE)
        .backgroundColor(Color.web("#111111"))
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
    tilesBorderPane.setCenter(highscoresGraphTile);


    Platform.runLater(() -> {
      turnoverTile.setValue(Double.valueOf("7000000000"));
    });

  }

  public void setCompetition(CompetitionRepresentation competition) {
    if(competition != null) {
      GameRepresentation game = client.getGame(competition.getGameId());
      GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());

      LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      long diff = ChronoUnit.DAYS.between(start, end);

      String duration = "Duration: " + simpleDateFormat.format(competition.getStartDate())
          + " - " + simpleDateFormat.format(competition.getEndDate())
          + " (" + diff + " days)";
      durationLabel.setText(duration);
      turnoverTile.setTitle("-");
      competitionLabel.setText(competition.getName());
      tableNameLabel.setText(game.getGameDisplayName());

      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Wheel);
      if(item != null) {
        String url = item.getUri();
        byte[] bytes = RestClient.getInstance().readBinary(url);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Image image = new Image(byteArrayInputStream);
        competitionWheelImage.setImage(image);
      }
      else {
        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        competitionWheelImage.setImage(wheel);
      }
    }
    else {
      durationLabel.setText("");
    }
  }
}