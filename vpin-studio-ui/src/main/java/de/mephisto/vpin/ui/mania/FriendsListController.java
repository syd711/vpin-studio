package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetContact;
import de.mephisto.vpin.ui.HeaderResizeableController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.ui.mania.dialogs.ManiaDialogs;
import de.mephisto.vpin.ui.mania.panels.FriendCabinetRowPanelController;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.*;

public class FriendsListController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendsListController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TextField textfieldSearch;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyBox;

  @FXML
  private VBox loadingBox;

  @FXML
  private VBox emptySearchBox;

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    String term = textfieldSearch.getText();
  }

  @FXML
  private void onClear() {
    textfieldSearch.setText("");
  }

  @FXML
  private void onInvite() {
    ManiaDialogs.openAccountSearchDialog();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (!ManiaHelper.isRegistered()) {
      HeaderResizeableController.toggleManiaView();
      return;
    }

    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyBox.managedProperty().bindBidirectional(emptyBox.visibleProperty());
    loadingBox.managedProperty().bindBidirectional(loadingBox.visibleProperty());
    emptySearchBox.managedProperty().bindBidirectional(emptySearchBox.visibleProperty());

    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      debouncer.debounce("search", () -> {
        Platform.runLater(() -> {
          reload();
        });
      }, 500);
    });
    reload();
  }

  public void reload() {
    client.getManiaService().clearCache();

    dataBox.getChildren().removeAll(dataBox.getChildren());
    dataBox.setVisible(false);
    emptyBox.setVisible(false);
    emptySearchBox.setVisible(false);
    loadingBox.setVisible(true);

    JFXFuture.supplyAsync(() -> {
          List<CabinetContact> contacts = getFilteredContacts();
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
              LOG.error("Failed to loading friends data: " + e.getMessage(), e);
              Platform.runLater(() -> {
                WidgetFactory.showAlert(Studio.stage, "Error", "Error loading friends data: " + e.getMessage());
              });
              return Collections.emptyList();
            }
          }
          return friendPanels;
        })
        .onErrorSupply(e -> {
          LOG.error("Loading friend list failed", e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(stage, "Error", "Loading friend list failed: " + e.getMessage());
          });
          return Collections.emptyList();
        })
        .thenAcceptLater(data -> {
          loadingBox.setVisible(false);

          String term = textfieldSearch.getText();
          if (data.isEmpty() && !StringUtils.isEmpty(term)) {
            emptySearchBox.setVisible(true);
            return;
          }

          if (data.isEmpty()) {
            emptyBox.setVisible(true);
            return;
          }

          dataBox.setVisible(true);
          dataBox.getChildren().removeAll(dataBox.getChildren());
          dataBox.getChildren().addAll((Collection<? extends Node>) data);
        });
  }

  private List<CabinetContact> getFilteredContacts() {
    List<CabinetContact> contacts = maniaClient.getContactClient().getContacts(maniaClient.getCabinetClient().getDefaultCabinetCached().getId());
    String term = textfieldSearch.getText();
    if (!StringUtils.isEmpty(term)) {
      return contacts.stream().filter(c -> c.getDisplayName().toLowerCase().contains(term.toLowerCase())).collect(Collectors.toList());
    }
    return contacts;
  }
}
