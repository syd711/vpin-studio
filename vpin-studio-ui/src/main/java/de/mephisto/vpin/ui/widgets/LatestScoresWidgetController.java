package de.mephisto.vpin.ui.widgets;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class LatestScoresWidgetController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(LatestScoresWidgetController.class);

  @FXML
  private VBox highscoreVBox;

  // Add a public no-args constructor
  public LatestScoresWidgetController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameRepresentation> games = client.getRecentlyPlayedGames(5);

    try {
      int count = 0;
      for (GameRepresentation game : games) {
        if (!game.getScores().isEmpty()) {
          GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
          GameMediaItemRepresentation wheelMedia = gameMedia.getMedia().get(PopperScreen.Wheel.name());
          if (wheelMedia == null) {
            continue;
          }

          FXMLLoader loader = new FXMLLoader(LatestScoreItemWidgetController.class.getResource("widget-latest-score-item.fxml"));
          Parent row = loader.load();
          LatestScoreItemWidgetController controller = loader.getController();
          controller.setData(game, game.getScores().get(0), wheelMedia);

          highscoreVBox.getChildren().add(row);
          count++;

          if (count == 10) {
            break;
          }
        }
      }
    } catch (IOException e) {
      LOG.error("Failed to create widget: " + e.getMessage(), e);
    }
  }
}