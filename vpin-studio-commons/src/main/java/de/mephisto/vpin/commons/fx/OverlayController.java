package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetExternalPageController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class OverlayController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private StackPane rootStack;

  @FXML
  private Label titleLabel;

  @FXML
  private WidgetCompetitionController offlineCompetitionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetCompetitionController discordCompetitionController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetLatestScoresController latestScoresController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetPlayerRankController playersController; //fxml magic! Not unused -> id + "Controller"

  @FXML
  private WidgetExternalPageController externalPageController; //fxml magic! Not unused -> id + "Controller"

  // Add a public no-args constructor
  public OverlayController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void refreshData() {
    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    MonitorInfo screen = ServerFX.client.getSystemService().getScreenInfo(overlaySettings.getOverlayScreenId());
    if(screen.getWidth() < screen.getHeight()) {
      rootStack.setRotate(0);
    }

    LOG.info("Refreshing overlay.");
    PreferenceEntryRepresentation systemName = ServerFX.client.getPreferenceService().getPreference(PreferenceNames.SYSTEM_NAME);
    String name = systemName.getValue();
    if (StringUtils.isEmpty(name) || name.equals("null")) {
      name = UIDefaults.VPIN_NAME;
    }
    titleLabel.setText(name);


    if (offlineCompetitionController != null) {
      CompetitionRepresentation c = ServerFX.client.getCompetitionService().getActiveCompetition(CompetitionType.OFFLINE);
      offlineCompetitionController.setCompetitionType(CompetitionType.OFFLINE);
      offlineCompetitionController.refresh(c);
    }

    if (discordCompetitionController != null) {
      CompetitionRepresentation c = ServerFX.client.getCompetitionService().getActiveCompetition(CompetitionType.DISCORD);
      discordCompetitionController.setCompetitionType(CompetitionType.DISCORD);
      discordCompetitionController.refresh(c);
    }

    if (latestScoresController != null) {
      latestScoresController.refresh();
    }

    if (externalPageController != null) {
      externalPageController.refresh();
    }

    if (playersController != null) {
      playersController.refresh();
    }
  }
}