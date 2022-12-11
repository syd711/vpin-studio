package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetFinishedCompetitionsController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OverlayController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayController.class);

  @FXML
  private BorderPane rotatedRoot;

  @FXML
  private Label titleLabel;

  @FXML
  private WidgetFinishedCompetitionsController finishedCompetitionsController; //fxml magic! Not unused -> id + "Controller"@FXML

  @FXML
  private WidgetCompetitionController activeCompetitionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetLatestScoresController latestScoresController; //fxml magic! Not unused -> id + "Controller"

  // Add a public no-args constructor
  public OverlayController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void refreshData() {
    PreferenceEntryRepresentation systemName = OverlayWindowFX.client.getPreference(PreferenceNames.SYSTEM_NAME);
    titleLabel.setText(systemName.getValue());

    activeCompetitionController.refresh();
    latestScoresController.refresh();

    List<CompetitionRepresentation> competitions = OverlayWindowFX.client.getFinishedCompetitions(3);
    finishedCompetitionsController.setCompetitions(competitions);
  }
}