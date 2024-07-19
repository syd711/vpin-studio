package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.mania.model.TableScoreDetails;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
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

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaWidgetLatestScoresController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaWidgetLatestScoresController.class);

  @FXML
  private VBox highscoreVBox;

  @FXML
  private BorderPane root;

  @FXML
  private StackPane viewStack;

  private Parent loadingOverlay;

  // Add a public no-args constructor
  public ManiaWidgetLatestScoresController() {
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
    if(!viewStack.getChildren().contains(loadingOverlay)) {
      viewStack.getChildren().add(loadingOverlay);
    }

    new Thread(() -> {
      List<TableScoreDetails> recentScores = maniaClient.getHighscoreClient().getRecentHighscores();
      Platform.runLater(() -> {
        highscoreVBox.getChildren().removeAll(highscoreVBox.getChildren());

        try {
          if (recentScores.isEmpty()) {
            Label label = new Label("                            No highscore record yet.\nThe history of newly achieved highscores will be shown here.");
            label.setPadding(new Insets(80, 0, 0, 100));
            label.getStyleClass().add("preference-description");
            highscoreVBox.getChildren().add(label);
          }
          else {
            for (TableScoreDetails score : recentScores) {
              VpsTable vpsTable = client.getVpsService().getTableById(score.getVpsTableId());
              if(vpsTable == null) {
                continue;
              }

              FXMLLoader loader = new FXMLLoader(ManiaWidgetLatestScoreItemController.class.getResource("mania-widget-latest-score-item.fxml"));
              Pane row = loader.load();
              row.setPrefWidth(root.getPrefWidth() - 24);
              ManiaWidgetLatestScoreItemController controller = loader.getController();
              controller.setData(vpsTable, score);

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