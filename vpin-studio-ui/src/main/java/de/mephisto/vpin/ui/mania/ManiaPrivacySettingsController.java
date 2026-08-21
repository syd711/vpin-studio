package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.HeaderResizeableController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.panels.CabinetRowPanelController;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class ManiaPrivacySettingsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaPrivacySettingsController.class);

  @FXML
  private CheckBox showOnlineStatusCheckbox;

  @FXML
  private CheckBox showActiveGameCheckbox;

  @FXML
  private CheckBox searchableCheckbox;

  @FXML
  private CheckBox submitAllCheckbox;

  @FXML
  private CheckBox submitTablesCheckbox;

  @FXML
  private CheckBox submitAllRatingsCheckbox;

  @FXML
  private CheckBox submitPlayedCountCheckbox;

  @FXML
  private Button syncTablesBtn;

  @FXML
  private Button syncScoresBtn;

  @FXML
  private VBox playersBox;

  private ManiaSettings settings;

  private void showSyncPrompt() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.synchronize_cabinet"), Messages.get("dialog.you_privacy_settings_have_been_changed_do"), Messages.get("dialog.the_data_is_send_anonymously_and_will"), Messages.get("dialog.synchronize_data"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ManiaHelper.runScoreSynchronization(false);
    }
  }

  @FXML
  private void onHighscoreSync() {
    ManiaHelper.runScoreSynchronization(true);
  }

  @FXML
  private void onTablesSync() {
    ManiaHelper.runTablesSynchronization();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (!ManiaHelper.isRegistered()) {
      HeaderResizeableController.toggleManiaView();
      return;
    }

    settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
    Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();

    syncTablesBtn.setDisable(true);
    syncScoresBtn.setDisable(true);

    if (cabinet == null) {
      return;
    }

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
          client.getPreferenceService().setJsonPreference(settings);
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
          client.getPreferenceService().setJsonPreference(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    syncTablesBtn.setDisable(!settings.isSubmitTables());

    submitTablesCheckbox.setSelected(settings.isSubmitTables());
    submitTablesCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          submitAllRatingsCheckbox.setDisable(!newValue);
          syncTablesBtn.setDisable(!newValue);
          settings.setSubmitTables(newValue);
          client.getPreferenceService().setJsonPreference(settings);
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


    submitAllRatingsCheckbox.setSelected(settings.isSubmitRatings());
    submitPlayedCountCheckbox.setSelected(settings.isSubmitPlayed());
    submitAllCheckbox.setSelected(settings.isSubmitAllScores());
    submitAllRatingsCheckbox.setDisable(submitTablesCheckbox.isDisable());

    syncScoresBtn.setDisable(!settings.isSubmitAllScores());
    submitAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          syncScoresBtn.setDisable(!newValue);
          ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          settings.setSubmitAllScores(newValue);
          client.getPreferenceService().setJsonPreference(settings);
          if (newValue) {
            showSyncPrompt();
          }
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    submitAllRatingsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          settings.setSubmitRatings(newValue);
          client.getPreferenceService().setJsonPreference(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    submitPlayedCountCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
          settings.setSubmitPlayed(newValue);
          client.getPreferenceService().setJsonPreference(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        }
      }
    });

    try {
      FXMLLoader loader = new FXMLLoader(CabinetRowPanelController.class.getResource("cabinet-row-panel.fxml"));
      loader.setResources(Messages.getBundle());
      Pane node = loader.load();
      CabinetRowPanelController friendController = loader.getController();
      friendController.setData(this, maniaClient.getCabinetClient().getDefaultCabinetCached());
      playersBox.getChildren().add(node);
    }
    catch (Exception e) {
      LOG.error(Messages.get("dialog.error_loading_cabinet_player_data") + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.error_loading_cabinet_player_data") + e.getMessage());
      });
    }
  }
}
