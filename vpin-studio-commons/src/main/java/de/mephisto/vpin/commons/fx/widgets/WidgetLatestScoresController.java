package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetLatestScoresController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox highscoreVBox;

  @FXML
  private BorderPane root;

  @FXML
  private Pane listRoot;

  @FXML
  private StackPane viewStack;

  private Parent loadingOverlay;

  // Add a public no-args constructor
  public WidgetLatestScoresController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay-plain.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Latest Scores...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    listRoot.setVisible(false);
    if (!viewStack.getChildren().contains(loadingOverlay)) {
      viewStack.getChildren().add(loadingOverlay);
    }

    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    MonitorInfo screenBounds = ServerFX.client.getScreenInfo(overlaySettings.getOverlayScreenId());

    new Thread(() -> {
      int limit = 12;
      if (screenBounds.getWidth() > 2000 && screenBounds.getWidth() < 3000) {
        limit = 10;
      }
      else if (screenBounds.getWidth() < 2000) {
        limit = 8;
      }

      final List<Pane> scoresPanels = new ArrayList<>();
      try {
        ScoreSummaryRepresentation scoreSummary = ServerFX.client.getRecentScores(limit);

        List<ScoreRepresentation> scores = scoreSummary.getScores();
        if (scores.isEmpty()) {
          Label label = new Label("                            No highscore record yet.\nThe history of newly achieved highscores will be shown here.");
          label.setPadding(new Insets(80, 0, 0, 100));
          label.getStyleClass().add("preference-description");
//          scoresPanels.add(label);
        }
        else {
          for (ScoreRepresentation score : scores) {
            GameRepresentation game = ServerFX.client.getGameCached(score.getGameId());
            FrontendMediaRepresentation frontendMedia = ServerFX.client.getFrontendMedia(score.getGameId());
            if (game == null) {
              continue;
            }
            FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
            Pane row = loader.load();
            row.setPrefWidth(root.getPrefWidth() - 24);
            WidgetLatestScoreItemController controller = loader.getController();
            controller.setData(game, frontendMedia, score);
            scoresPanels.add(row);
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to create widget: " + e.getMessage(), e);
      }

      Platform.runLater(() -> {
        highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

        for (Pane scoresPanel : scoresPanels) {
          highscoreVBox.getChildren().add(scoresPanel);
        }

        viewStack.getChildren().remove(loadingOverlay);
        listRoot.setVisible(true);
      });
    }).start();
  }
}