package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
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

public class WidgetPlayerScoreController extends WidgetController implements Initializable {

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

  public void setData(GameRepresentation game, ScoreRepresentation score) {
    GameMediaRepresentation gameMedia = game.getGameMedia();
    GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
    if (item != null) {
      ByteArrayInputStream gameMediaItem = ServerFX.client.getGameMediaItem(score.getGameId(), PopperScreen.Wheel);
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

    Image backgroundImage = new Image(ServerFX.client.getCompetitionBackground(game.getId()));
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
      BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
      BackgroundSize.DEFAULT);
    rootStack.setBackground(new Background(myBI));
  }

  public void setData(GameRepresentation game, int position, TableScore tableScore) {
    if (game == null) {
      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
      wheelImageView.setImage(wheel);
    }
    else {
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = ServerFX.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        Image image = new Image(gameMediaItem);
        wheelImageView.setImage(image);
      }
      else {
        Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        wheelImageView.setImage(wheel);
      }
    }

    tableLabel.setText(game.getGameDisplayName());

    positionLabel.setText("#" + position);
    nameLabel.setText(tableScore.getPlayerName() + " [" + tableScore.getPlayerInitials() + "]");

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(tableScore.getScoreText());

    String date = DateFormat.getDateTimeInstance().format(tableScore.getCreationDate());
    changeDateLabel.setText("Updated: " + date);

    Image backgroundImage = new Image(ServerFX.client.getCompetitionBackground(game.getId()));
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
      BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
      BackgroundSize.DEFAULT);
    rootStack.setBackground(new Background(myBI));
  }
}