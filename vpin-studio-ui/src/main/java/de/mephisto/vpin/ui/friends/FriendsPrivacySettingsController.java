package de.mephisto.vpin.ui.friends;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.friends.panels.FriendCabinetRowPanelController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class FriendsPrivacySettingsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendsPrivacySettingsController.class);

  @FXML
  private CheckBox showOnlineStatusCheckbox;

  @FXML
  private CheckBox showActiveGameCheckbox;

  @FXML
  private CheckBox searchableCheckbox;

  @FXML
  private VBox playersBox;

  private TournamentSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getTournamentsService().getSettings();
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    CabinetSettings cabinetSettings = cabinet.getSettings();

    showActiveGameCheckbox.setDisable(!settings.isShowOnlineStatus());

    showOnlineStatusCheckbox.setSelected(settings.isShowOnlineStatus());
    showOnlineStatusCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setShowOnlineStatus(newValue);
          if (!newValue) {
            settings.setShowActiveGameStatus(false);
          }
          settings = client.getTournamentsService().saveSettings(settings);
          showActiveGameCheckbox.setDisable(!settings.isShowOnlineStatus());
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    showActiveGameCheckbox.setSelected(settings.isShowActiveGameStatus());
    showActiveGameCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setShowActiveGameStatus(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    searchableCheckbox.setSelected(cabinetSettings.isSearchable());
    searchableCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          cabinetSettings.setSearchable(newValue);
          maniaClient.getCabinetClient().update(cabinet);
        }
        catch (Exception e) {
          LOG.error("Failed to save cabinet data: " + e.getMessage(), e);
        }
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(FriendCabinetRowPanelController.class.getResource("friend-cabinet-row-panel.fxml"));
      Pane node = loader.load();
      FriendCabinetRowPanelController friendController = loader.getController();
      friendController.setData(this, maniaClient.getCabinetClient().getCabinetCached());
      playersBox.getChildren().add(node);
    }
    catch (Exception e) {
      LOG.error("Error loading cabinet player data: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Error loading cabinet player data: " + e.getMessage());
      });
    }
  }
}
