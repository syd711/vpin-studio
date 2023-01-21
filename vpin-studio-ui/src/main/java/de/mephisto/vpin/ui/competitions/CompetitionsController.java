package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreListRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.players.BuiltInPlayersController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class CompetitionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionsController.class);

  private CompetitionsOfflineController offlineController;
  private CompetitionsOnlineController onlineController;

  @FXML
  private Tab offlineTab;

  @FXML
  private Tab onlineTab;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @Override
  public void onViewActivated() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsOfflineController.class.getResource("tab-competitions-offline.fxml"));
      Parent offline = loader.load();
      offlineController = loader.getController();
//      builtInPlayersController.setPlayersController(this);
      offlineTab.setContent(offline);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(CompetitionsOnlineController.class.getResource("tab-competitions-online.fxml"));
      Parent offline = loader.load();
      onlineController = loader.getController();
//      builtInPlayersController.setPlayersController(this);
      onlineTab.setContent(offline);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }
  }
}