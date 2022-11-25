package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerScoreRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreRepresentation;
import de.mephisto.vpin.ui.widgets.WidgetController;
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

  public void setData(PlayerScoreRepresentation playerScore) {
    String url = playerScore.getWheelUrl();
    byte[] bytes = RestClient.getInstance().readBinary(url);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    Image image = new Image(byteArrayInputStream);

    wheelImageView.setImage(image);

    tableLabel.setText(playerScore.getTableName());

    ScoreRepresentation score = playerScore.getScore();
    positionLabel.setText("#" + score.getPosition());
    nameLabel.setText(score.getUserInitials());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

    String date = simpleDateFormat.format(playerScore.getUpdatedAt());
    changeDateLabel.setText("Updated: " + date);
  }
}