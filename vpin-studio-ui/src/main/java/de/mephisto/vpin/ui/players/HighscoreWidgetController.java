package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreWidgetController extends WidgetController implements Initializable  {

  @FXML
  private BorderPane root;

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
  public HighscoreWidgetController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(PlayerRepresentation player, ScoreRepresentation score) {
    GameRepresentation game = client.getGame(score.getGameId());
    GameMediaRepresentation gameMedia = client.getGameMedia(score.getGameId());
    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Wheel);
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
    nameLabel.setText(score.getPlayerInitials());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

    String date = DateFormat.getDateTimeInstance().format(score.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);
  }
}