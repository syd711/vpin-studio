package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tournaments.TournamentHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.SimpleDateFormat;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentTableGameCellContainer extends HBox {

  public static final int LABEL_WIDTH = 76;

  public TournamentTableGameCellContainer(GameRepresentation game, Tournament tournament, TournamentTable tournamentTable) {
    super(3);

    String name = tournamentTable.getDisplayName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    InputStream gameMediaItem = ServerFX.class.getResourceAsStream("avatar-blank.png");
    if (game != null) {
      InputStream gameItem = client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
      if (gameItem != null) {
        gameMediaItem = gameItem;
      }
    }
    Image image = new Image(gameMediaItem);
    ImageView imageView = new ImageView(image);
    imageView.setPreserveRatio(true);
    imageView.setFitWidth(100);
    Tooltip.install(imageView, new Tooltip(tournamentTable.getDisplayName()));

    this.getChildren().add(imageView);

    VBox column = new VBox(3);
    this.getChildren().add(column);

    Label title = new Label(name);
    title.setTooltip(new Tooltip(tournamentTable.getDisplayName()));
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + TournamentHelper.getLabelCss(tournament, tournamentTable));
    column.getChildren().add(title);

    if (game == null) {
      Label label = new Label("No matching table installed");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      column.getChildren().add(label);
    }
    else if (!tournamentTable.isEnabled()) {
      Label label = new Label("Table not enabled right now");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      column.getChildren().add(label);
    }

    if (tournamentTable.getStartDate() != null) {
      HBox row = new HBox(6);
      row.setAlignment(Pos.BASELINE_LEFT);
      Label startDateLabel = new Label("Start:");
      startDateLabel.setPrefWidth(LABEL_WIDTH);
      startDateLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-weight: bold;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      Label startDate = new Label(SimpleDateFormat.getDateTimeInstance().format(tournamentTable.getStartDate()));
      startDate.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      row.getChildren().add(startDateLabel);
      row.getChildren().add(startDate);

      column.getChildren().add(row);
    }
    if (tournamentTable.getEndDate() != null) {
      HBox row = new HBox(6);
      row.setAlignment(Pos.BASELINE_LEFT);
      Label endDateLabel = new Label("End:");
      endDateLabel.setPrefWidth(LABEL_WIDTH);
      endDateLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-weight: bold;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      Label endDate = new Label(SimpleDateFormat.getDateTimeInstance().format(tournamentTable.getEndDate()));
      endDate.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      row.getChildren().add(endDateLabel);
      row.getChildren().add(endDate);

      column.getChildren().add(row);


      HBox remainingRow = new HBox(6);
      remainingRow.setAlignment(Pos.BASELINE_LEFT);
      Label remainingLabel = new Label("Remaining:");
      remainingLabel.setPrefWidth(LABEL_WIDTH);
      remainingLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-weight: bold;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      Label remaining = new Label(tournamentTable.remainingDays() + " days");
      remaining.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;" + TournamentHelper.getLabelCss(tournament, tournamentTable));

      remainingRow.getChildren().add(remainingLabel);
      remainingRow.getChildren().add(remaining);

      column.getChildren().add(remainingRow);
    }


    if (game != null) {
      if (game.getHighscoreType() == null) {
        Label error = new Label("No valid highscore found.");
        error.setStyle("-fx-padding: 3 6 3 6;");
        error.getStyleClass().add("error-title");
        column.getChildren().add(error);
      }
    }

    setPadding(new Insets(3, 0, 6, 0));
  }
}
