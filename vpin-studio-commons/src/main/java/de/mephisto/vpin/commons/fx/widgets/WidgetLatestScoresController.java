package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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

  @FXML
  private StackPane viewStack;

  private Parent loadingOverlay;

  // Add a public no-args constructor
  public WidgetLatestScoresController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Latest Scores...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    viewStack.getChildren().add(loadingOverlay);
    new Thread(() -> {
      int limit = 12;
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      if(screenBounds.getWidth() > 2000 && screenBounds.getWidth() < 3000) {
        limit = 10;
      }
      else if(screenBounds.getWidth() < 2000) {
        limit = 8;
      }

      ScoreSummaryRepresentation scoreSummary = OverlayWindowFX.client.getRecentlyPlayedGames(limit);
      Platform.runLater(() -> {
        highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

        try {
          List<ScoreRepresentation> scores = scoreSummary.getScores();
          for (ScoreRepresentation score : scores) {
            GameRepresentation game = OverlayWindowFX.client.getGame(score.getGameId());
            GameMediaRepresentation gameMedia = game.getGameMedia();
            GameMediaItemRepresentation wheelMedia = gameMedia.getMedia().get(PopperScreen.Wheel.name());
            if (wheelMedia == null) {
              continue;
            }

            FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
            Pane row = loader.load();
            row.setPrefWidth(root.getPrefWidth() - 24);
            WidgetLatestScoreItemController controller = loader.getController();
            controller.setData(game, score);

            highscoreVBox.getChildren().add(row);
          }
        } catch (IOException e) {
          LOG.error("Failed to create widget: " + e.getMessage(), e);
        }

        viewStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }
}