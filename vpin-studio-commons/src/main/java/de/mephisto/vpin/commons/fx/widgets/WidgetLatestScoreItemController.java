package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.commons.utils.WidgetFactory.getScoreFont;

public class WidgetLatestScoreItemController extends WidgetController implements Initializable {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  @FXML
  private BorderPane root;

  @FXML
  private VBox highscoreVBox;

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
  public WidgetLatestScoreItemController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(GameRepresentation game, FrontendMediaRepresentation gameMedia, ScoreRepresentation score) {
    InputStream gameMediaItem = ServerFX.client.getWheelIcon(game.getId(), true);
    if (gameMediaItem == null) {
      gameMediaItem = ServerFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(gameMediaItem);

    FrontendMediaItemRepresentation wheelMedia = gameMedia.getDefaultMediaItem(VPinScreen.Wheel);
    if (wheelMedia == null) {
      image = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
    }

    wheelImageView.setImage(image);

    tableLabel.setText(game.getGameDisplayName());
    positionLabel.setText("#" + score.getPosition());

    if (score.getPlayer() != null) {
      nameLabel.setText(score.getPlayer().getName());
    }
    else {
      nameLabel.setText(score.getPlayerInitials());
    }

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getFormattedScore());

    String date = simpleDateFormat.format(score.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);

    InputStream competitionBackground = ServerFX.client.getCompetitionBackground(game.getId());
    if (competitionBackground != null) {
      Image backgroundImage = new Image(competitionBackground);
      BackgroundImage myBI = new BackgroundImage(backgroundImage,
          BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
          BackgroundSize.DEFAULT);
      root.setBackground(new Background(myBI));
    }
  }
}