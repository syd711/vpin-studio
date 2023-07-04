package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.restclient.util.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
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
  private Label firstLabel;

  @FXML
  private Label secondLabel;

  @FXML
  private Label thirdLabel;

  @FXML
  private Label scoreLabel1;

  @FXML
  private Label scoreLabel2;

  @FXML
  private Label scoreLabel3;

  private Label emptylabel;

  private final static String OFFLINE_EMPTY_TEXT = "                        No active offline competition found.\nStart an offline competition to compete with friends and family.";
  private final static String ONLINE_EMPTY_TEXT = "                            No active Discord competition found.\nStart an online competition on your Discord server or join an existing one.";

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

    emptylabel = new Label(OFFLINE_EMPTY_TEXT);
    emptylabel.getStyleClass().add("preference-description");
    emptylabel.setPadding(new Insets(100, 0, 120, 40));
    competitionStack.getChildren().add(emptylabel);

    emptylabel.setVisible(false);
    topBox.setVisible(false);
  }

  public void setCompetition(CompetitionType competitionType, CompetitionRepresentation competition) {
    if (competition == null) {
      if (competitionType.equals(CompetitionType.DISCORD)) {
        emptylabel.setText(ONLINE_EMPTY_TEXT);
      }
      else {
        emptylabel.setText(OFFLINE_EMPTY_TEXT);
      }
      durationLabel.setText("");
      emptylabel.setVisible(true);
      topBox.setVisible(false);
    }
    else {
      topBox.setVisible(true);
      emptylabel.setVisible(false);
      GameRepresentation game = OverlayWindowFX.client.getGame(competition.getGameId());
      GameMediaRepresentation gameMedia = game.getGameMedia();

      durationLabel.setText("Duration: " + DateUtil.formatDuration(competition.getStartDate(), competition.getEndDate()));
      competitionLabel.setText(competition.getName());
      tableNameLabel.setText(game.getGameDisplayName());

      boolean isActive = competition.isActive();
      firstLabel.setVisible(isActive);
      secondLabel.setVisible(isActive);
      thirdLabel.setVisible(isActive);
      scoreLabel1.setVisible(isActive);
      scoreLabel2.setVisible(isActive);
      scoreLabel3.setVisible(isActive);

      name1.setText("-");
      name2.setText("-");
      name3.setText("-");

      scoreLabel1.setText("0");
      scoreLabel2.setText("0");
      scoreLabel3.setText("0");

      name1.setVisible(isActive);
      name2.setVisible(isActive);
      name3.setVisible(isActive);

      if (competition.isActive()) {
        ScoreSummaryRepresentation latestCompetitionScore = OverlayWindowFX.client.getCompetitionScore(competition.getId());
        if (latestCompetitionScore != null) {
          List<ScoreRepresentation> scores = latestCompetitionScore.getScores();
          if (scores.size() >= 3) {
            ScoreRepresentation score1 = scores.get(0);
            name1.setText(formatScoreText(score1));
            scoreLabel1.setFont(getCompetitionScoreFont());
            scoreLabel1.setText(score1.getScore());

            ScoreRepresentation score2 = scores.get(1);
            name2.setText(formatScoreText(score2));
            scoreLabel2.setFont(getCompetitionScoreFont());
            scoreLabel2.setText(score2.getScore());

            ScoreRepresentation score3 = scores.get(2);
            name3.setText(formatScoreText(score3));
            scoreLabel3.setFont(getCompetitionScoreFont());
            scoreLabel3.setText(score3.getScore());
          }
        }
      }

      GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
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