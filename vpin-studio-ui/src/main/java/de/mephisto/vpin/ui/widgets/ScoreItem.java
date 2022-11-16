package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;

public class ScoreItem extends BorderPane {

  private final GameRepresentation game;
  private final ScoreRepresentation score;
  private final GameMediaItemRepresentation wheelMedia;

  public ScoreItem(GameRepresentation game, ScoreRepresentation score, GameMediaItemRepresentation wheelMedia) {
    this.game = game;
    this.score = score;
    this.wheelMedia = wheelMedia;
    initComponent();
  }

  private void initComponent() {
    Studio.stage.getScene().getStylesheets().add(getClass().getResource("./stylesheet.css").toExternalForm());
    this.getStyleClass().add("navigation-panel");


    String url = wheelMedia.getUri();
    byte[] bytes = RestClient.getInstance().readBinary(url);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    Image image = new Image(byteArrayInputStream);

    ImageView wheelIcon = new ImageView(image);
    wheelIcon.setFitHeight(40);
    wheelIcon.setFitWidth(40);
    wheelIcon.setPreserveRatio(true);
    setLeft(wheelIcon);


    VBox center = new VBox();
    center.setAlignment(Pos.BASELINE_LEFT);
    setCenter(center);


    Label tableLabel = new Label(game.getGameDisplayName());
    center.getChildren().add(tableLabel);

    HBox infoRow = new HBox();
    infoRow.setAlignment(Pos.BASELINE_LEFT);
    center.getChildren().add(infoRow);

    Label positionLabel = new Label("#" + score.getPosition());
    positionLabel.setPrefWidth(50);
    infoRow.getChildren().add(positionLabel);

    ImageView avatar = new ImageView();
    avatar.setFitWidth(30);
    avatar.setFitHeight(30);
    avatar.setPreserveRatio(true);
    infoRow.getChildren().add(avatar);

    Label nameLabel = new Label(score.getUserInitials());
    infoRow.getChildren().add(nameLabel);

    HBox scoreRow = new HBox();
    scoreRow.setAlignment(Pos.BOTTOM_RIGHT);
    Label scoreLabel = new Label(score.getScore());
    scoreRow.getChildren().add(scoreLabel);
  }
}
