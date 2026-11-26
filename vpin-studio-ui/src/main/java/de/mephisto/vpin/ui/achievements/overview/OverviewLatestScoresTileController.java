package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.ui.players.WidgetPlayerScoreController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Overview tile: the latest highscores, rendered with the same score tiles as the players view.
 */
public class OverviewLatestScoresTileController {
  private static final Logger LOG = LoggerFactory.getLogger(OverviewLatestScoresTileController.class);

  private static final int LIMIT = 12;

  @FXML
  private Label statusLabel;
  @FXML
  private VBox scoresBox;

  private record Entry(GameRepresentation game, ScoreRepresentation score) {
  }

  @FXML
  private void initialize() {
    statusLabel.managedProperty().bind(statusLabel.visibleProperty());
  }

  public void render() {
    scoresBox.getChildren().clear();
    statusLabel.setText(Messages.get("widgets.widget_latest_scores.loading_latest_scores"));
    statusLabel.setVisible(true);

    JFXFuture.supplyAsync(() -> {
      List<Entry> entries = new ArrayList<>();
      for (ScoreRepresentation score : client.getGameService().getRecentScores(LIMIT).getScores()) {
        GameRepresentation game = client.getGameService().getGameCached(score.getGameId());
        if (game != null) {
          entries.add(new Entry(game, score));
        }
      }
      return entries;
    }).thenAcceptLater(entries -> {
      scoresBox.getChildren().clear();
      for (Entry entry : entries) {
        try {
          FXMLLoader loader = new FXMLLoader(WidgetPlayerScoreController.class.getResource("widget-highscore.fxml"));
          loader.setResources(Messages.getBundle());
          Pane row = loader.load();
          WidgetPlayerScoreController controller = loader.getController();
          controller.setData(entry.game(), entry.score());
          scoresBox.getChildren().add(row);
        }
        catch (IOException e) {
          LOG.error("Failed to load score tile: {}", e.getMessage(), e);
        }
      }
      statusLabel.setText(Messages.get("widgets.widget_latest_scores.no_highscore_placeholder"));
      statusLabel.setVisible(scoresBox.getChildren().isEmpty());
    }).onErrorLater(e -> {
      LOG.error("Failed to load latest scores: {}", e.getMessage(), e);
      statusLabel.setText(Messages.get("widgets.widget_latest_scores.no_highscore_placeholder"));
    });
  }
}
