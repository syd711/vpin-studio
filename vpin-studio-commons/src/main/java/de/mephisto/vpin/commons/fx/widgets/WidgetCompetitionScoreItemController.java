package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class WidgetCompetitionScoreItemController extends WidgetController implements Initializable {
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
  public WidgetCompetitionScoreItemController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(int gameId, ScoreRepresentation score) {
    PlayerRepresentation player = score.getPlayer();
    if(player != null && player.getAvatarUrl() != null) {
      Image image = new Image(player.getAvatarUrl());
      avatarImageView.setImage(image);
    }


    positionLabel.setText("#" + score.getPosition());

    if (score.getPlayer() != null) {
      nameLabel.setText(score.getPlayer().getName());
    }
    else {
      nameLabel.setText(score.getPlayerInitials());
    }

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

    String date = simpleDateFormat.format(score.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);

    Image backgroundImage = new Image(OverlayWindowFX.client.getCompetitionBackground(gameId));
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
        BackgroundSize.DEFAULT);
    root.setBackground(new Background(myBI));
  }
}