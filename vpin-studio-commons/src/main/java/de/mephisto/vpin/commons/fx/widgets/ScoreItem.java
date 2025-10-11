package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
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
  private final FrontendMediaItemRepresentation wheelMedia;

  public ScoreItem(GameRepresentation game, ScoreRepresentation score, FrontendMediaItemRepresentation wheelMedia) {
    this.game = game;
    this.score = score;
    this.wheelMedia = wheelMedia;
    initComponent();
  }

  private void initComponent() {
    ByteArrayInputStream gameMediaItem = ServerFX.client.getWheelIcon(this.game.getId(), true);
    Image image = new Image(gameMediaItem);

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

    Label nameLabel = new Label(score.getPlayerInitials());
    infoRow.getChildren().add(nameLabel);

    HBox scoreRow = new HBox();
    scoreRow.setAlignment(Pos.BOTTOM_RIGHT);
    Label scoreLabel = new Label(score.getFormattedScore());
    scoreRow.getChildren().add(scoreLabel);
  }
}
