package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerDialogController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class TablesAssetViewSidebarController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox assetSidebarVBox;

  private TableAssetManagerDialogController dialogController;

  public void setVisible(boolean b) {
    this.assetSidebarVBox.setVisible(b);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    assetSidebarVBox.managedProperty().bindBidirectional(assetSidebarVBox.visibleProperty());
    assetSidebarVBox.setVisible(false);

    try {
      FXMLLoader loader = new FXMLLoader(TableAssetManagerDialogController.class.getResource("dialog-table-asset-manager-embedded.fxml"));
      Parent root = loader.load();
      dialogController = loader.getController();
      assetSidebarVBox.getChildren().add(root);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }
  }

  public void setGame(TableOverviewController tableOverviewController, GameRepresentation game, VPinScreen screen) {
    dialogController.setGame(Studio.stage, tableOverviewController, game, screen, true);
  }

  public void refreshTableMediaView() {
    dialogController.refreshTableMediaView();
  }
}
