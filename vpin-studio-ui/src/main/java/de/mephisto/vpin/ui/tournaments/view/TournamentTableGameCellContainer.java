package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tournaments.TournamentHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentTableGameCellContainer extends HBox {

  public TournamentTableGameCellContainer(GameRepresentation game, TournamentTable tournamentTable) {
    super(3);

    String name = game.getGameDisplayName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    InputStream gameMediaItem = client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
    if (gameMediaItem == null) {
      gameMediaItem = OverlayWindowFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(gameMediaItem);

    GameMediaRepresentation gameMedia = game.getGameMedia();
    GameMediaItemRepresentation wheelMedia = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
    if (wheelMedia == null) {
      image = new Image(OverlayWindowFX.class.getResourceAsStream("avatar-blank.png"));
    }

    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(100);

    this.getChildren().add(imageView);

    VBox column = new VBox(3);
    this.getChildren().add(column);

    Label title = new Label(name);
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + TournamentHelper.getLabelCss(tournamentTable));
    column.getChildren().add(title);

    if (tournamentTable.getStartDate() != null) {
      Label startDate = new Label("Start Date: " + SimpleDateFormat.getDateTimeInstance().format(tournamentTable.getStartDate()));
      startDate.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournamentTable));
      column.getChildren().add(startDate);
    }
    if (tournamentTable.getEndDate() != null) {
      Label endDate = new Label("End Date: " + SimpleDateFormat.getDateTimeInstance().format(tournamentTable.getEndDate()));
      endDate.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournamentTable));
      column.getChildren().add(endDate);
    }


    ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
    if (StringUtils.isEmpty(summary.getRaw())) {
      Label error = new Label("No valid highscore found.");
      error.setStyle("-fx-padding: 3 6 3 6;");
      error.getStyleClass().add("error-title");
      column.getChildren().add(error);
    }

    setPadding(new Insets(3, 0, 6, 0));
  }
}
