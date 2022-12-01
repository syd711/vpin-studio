package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.ui.widgets.WidgetOfflineCompetitionController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
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
  private BorderPane widgetRoot;

  @FXML
  private BorderPane widgetTop;


  // Add a public no-args constructor
  public DashboardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Dashboard"));

    try {
      FXMLLoader loader = new FXMLLoader(WidgetLatestScoresController.class.getResource("widget-latest-scores.fxml"));
      BorderPane root = loader.load();
      root.setMaxHeight(Double.MAX_VALUE);
      widgetRoot.setLeft(root);
    } catch (IOException e) {
      LOG.error("Failed to load score widget: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetOfflineCompetitionController.class.getResource("widget-offline-competition.fxml"));
      BorderPane root = loader.load();
      root.setMaxWidth(Double.MAX_VALUE);
      WidgetOfflineCompetitionController controller = loader.getController();

      List<CompetitionRepresentation> activeOfflineCompetitions = client.getActiveOfflineCompetitions();
      if(!activeOfflineCompetitions.isEmpty()) {
        controller.setCompetition(activeOfflineCompetitions.get(0));
        widgetTop.setTop(root);
      }

    } catch (IOException e) {
      LOG.error("Failed to load score widget: " + e.getMessage(), e);
    }

//    webWidget.getEngine().load("https://virtualpinballchat.com/#/weekly-rankings/competition-corner");
  }

}