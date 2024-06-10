package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(AlxController.class);

  @FXML
  private VBox mostPlayedWidget;

  @FXML
  private VBox timePlayedWidget;

  @FXML
  private VBox scoresWidget;

  @FXML
  private VBox tileList;

  @FXML
  private BorderPane root;


  // Add a public no-args constructor
  public AlxController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      AlxSummary alxSummary = client.getAlxService().getAlxSummary();
      List<TableAlxEntry> entries = alxSummary.getEntries();
      AlxFactory.createMostPlayed(mostPlayedWidget, entries);
      AlxFactory.createLongestPlayed(timePlayedWidget, entries);
      AlxFactory.createRecordedScores(scoresWidget, entries);
      AlxFactory.createTotalTimeTile(tileList, entries);
      AlxFactory.createTotalGamesPlayedTile(tileList, entries);
      AlxFactory.createTotalScoresTile(tileList, entries);
      AlxFactory.createTotalHighScoresTile(tileList, entries);
      Date alxStartDate = alxSummary.getStartDate()!=null? alxSummary.getStartDate(): 
        new Date(System.currentTimeMillis() - 1 * 365 * 24 * 3600 * 1000);
      AlxFactory.createAvgWeekTimeTile(tileList, entries, alxStartDate);
    } catch (Exception e) {
      LOG.error("Failed to initialize dashboard: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize dashboard: " + e.getMessage(), "Please submit an issue on Discord for this.");
    }

    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));
  }

  @Override
  public void onViewActivated() {
    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));
  }
}