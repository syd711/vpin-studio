package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetCompetitionSummaryController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetCompetitionSummaryController.class);

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
  public WidgetCompetitionSummaryController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    competitionStack.setStyle(" -fx-border-radius: 6 6 6 6;\n" +
        "    -fx-border-style: solid solid solid solid;\n" +
        "    -fx-border-color: #111111;\n" +
        "    -fx-background-color: #111111;\n" +
        "    -fx-background-radius: 6;" +
        "    -fx-border-width: 1;");
  }

  public void setCompetition(CompetitionRepresentation competition) {
    if (competition != null) {
      GameRepresentation game = OverlayWindowFX.client.getGame(competition.getGameId());
      ScoreSummaryRepresentation gameScores = OverlayWindowFX.client.getGameScores(game.getId());
      GameMediaRepresentation gameMedia = game.getGameMedia();

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
        ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(competition.getGameId(), PopperScreen.Wheel);
        Image image = new Image(gameMediaItem);
        competitionWheelImage.setImage(image);
      }
      else {
        Image wheel = new Image(OverlayWindowFX.class.getResourceAsStream("avatar-blank.png"));
        competitionWheelImage.setImage(wheel);
      }

      Image image = new Image(OverlayWindowFX.client.getCompetitionBackground(competition.getGameId()));
      BackgroundImage myBI = new BackgroundImage(image,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
      topBox.setBackground(new Background(myBI));
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