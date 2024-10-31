package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class BackglassPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassPreferencesController.class);

  @FXML
  private CheckBox pluginsCheckbox;

  @FXML
  private CheckBox backglassMissingCheckbox;

  @FXML
  private CheckBox fuzzyMatchingCheckbox;

  @FXML
  private CheckBox startModeCheckbox;

  @FXML
  private Label noMatchFound;

  private DirectB2ServerSettings backglassServerSettings;

  private boolean saveEnabled = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      backglassServerSettings = Studio.client.getBackglassServiceClient().getServerSettings();
      boolean serverInstalled = backglassServerSettings != null;

      noMatchFound.setVisible(!serverInstalled);
      pluginsCheckbox.setDisable(!serverInstalled);
      backglassMissingCheckbox.setDisable(!serverInstalled);
      fuzzyMatchingCheckbox.setDisable(!serverInstalled);
      startModeCheckbox.setDisable(!serverInstalled);

      if (serverInstalled) {
        pluginsCheckbox.setSelected(backglassServerSettings.isPluginsOn());
        pluginsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setPluginsOn(newValue);
          saveSettings();
        });

        backglassMissingCheckbox.setSelected(backglassServerSettings.isShowStartupError());
        backglassMissingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setShowStartupError(newValue);
          saveSettings();
        });

        fuzzyMatchingCheckbox.setSelected(backglassServerSettings.isDisableFuzzyMatching());
        fuzzyMatchingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setDisableFuzzyMatching(newValue);
          saveSettings();
        });

        startModeCheckbox.setSelected(backglassServerSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE);
        startModeCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          int mode = DirectB2ServerSettings.EXE_START_MODE;
          if (!newValue) {
            mode = DirectB2ServerSettings.STANDARD_START_MODE;
          }
          backglassServerSettings.setDefaultStartMode(mode);
          saveSettings();
        });

        saveEnabled = true;
      }
    } catch (Exception e) {
      LOG.info("Failed to initialize backglass setting preferences: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize backglass setting preferences: " + e.getMessage());
    }
  }

  private void saveSettings() {
    try {
      if (saveEnabled) {
        Studio.client.getBackglassServiceClient().saveServerSettings(backglassServerSettings);
      }
    } catch (Exception e) {
      LOG.error("Failed to save backglass server settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save backglass server settings: " + e.getMessage());
    }
  }
}
