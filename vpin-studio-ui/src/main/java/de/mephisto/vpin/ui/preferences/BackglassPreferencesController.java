package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.DirectB2ServerSettings;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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

  private DirectB2ServerSettings serverSettings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    serverSettings = Studio.client.getBackglassServiceClient().getServerSettings();

    pluginsCheckbox.setSelected(serverSettings.isPluginsOn());
    pluginsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setPluginsOn(newValue);
      saveSettings();
    });

    backglassMissingCheckbox.setSelected(serverSettings.isShowStartupError());
    backglassMissingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setShowStartupError(newValue);
      saveSettings();
    });
  }

  private void saveSettings() {
    try {
      Studio.client.getBackglassServiceClient().saveServerSettings(serverSettings);
    } catch (Exception e) {
      LOG.error("Failed to save backglass server settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save backglass server settings: " + e.getMessage());
    }
  }
}
