package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetLatestScoresController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetLatestScoresController.class);

  @FXML
  private VBox highscoreVBox;

  @FXML
  private BorderPane root;

  // Add a public no-args constructor
  public WidgetLatestScoresController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  public void setScoreSummary(ScoreSummaryRepresentation scoreSummary) {
    highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());
    try {
      int count = 0;
      List<ScoreRepresentation> scores = scoreSummary.getScores();
      for (ScoreRepresentation score : scores) {
        GameRepresentation game = OverlayWindowFX.client.getGame(score.getGameId());
        GameMediaRepresentation gameMedia = game.getGameMedia();

        GameMediaItemRepresentation wheelMedia = gameMedia.getMedia().get(PopperScreen.Wheel.name());
        if (wheelMedia == null) {
          continue;
        }

        FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
        BorderPane row = loader.load();
        row.setPrefWidth(root.getPrefWidth() - 48);
        WidgetLatestScoreItemController controller = loader.getController();
        controller.setData(game, score, wheelMedia);

        highscoreVBox.getChildren().add(row);
        count++;

        if (count == 10) {
          break;
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to create widget: " + e.getMessage(), e);
    }
  }
}