package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.players.WidgetPlayerScoreController;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerScoreLoadingProgressModel extends ProgressModel<PlayerRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerScoreLoadingProgressModel.class);
  private List<PlayerRepresentation> players;
  private final VBox highscoreList;
  private final Label noScoreLabel;

  private final Iterator<PlayerRepresentation> playerIterator;

  public PlayerScoreLoadingProgressModel(PlayerRepresentation playerRepresentation, VBox highscoreList, Label noScoreLabel) {
    super("Loading Player Highscores");
    this.players = Arrays.asList(playerRepresentation);
    this.highscoreList = highscoreList;
    this.noScoreLabel = noScoreLabel;
    this.playerIterator = players.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return players.size();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.playerIterator.hasNext();
  }

  @Override
  public PlayerRepresentation getNext() {
    return playerIterator.next();
  }

  @Override
  public String nextToString(PlayerRepresentation game) {
    return "";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, PlayerRepresentation player) {
    ScoreSummaryRepresentation playerScores = client.getPlayerService().getPlayerScores(player.getInitials());
    progressResultModel.getResults().add(playerScores);


    Platform.runLater(() -> {
      highscoreList.getChildren().removeAll(highscoreList.getChildren());
      highscoreList.getStyleClass().remove("media-container");
      if (playerScores.getScores().isEmpty()) {
        noScoreLabel.setVisible(true);
        noScoreLabel.setText("No scores found for this player.");
      }
      else {
        highscoreList.getStyleClass().add("media-container");
        noScoreLabel.setVisible(false);
        for (ScoreRepresentation playerScore : playerScores.getScores()) {
          GameRepresentation game = client.getGameService().getGameCached(playerScore.getGameId());
          if (game == null) {
            continue;
          }

          try {
            FXMLLoader loader = new FXMLLoader(WidgetPlayerScoreController.class.getResource("widget-highscore.fxml"));
            Pane row = loader.load();
            row.setPrefWidth(600);
            WidgetPlayerScoreController controller = loader.getController();
            controller.setData(game, playerScore);
            highscoreList.getChildren().add(row);
          } catch (IOException e) {
            LOG.error("failed to load score component: " + e.getMessage(), e);
          }
        }
      }
    });
  }
}
