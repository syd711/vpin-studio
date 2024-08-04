package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetPlayerStatsController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabManiaPlayerStatsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TabManiaPlayerStatsController.class);

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetRight;

  private ManiaWidgetPlayerStatsController playerStatsWidgetController;
  private ManiaController maniaController;

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    playerStatsWidgetController.onViewActivated(options);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetPlayerStatsController.class.getResource("mania-widget-player-stats.fxml"));
      BorderPane root = loader.load();
      playerStatsWidgetController = loader.getController();
      root.setMaxWidth(Double.MAX_VALUE);
      root.setMaxHeight(Double.MAX_VALUE);
      widgetRight.setCenter(root);
    } catch (IOException e) {
      LOG.error("Failed to load ManiaWidgetPlayerStatsController widget: " + e.getMessage(), e);
    }
  }

  public void setManiaController(ManiaController maniaController) {
    this.maniaController = maniaController;
    playerStatsWidgetController.setManiaController(maniaController);
  }
}
