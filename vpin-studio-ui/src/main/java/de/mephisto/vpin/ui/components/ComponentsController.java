package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ComponentsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentsController.class);

  @FXML
  private Parent root;

  @FXML
  private Tab overviewTab;

  @FXML
  private Tab vpxTab;

  @FXML
  private Tab mameTab;

  @FXML
  private Tab b2sTab;

  @FXML
  private Tab freezyTab;

  @FXML
  private Tab flexDMDTab;

  @FXML
  private Tab serumTab;

  @FXML
  private TabPane tabPane;


  @FXML
  private Pane alx1;

  // Add a public no-args constructor
  public ComponentsController() {
  }

  private void updateForTabSelection(int index) {
    if (index == 0) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Overview"));
    }
    else if (index == 1) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Visual Pinball"));
    }
    else if (index == 2) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "VPin MAME"));
    }
    else if (index == 3) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "B2S Server"));
    }
    else if (index == 4) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Freezy"));
    }
    else if (index == 5) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "FlexDMD"));
    }
    else if (index == 6) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager", "Serum"));
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setInitialController("scene-components.fxml", this, root);
    NavigationController.setBreadCrumb(Arrays.asList("System Manager"));
    try {
      FXMLLoader loader = new FXMLLoader(TabOverviewController.class.getResource("tab-overview.fxml"));
      Parent builtInRoot = loader.load();
      overviewTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TabMameController.class.getResource("tab-mame.fxml"));
      Parent builtInRoot = loader.load();
      mameTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tab: " + e.getMessage(), e);
    }

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      updateForTabSelection(t1.intValue());
    });

    updateForTabSelection(0);

    TileFactory.createAlxTiles(alx1);
  }

  @Override
  public void onViewActivated() {

  }
}