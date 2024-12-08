package de.mephisto.vpin.ui.friends;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.friends.panels.FriendRowPanelController;
import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

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
  private void onSearchKeyPressed(KeyEvent e) {
    String term = textfieldSearch.getText();
  }

  @FXML
  private void onClear() {
    textfieldSearch.setText("");
  }

  @FXML
  private void onInvite() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyBox.managedProperty().bindBidirectional(emptyBox.visibleProperty());
    loadingBox.managedProperty().bindBidirectional(loadingBox.visibleProperty());

    dataBox.setVisible(false);
    emptyBox.setVisible(false);
    loadingBox.setVisible(true);

    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      debouncer.debounce("search", () -> {
        Platform.runLater(() -> {
          String term = textfieldSearch.getText();

        });
      }, 300);
    });


    JFXFuture.supplyAsync(() -> {
          List<Cabinet> contacts = Studio.maniaClient.getContactClient().getContacts();
          List<Node> friendPanels = new ArrayList<>();
          for (Cabinet contact : contacts) {
            try {
              FXMLLoader loader = new FXMLLoader(FriendRowPanelController.class.getResource("friend-row-panel.fxml"));
              Pane node = loader.load();
              FriendRowPanelController friendController = loader.getController();
              friendController.setData(contact);
              friendPanels.add(node);
            }
            catch (Exception e) {
              LOG.error("Failed to loading friends data: " + e.getMessage(), e);
              WidgetFactory.showAlert(Studio.stage, "Error", "Error loading friends data: " + e.getMessage());
              return Collections.emptyList();
            }
          }
          return friendPanels;
        })
        .onErrorSupply(e -> {
          LOG.error("Loading friend list failed", e);
          Platform.runLater(() -> WidgetFactory.showAlert(stage, "Error", "Loading friend list failed: " + e.getMessage()));
          return Collections.emptyList();
        })
        .thenAcceptLater(data -> {
          if (data.isEmpty()) {
            emptyBox.setVisible(true);
            loadingBox.setVisible(false);
            return;
          }

          dataBox.setVisible(true);
          dataBox.getChildren().removeAll(dataBox.getChildren());
          dataBox.getChildren().addAll((Collection<? extends Node>) data);
        });

  }
}
