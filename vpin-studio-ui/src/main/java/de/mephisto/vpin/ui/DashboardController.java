package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetFinishedCompetitionsController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreSummaryRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DashboardController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(DashboardController.class);

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetCompetition;

  @FXML
  private BorderPane widgetFinishedCompetitions;

  @FXML
  private StackPane dashboardStack;

  private WidgetCompetitionController offlineCompetitionController;
  private WidgetFinishedCompetitionsController finishedCompetitionsController;
  private WidgetLatestScoresController latestScoresController;

  private BorderPane activeCompetitionBorderPane;
  private BorderPane finishedCompetitionsBorderPane;


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
      activeCompetitionBorderPane = loader.load();
      activeCompetitionBorderPane.setMaxWidth(Double.MAX_VALUE);
      offlineCompetitionController = loader.getController();
      widgetCompetition.setTop(activeCompetitionBorderPane);

    } catch (IOException e) {
      LOG.error("Failed to load competitions widget: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetFinishedCompetitionsController.class.getResource("widget-finished-competitions.fxml"));
      finishedCompetitionsBorderPane = loader.load();
      finishedCompetitionsController = loader.getController();
      finishedCompetitionsBorderPane.setMaxWidth(Double.MAX_VALUE);
      widgetFinishedCompetitions.setCenter(finishedCompetitionsBorderPane);
    } catch (IOException e) {
      LOG.error("Failed to load finished competitions widget: " + e.getMessage(), e);
    }

    NavigationController.setInitialController("scene-dashboard.fxml", this);
    onViewActivated();
  }

  @Override
  public void onViewActivated() {
    Platform.runLater(() -> {
      ScoreSummaryRepresentation scoreSummary = OverlayWindowFX.client.getRecentlyPlayedGames(10);
      latestScoresController.setScoreSummary(scoreSummary);

      List<CompetitionRepresentation> activeCompetitions = client.getActiveOfflineCompetitions();
      if (!activeCompetitions.isEmpty()) {
        offlineCompetitionController.setCompetition(activeCompetitions.get(0));
      }
      else {
        offlineCompetitionController.setCompetition(null);
      }

      List<CompetitionRepresentation> competitions = OverlayWindowFX.client.getFinishedCompetitions(3);
      finishedCompetitionsController.setCompetitions(competitions);

//      if (activeCompetitions.isEmpty()) {
//        widgetCompetition.setTop(finishedCompetitionsBorderPane);
//        widgetFinishedCompetitions.setTop(null);
//      }
//      else {
//
//      }
    });
  }
}