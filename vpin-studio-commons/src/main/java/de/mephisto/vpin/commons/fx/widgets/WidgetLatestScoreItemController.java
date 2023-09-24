package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tables.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.tables.GameMediaRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
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

  public void setData(GameRepresentation game, ScoreRepresentation score) {
    InputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
    if(gameMediaItem == null) {
      gameMediaItem = OverlayWindowFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(gameMediaItem);

    GameMediaRepresentation gameMedia = game.getGameMedia();
    GameMediaItemRepresentation wheelMedia = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
    if (wheelMedia == null) {
      image = new Image(OverlayWindowFX.class.getResourceAsStream("avatar-blank.png"));
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
    scoreLabel.setText(score.getScore());

    String date = simpleDateFormat.format(score.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);

    Image backgroundImage = new Image(OverlayWindowFX.client.getCompetitionBackground(game.getId()));
    BackgroundImage myBI = new BackgroundImage(backgroundImage,
        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
        BackgroundSize.DEFAULT);
    root.setBackground(new Background(myBI));
  }
}