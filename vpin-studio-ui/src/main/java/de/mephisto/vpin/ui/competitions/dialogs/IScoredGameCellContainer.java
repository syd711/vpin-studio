package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameCellContainer extends HBox {

  public IScoredGameCellContainer(IScoredSubscriptionDialogController.IScoredSubscription subscription) {
    super(3);

    String name = subscription.getiScoredGame().getName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    InputStream gameMediaItem = OverlayWindowFX.class.getResourceAsStream("avatar-blank.png");
    if (subscription.getGameId() > 0) {
      InputStream gameItem = client.getGameMediaItem(subscription.getGameId(), PopperScreen.Wheel);
      if (gameItem != null) {
        gameMediaItem = gameItem;
      }
    }
    Image image = new Image(gameMediaItem);
    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(100);
    Tooltip.install(imageView, new Tooltip(name));

    this.getChildren().add(imageView);

    VBox column = new VBox(3);
    this.getChildren().add(column);

    Label title = new Label(name);
    title.setTooltip(new Tooltip(name));
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
    column.getChildren().add(title);

    if (subscription.getGameId() == 0) {
      Label label = new Label("Table not installed");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      column.getChildren().add(label);
    }

    if (subscription.getGameId() > 0) {
      ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(subscription.getGameId());
      if (StringUtils.isEmpty(summary.getRaw())) {
        Label error = new Label("No valid highscore found.");
        error.setStyle("-fx-padding: 3 6 3 6;");
        error.getStyleClass().add("error-title");
        column.getChildren().add(error);
      }
    }

    setPadding(new Insets(3, 0, 6, 0));
  }
}
