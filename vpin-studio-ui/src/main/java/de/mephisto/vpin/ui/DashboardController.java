package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.widgets.LatestScoresWidgetController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(DashboardController.class);

  @FXML
  private BorderPane widgetRoot;

  private Parent root;

  // Add a public no-args constructor
  public DashboardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    try {
      FXMLLoader loader = new FXMLLoader(LatestScoresWidgetController.class.getResource("widget-latest-scores.fxml"));
      root = loader.load();
      widgetRoot.setLeft(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}