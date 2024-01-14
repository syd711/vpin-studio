package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ResourceBundle;

public class WidgetPlayerScoreController extends WidgetController implements Initializable  {

  @FXML
  private StackPane rootStack;

  @FXML
  private ImageView wheelImageView;

  @FXML
  private Label tableLabel;

  @FXML
  private Label positionLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label scoreLabel;

  @FXML
  private Label changeDateLabel;

  // Add a public no-args constructor
  public WidgetPlayerScoreController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(PlayerRepresentation player, GameRepresentation game, ScoreRepresentation score) {
    GameMediaRepresentation gameMedia = game.getGameMedia();
    GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
    if(item != null) {
      ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(score.getGameId(), PopperScreen.Wheel);
      Image image = new Image(gameMediaItem);
      wheelImageView.setImage(image);
    }
    else {
      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      wheelImageView.setImage(wheel);
    }

    tableLabel.setText(game.getGameDisplayName());

    positionLabel.setText("#" + score.getPosition());
    nameLabel.setText(score.getPlayer().getName());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

    String date = DateFormat.getDateTimeInstance().format(score.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);

    Image backgroundImage = new Image(OverlayWindowFX.client.getCompetitionBackground(game.getId()));
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
      BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
      BackgroundSize.DEFAULT);
    rootStack.setBackground(new Background(myBI));
  }
}