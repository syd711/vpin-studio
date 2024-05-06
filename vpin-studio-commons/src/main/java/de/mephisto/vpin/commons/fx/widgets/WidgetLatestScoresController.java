package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
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
      if (screenBounds.getWidth() > 2000 && screenBounds.getWidth() < 3000) {
        limit = 10;
      }
      else if (screenBounds.getWidth() < 2000) {
        limit = 8;
      }

      ScoreSummaryRepresentation scoreSummary = ServerFX.client.getRecentScores(limit);
      Platform.runLater(() -> {
        highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

        try {
          List<ScoreRepresentation> scores = scoreSummary.getScores();
          if (scores.isEmpty()) {
            Label label = new Label("                            No highscore record yet.\nThe history of newly achieved highscores will be shown here.");
            label.setPadding(new Insets(80, 0, 0, 100));
            label.getStyleClass().add("preference-description");
            highscoreVBox.getChildren().add(label);
          }
          else {
            for (ScoreRepresentation score : scores) {
              GameRepresentation game = ServerFX.client.getGameCached(score.getGameId());
              if(game == null) {
                continue;
              }

              FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
              Pane row = loader.load();
              row.setPrefWidth(root.getPrefWidth() - 24);
              WidgetLatestScoreItemController controller = loader.getController();
              controller.setData(game, score);

              highscoreVBox.getChildren().add(row);
            }
          }

        } catch (IOException e) {
          LOG.error("Failed to create widget: " + e.getMessage(), e);
        }

        viewStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }
}