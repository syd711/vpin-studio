package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
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
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getCompetitionScoreFont;

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
  private Label name4;

  @FXML
  private Label name5;

  @FXML
  private Label firstLabel;

  @FXML
  private Label secondLabel;

  @FXML
  private Label thirdLabel;

  @FXML
  private Label fourthLabel;

  @FXML
  private Label fifthLabel;

  @FXML
  private Label scoreLabel1;

  @FXML
  private Label scoreLabel2;

  @FXML
  private Label scoreLabel3;

  @FXML
  private Label scoreLabel4;

  @FXML
  private Label scoreLabel5;

  private Label emptylabel;

  private final static String ISCORED_EMPTY_TEXT = "                 No iScored subscription found.\nAdd iScored subscriptions to compete with other players.";
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

    emptylabel = new Label("");
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
      else if (competitionType.equals(CompetitionType.OFFLINE)) {
        emptylabel.setText(OFFLINE_EMPTY_TEXT);
      }
      else if (competitionType.equals(CompetitionType.ISCORED)) {
        emptylabel.setText(ISCORED_EMPTY_TEXT);
      }
      else {
        emptylabel.setText(OFFLINE_EMPTY_TEXT);
      }
      durationLabel.setText("");
      emptylabel.setVisible(true);
      topBox.setVisible(false);

      return;
    }

    topBox.setVisible(true);
    emptylabel.setVisible(false);

    GameRepresentation game = ServerFX.client.getGame(competition.getGameId());
    if (game != null) {
      if (competitionType.equals(CompetitionType.SUBSCRIPTION)) {
        durationLabel.setText("Table Subscription");
        tableNameLabel.setText("Top Scores");
      }
      else if (competitionType.equals(CompetitionType.ISCORED)) {
        durationLabel.setText("iScored Subscription");
        tableNameLabel.setText("Top Scores");
      }
      else {
        durationLabel.setText("Duration: " + DateUtil.formatDuration(competition.getStartDate(), competition.getEndDate()));
        tableNameLabel.setText(game.getGameDisplayName());
      }
    }

    competitionLabel.setText(competition.getName());

    boolean isActive = competition.isActive();
    firstLabel.setVisible(isActive);
    secondLabel.setVisible(isActive);
    thirdLabel.setVisible(isActive);
    scoreLabel1.setVisible(isActive);
    scoreLabel2.setVisible(isActive);
    scoreLabel3.setVisible(isActive);
    scoreLabel4.setVisible(isActive);
    scoreLabel5.setVisible(isActive);

    name1.setText("-");
    name2.setText("-");
    name3.setText("-");
    name4.setText("-");
    name5.setText("-");

    scoreLabel1.setText("0");
    scoreLabel2.setText("0");
    scoreLabel3.setText("0");
    scoreLabel4.setText("0");
    scoreLabel5.setText("0");

    name1.setVisible(isActive);
    name2.setVisible(isActive);
    name3.setVisible(isActive);
    name4.setVisible(isActive);
    name5.setVisible(isActive);

    if (!competition.isActive() || game == null) {
      return;
    }

    ScoreSummaryRepresentation latestCompetitionScore = null;
    if (competitionType.equals(CompetitionType.ISCORED)) {
      GameRoom gameRoom = IScored.getGameRoom(competition.getUrl());
      if (gameRoom != null) {
        latestCompetitionScore = ScoreSummaryRepresentation.forGameRoom(gameRoom, competition.getVpsTableId(), competition.getVpsTableVersionId());
      }
    }
    else {
      latestCompetitionScore = ServerFX.client.getCompetitionScore(competition.getId());
    }

    if (latestCompetitionScore != null && !latestCompetitionScore.getScores().isEmpty()) {
      List<ScoreRepresentation> scores = latestCompetitionScore.getScores();

      int index = 0;
      if (index <= scores.size()) {
        ScoreRepresentation score1 = scores.get(index);
        name1.setText(formatScoreText(score1));
        scoreLabel1.setFont(getCompetitionScoreFont());
        scoreLabel1.setText(score1.getScore());
      }

      index++;
      if (index < scores.size()) {
        ScoreRepresentation score2 = scores.get(index);
        name2.setText(formatScoreText(score2));
        scoreLabel2.setFont(getCompetitionScoreFont());
        scoreLabel2.setText(score2.getScore());
      }

      index++;
      if (index < scores.size()) {
        ScoreRepresentation score3 = scores.get(index);
        name3.setText(formatScoreText(score3));
        scoreLabel3.setFont(getCompetitionScoreFont());
        scoreLabel3.setText(score3.getScore());
      }

      index++;
      if (index < scores.size()) {
        ScoreRepresentation score4 = scores.get(index);
        name4.setText(formatScoreText(score4));
        scoreLabel4.setFont(getCompetitionScoreFont());
        scoreLabel4.setText(score4.getScore());
      }

      index++;
      if (index < scores.size()) {
        ScoreRepresentation score5 = scores.get(index);
        name5.setText(formatScoreText(score5));
        scoreLabel5.setFont(getCompetitionScoreFont());
        scoreLabel5.setText(score5.getScore());
      }

      FrontendMediaRepresentation gameMedia = game.getGameMedia();
      FrontendMediaItemRepresentation item = gameMedia.getDefaultMediaItem(VPinScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = ServerFX.client.getGameMediaItem(competition.getGameId(), VPinScreen.Wheel);
        Image image = new Image(gameMediaItem);
        competitionWheelImage.setImage(image);
      }
      else {
        Image wheel = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
        competitionWheelImage.setImage(wheel);
      }
    }

    InputStream competitionBackground = ServerFX.client.getCompetitionBackground(competition.getGameId());
    if (competitionBackground != null) {
      Image image = new Image(competitionBackground);
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