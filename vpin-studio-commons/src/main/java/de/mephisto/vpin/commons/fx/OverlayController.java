package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.widgets.WidgetOfflineCompetitionController;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OverlayController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayController.class);

  @FXML
  private BorderPane widgetTop;

  @FXML
  private WidgetOfflineCompetitionController offlineCompetitionController; //fxml magic! Not unused -> id + "Controller"

  // Add a public no-args constructor
  public OverlayController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<CompetitionRepresentation> activeOfflineCompetitions = OverlayWindowFX.client.getActiveOfflineCompetitions();
    if (!activeOfflineCompetitions.isEmpty()) {
      offlineCompetitionController.setCompetition(activeOfflineCompetitions.get(0));
    }
  }

}