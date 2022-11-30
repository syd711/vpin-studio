package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ImageUtil;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionSummaryWidgetController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionSummaryWidgetController.class);

  @FXML
  private Label competitionLabel;

  @FXML
  private StackPane competitionStack;

  @FXML
  private ImageView competitionWheelImage;

  @FXML
  private Label tableNameLabel;

  @FXML
  private HBox topBox;

  @FXML
  private Label durationLabel;

  @FXML
  private Label name1;

  @FXML
  private Label name2;

  @FXML
  private Label name3;

  @FXML
  private Label scoreLabel1;

  @FXML
  private Label scoreLabel2;

  @FXML
  private Label scoreLabel3;

  // Add a public no-args constructor
  public CompetitionSummaryWidgetController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Image image = new Image(Studio.class.getResourceAsStream("competition-bg-default.png"));

    competitionStack.setStyle(" -fx-border-radius: 6 6 6 6;\n" +
        "    -fx-border-style: solid solid solid solid;\n" +
        "    -fx-border-color: #111111;\n" +
        "    -fx-background-color: #111111;\n" +
        "    -fx-background-radius: 6;" +
        "    -fx-border-width: 1;");
    BackgroundImage myBI = new BackgroundImage(image,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
        BackgroundSize.DEFAULT);
    topBox.setBackground(new Background(myBI));

    image = ImageUtil.createAvatar("MFA");
  }

  public void setCompetition(CompetitionRepresentation competition) {
    if (competition != null) {
      GameRepresentation game = client.getGame(competition.getGameId());
      GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
      ScoreSummaryRepresentation gameScores = client.getGameScores(game.getId());

      LocalDate start = competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate end = competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate now = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      long durationDays = ChronoUnit.DAYS.between(start, end);

      String duration = "Duration: " + DateFormat.getDateInstance().format(competition.getStartDate())
          + " - " + DateFormat.getDateInstance().format(competition.getEndDate())
          + " (" + durationDays + " days)";
      durationLabel.setText(duration);
      competitionLabel.setText(competition.getName());
      tableNameLabel.setText(game.getGameDisplayName());

      List<ScoreRepresentation> scores = gameScores.getScores();
      if (scores.size() == 3) {
        ScoreRepresentation score1 = scores.get(0);
        name1.setText(formatScoreText(score1));
        scoreLabel1.setFont(getScoreFontSmall());
        scoreLabel1.setText(score1.getScore());

        ScoreRepresentation score2 = scores.get(1);
        name2.setText(formatScoreText(score2));
        scoreLabel2.setFont(getScoreFontSmall());
        scoreLabel2.setText(score2.getScore());

        ScoreRepresentation score3 = scores.get(2);
        name3.setText(formatScoreText(score3));
        scoreLabel3.setFont(getScoreFontSmall());
        scoreLabel3.setText(score3.getScore());
      }

      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Wheel);
      if (item != null) {
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

  private String formatScoreText(ScoreRepresentation score) {
    String name = score.getPlayerInitials();
    if (score.getPlayer() != null) {
      name = score.getPlayer().getName();
    }

    while (name.length() < 40) {
      name += " ";
    }

    return name;
  }
}