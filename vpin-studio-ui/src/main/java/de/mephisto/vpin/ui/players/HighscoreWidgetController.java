package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreWidgetController extends WidgetController implements Initializable  {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

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
      String url = item.getUri();
      byte[] bytes = RestClient.getInstance().readBinary(url);
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      Image image = new Image(byteArrayInputStream);
      wheelImageView.setImage(image);
    }
//    else {
//      Image wheel = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
//      wheelImageView.setImage(wheel);
//    }

    tableLabel.setText(game.getGameDisplayName());

    positionLabel.setText("#" + score.getPosition());
    nameLabel.setText(score.getPlayerInitials());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

//    String date = simpleDateFormat.format(score.getUpdatedAt());
//    changeDateLabel.setText("Updated: " + date);
  }
}