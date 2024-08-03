package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ManiaController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaController.class);

  @FXML
  private Parent root;

  @FXML
  private Tab overviewTab;

  @FXML
  private Tab tablesTab;

  @FXML
  private Tab tableAlxTab;

  @FXML
  private Tab tablePlayerStatsTab;

  @FXML
  private TabPane tabPane;

  private TabManiaOverviewController overviewTabController;
  private TabManiaTableScoresController tableScoresTabController;
  private TabManiaTableAlxController tableAlxTabController;
  private TabManiaPlayerStatsController playerStatsTabController;

  // Add a public no-args constructor
  public ManiaController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    EventManager.getInstance().addListener(this);

    preferencesChanged(PreferenceType.uiSettings);

    NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Overview"));
    try {
      FXMLLoader loader = new FXMLLoader(TabManiaOverviewController.class.getResource("tab-overview.fxml"));
      Parent builtInRoot = loader.load();
      overviewTabController = loader.getController();
      overviewTabController.setManiaController(this);
      overviewTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TabManiaTableScoresController.class.getResource("tab-table-scores.fxml"));
      Parent builtInRoot = loader.load();
      tableScoresTabController = loader.getController();
      tablesTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TabManiaTableAlxController.class.getResource("tab-table-alx.fxml"));
      Parent builtInRoot = loader.load();
      tableAlxTabController = loader.getController();
      tableAlxTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TabManiaPlayerStatsController.class.getResource("tab-player-stats.fxml"));
      Parent builtInRoot = loader.load();
      playerStatsTabController = loader.getController();
      playerStatsTabController.setManiaController(this);
      tablePlayerStatsTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      updateForTabSelection(t1);
    });

    updateForTabSelection(0);
  }

  private void updateForTabSelection(Number index) {
    if (index.intValue() == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Player Ranking"));
      overviewTabController.onViewActivated(null);
    }
    else if (index.intValue() == 1) {
      NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Table Ranking"));
      tableScoresTabController.onViewActivated(null);
    }
    else if (index.intValue() == 2) {
      NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Table Statistics"));
      tableAlxTabController.onViewActivated(null);
    }
    else if (index.intValue() == 3) {
      NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Player Statistics"));
      playerStatsTabController.onViewActivated(null);
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    updateForTabSelection(tabPane.getSelectionModel().getSelectedIndex());
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.uiSettings)) {

    }
  }

  public void selectVpsTable(VpsTable vpsTable) {
    tabPane.getSelectionModel().select(1);
    tableScoresTabController.selectVpsTable(vpsTable);
  }
}