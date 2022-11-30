package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreSummaryRepresentation;
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

public class LatestScoreItemWidgetController extends WidgetController implements Initializable  {
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
  public LatestScoreItemWidgetController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(GameRepresentation game, ScoreSummaryRepresentation scoreSummary, GameMediaItemRepresentation wheel) {
    String url = wheel.getUri();
    byte[] bytes = RestClient.getInstance().readBinary(url);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    Image image = new Image(byteArrayInputStream);

    wheelImageView.setImage(image);

    tableLabel.setText(game.getGameDisplayName());
    ScoreRepresentation score = scoreSummary.getScores().get(0);

    positionLabel.setText("#" + score.getPosition());

    if(score.getPlayer() != null) {
      nameLabel.setText(score.getPlayer().getName());
    }
    else {
      nameLabel.setText(score.getPlayerInitials());
    }

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

    String date = simpleDateFormat.format(scoreSummary.getCreatedAt());
    changeDateLabel.setText("Updated: " + date);
  }
}