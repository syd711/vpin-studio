package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class OverlayController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private WidgetCompetitionController activeCompetitionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetLatestScoresController latestScoresController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetPlayerRankController playersController; //fxml magic! Not unused -> id + "Controller"

  // Add a public no-args constructor
  public OverlayController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void refreshData() {
    LOG.info("Refreshing overlay.");
    PreferenceEntryRepresentation systemName = OverlayWindowFX.client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = systemName.getValue();
    if(StringUtils.isEmpty(name) || name.equals("null") ) {
      name = UIDefaults.VPIN_NAME;
    }
    titleLabel.setText(name);


    CompetitionRepresentation c = OverlayWindowFX.client.getActiveCompetition(CompetitionType.OFFLINE);
    activeCompetitionController.refresh(c);
    latestScoresController.refresh();
//    finishedCompetitionsController.refresh();
    playersController.refresh();
  }
}