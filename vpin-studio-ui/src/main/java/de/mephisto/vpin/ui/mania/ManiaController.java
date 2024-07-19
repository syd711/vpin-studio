package de.mephisto.vpin.ui.mania;

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
  private TabPane tabPane;
  private TabManiaOverviewController overviewController;

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
      overviewController = loader.getController();
      overviewTab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

//    loadTab(vpxTab, "tab-vpx.fxml");
//    loadTab(b2sTab, "tab-b2s.fxml");
//    loadTab(mameTab, "tab-mame.fxml");
//    loadTab(flexDMDTab, "tab-flexdmd.fxml");
//    loadTab(freezyTab, "tab-freezy.fxml");
//    loadTab(serumTab, "tab-serum.fxml");
//
//    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
//      updateForTabSelection(t1.intValue());
//    });
//
//    updateForTabSelection(0);
  }

  private void loadTab(Tab tab, String file) {
    try {
      FXMLLoader loader = new FXMLLoader(ManiaController.class.getResource(file));
      Parent builtInRoot = loader.load();
      tab.setContent(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("VPin Mania", "Overview"));
    overviewController.onViewActivated(options);
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.uiSettings)) {

    }
  }
}