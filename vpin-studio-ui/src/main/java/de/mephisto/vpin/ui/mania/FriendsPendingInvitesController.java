package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetContact;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.mania.panels.FriendCabinetRowPanelController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.maniaClient;
import static de.mephisto.vpin.ui.Studio.stage;

public class FriendsPendingInvitesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendsPendingInvitesController.class);

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyBox;

  @FXML
  private VBox loadingBox;

  @FXML
  private VBox disabledBox;

  @FXML
  private void onReload() {
    reload();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyBox.managedProperty().bindBidirectional(emptyBox.visibleProperty());
    loadingBox.managedProperty().bindBidirectional(loadingBox.visibleProperty());
    disabledBox.managedProperty().bindBidirectional(disabledBox.visibleProperty());
    reload();
  }

  public void reload() {
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    CabinetSettings cabinetSettings = cabinet.getSettings();

    dataBox.getChildren().removeAll(dataBox.getChildren());
    dataBox.setVisible(false);
    disabledBox.setVisible(false);
    emptyBox.setVisible(false);
    loadingBox.setVisible(true);

    if (!cabinetSettings.isSearchable()) {
      loadingBox.setVisible(false);
      disabledBox.setVisible(true);
      return;
    }

    JFXFuture.supplyAsync(() -> {
          List<CabinetContact> contacts = Studio.maniaClient.getContactClient().getInvites();
          List<Node> friendPanels = new ArrayList<>();
          for (CabinetContact contact : contacts) {
            try {
              FXMLLoader loader = new FXMLLoader(FriendCabinetRowPanelController.class.getResource("friend-cabinet-row-panel.fxml"));
              Pane node = loader.load();
              FriendCabinetRowPanelController friendController = loader.getController();
              friendController.setData(this, contact);
              friendPanels.add(node);
            }
            catch (Exception e) {
              LOG.error("Failed to loading invites data: " + e.getMessage(), e);
              Platform.runLater(() -> {
                WidgetFactory.showAlert(Studio.stage, "Error", "Error loading invites data: " + e.getMessage());
              });
              return Collections.emptyList();
            }
          }
          return friendPanels;
        })
        .onErrorSupply(e -> {
          LOG.error("Loading invites list failed", e);
          Platform.runLater(() -> {
            loadingBox.setVisible(false);
            WidgetFactory.showAlert(stage, "Error", "Loading invites list failed: " + e.getMessage());
          });
          return Collections.emptyList();
        })
        .thenAcceptLater(data -> {
          loadingBox.setVisible(false);
          if (data.isEmpty()) {
            emptyBox.setVisible(true);
            return;
          }

          dataBox.setVisible(true);
          dataBox.getChildren().removeAll(dataBox.getChildren());
          dataBox.getChildren().addAll((Collection<? extends Node>) data);
        });
  }
}
