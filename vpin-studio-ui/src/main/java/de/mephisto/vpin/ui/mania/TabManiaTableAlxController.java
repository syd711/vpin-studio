package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.mania.widgets.ManiaWidgetVPSTableAlxController;
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

public class TabManiaTableAlxController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TabManiaTableAlxController.class);

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetRight;

  private ManiaWidgetVPSTableAlxController tableAlxController;

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    tableAlxController.refresh();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(ManiaWidgetVPSTableAlxController.class.getResource("mania-widget-vps-table-alx.fxml"));
      BorderPane playersBorderPane = loader.load();
      tableAlxController = loader.getController();
      playersBorderPane.setMaxWidth(Double.MAX_VALUE);
      playersBorderPane.setMaxHeight(Double.MAX_VALUE);
      widgetRight.setCenter(playersBorderPane);
    } catch (IOException e) {
      LOG.error("Failed to load ManiaWidgetVPSTableAlxController widget: " + e.getMessage(), e);
    }

    onViewActivated(null);
  }
}
