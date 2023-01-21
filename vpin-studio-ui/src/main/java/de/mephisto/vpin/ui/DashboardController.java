package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(DashboardController.class);

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetCompetition;

  @FXML
  private BorderPane widgetFinishedCompetitions;

  private WidgetCompetitionController activeCompetitionController;
  private WidgetLatestScoresController latestScoresController;
  private WidgetPlayerRankController playerRankController;

  // Add a public no-args constructor
  public DashboardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Dashboard"));

    try {
      FXMLLoader loader = new FXMLLoader(WidgetLatestScoresController.class.getResource("widget-latest-scores.fxml"));
      BorderPane root = loader.load();
      latestScoresController = loader.getController();
      root.setMaxHeight(Double.MAX_VALUE);
      widgetLatestScore.setLeft(root);
    } catch (IOException e) {
      LOG.error("Failed to load score widget: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionController.class.getResource("widget-active-competition.fxml"));
      BorderPane activeCompetitionBorderPane = loader.load();
      activeCompetitionBorderPane.setMaxWidth(Double.MAX_VALUE);
      activeCompetitionController = loader.getController();
      widgetCompetition.setTop(activeCompetitionBorderPane);

    } catch (IOException e) {
      LOG.error("Failed to load competitions widget: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetPlayerRankController.class.getResource("widget-player-rank.fxml"));
      BorderPane playersBorderPane = loader.load();
      playerRankController = loader.getController();
      playersBorderPane.setMaxWidth(Double.MAX_VALUE);
      widgetFinishedCompetitions.setCenter(playersBorderPane);
    } catch (IOException e) {
      LOG.error("Failed to load finished players widget: " + e.getMessage(), e);
    }

    NavigationController.setInitialController("scene-dashboard.fxml", this);
    onViewActivated();
  }

  @Override
  public void onViewActivated() {
    Platform.runLater(() -> {
      latestScoresController.refresh();
      playerRankController.refresh();
      activeCompetitionController.refresh();
    });
  }
}