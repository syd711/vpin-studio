package de.mephisto.vpin.ui.friends;

import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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

  private TournamentSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    settings = client.getTournamentsService().getSettings();
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    CabinetSettings cabinetSettings = cabinet.getSettings();

    showOnlineStatusCheckbox.setSelected(settings.isShowOnlineStatus());
    showOnlineStatusCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setShowOnlineStatus(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
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
  }
}
