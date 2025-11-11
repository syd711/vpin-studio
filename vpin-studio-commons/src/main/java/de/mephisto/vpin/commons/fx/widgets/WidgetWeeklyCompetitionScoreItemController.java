package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.competitions.CompetitionScore;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.fx.ServerFX.client;
import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;
import static de.mephisto.vpin.commons.utils.WidgetFactory.showAlertOption;

public class WidgetWeeklyCompetitionScoreItemController extends WidgetController implements Initializable {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  @FXML
  private BorderPane root;

  @FXML
  private ImageView avatarImageView;

  @FXML
  private Label positionLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label changeDateLabel;

  // Add a public no-args constructor
  public WidgetWeeklyCompetitionScoreItemController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(CompetitionScore score) {
    Image image = new Image(client.getCachedUrlImage(score.getAvatarUrl()));
    avatarImageView.setImage(image);

    positionLabel.setText("#" + score.getRank());

    nameLabel.setText(score.getParticipantName());

    scoreLabel.setFont(getScoreFont());
    long l = new Double(score.getScore()).longValue();
    scoreLabel.setText(ScoreFormatUtil.formatScore(l, Locale.getDefault()));

    changeDateLabel.setText(score.getLeague() != null ? score.getLeague() : "");
    System.out.println(score.getChallengeImageUrl());

    Image backgroundImage = new Image(client.getCachedUrlImage(score.getAvatarUrl()));
//    Color start = new Color(0f, 0f, 0f, .1f);
//    Color end = Color.decode("#111111");
//    ImageUtil.gradient(blurred, cropHeight, cropWidth, start, end);
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
        BackgroundSize.DEFAULT);
    root.setBackground(new Background(myBI));
  }
}