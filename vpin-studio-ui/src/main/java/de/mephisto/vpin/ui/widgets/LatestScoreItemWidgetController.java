package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class LatestScoreItemWidgetController extends WidgetController implements Initializable  {

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

  // Add a public no-args constructor
  public LatestScoreItemWidgetController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setData(GameRepresentation game, ScoreRepresentation score, GameMediaItemRepresentation wheel) {
    String url = wheel.getUri();
    byte[] bytes = RestClient.getInstance().readBinary(url);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    Image image = new Image(byteArrayInputStream);

    wheelImageView.setFitHeight(getTableFont().getSize() + getScoreFont().getSize() * 2);
    wheelImageView.setFitWidth(getTableFont().getSize() + getScoreFont().getSize() * 2);
    wheelImageView.setPreserveRatio(true);
    wheelImageView.setImage(image);

    tableLabel.setFont(getTableFont());
    tableLabel.setText(game.getGameDisplayName());

    positionLabel.setFont(getScoreFont());
    positionLabel.setText("#" + score.getPosition());

    nameLabel.setFont(getScoreFont());
    nameLabel.setText(score.getUserInitials());

    scoreLabel.setFont(getScoreFont());
    scoreLabel.setText(score.getScore());

  }
}