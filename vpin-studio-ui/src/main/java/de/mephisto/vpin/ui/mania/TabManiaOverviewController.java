package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetLatestScoresController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetPlayerRankController;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabManiaOverviewController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TabManiaOverviewController.class);

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetRight;

  private ManiaWidgetLatestScoresController latestScoresController;
  private ManiaWidgetPlayerRankController playerRankController;
  private ManiaController maniaController;

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    latestScoresController.refresh();
    playerRankController.refresh();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetLatestScoresController.class.getResource("mania-widget-latest-scores.fxml"));
      BorderPane root = loader.load();
      latestScoresController = loader.getController();
      latestScoresController.setOverviewController(this);
      root.setMaxHeight(Double.MAX_VALUE);
      widgetLatestScore.setLeft(root);
    } catch (IOException e) {
      LOG.error("Failed to load score widget: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetPlayerRankController.class.getResource("mania-widget-player-rank.fxml"));
      BorderPane playersBorderPane = loader.load();
      playerRankController = loader.getController();
      playersBorderPane.setMaxWidth(Double.MAX_VALUE);
      playersBorderPane.setMaxHeight(Double.MAX_VALUE);
      widgetRight.setCenter(playersBorderPane);
    } catch (IOException e) {
      LOG.error("Failed to load finished players widget: " + e.getMessage(), e);
    }
  }

  public void selectVpsTable(VpsTable vpsTable) {
    maniaController.selectVpsTable(vpsTable);
  }

  public void setManiaController(ManiaController maniaController) {
    playerRankController.setManiaController(maniaController);
    this.maniaController = maniaController;
  }
}
