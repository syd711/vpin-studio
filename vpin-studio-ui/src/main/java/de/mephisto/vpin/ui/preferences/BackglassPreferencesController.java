package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
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
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private DirectB2ServerSettings backglassServerSettings;

  private GameEmulatorRepresentation emulatorRepresentation;

  private boolean saveEnabled = false;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getBackglassGameEmulators();
      emulatorRepresentation = gameEmulators.get(0);
      ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
      emulatorCombo.setItems(emulators);
      emulatorCombo.setValue(emulatorRepresentation);
      emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
        emulatorRepresentation = t1;
        refresh();
      });

      backglassServerSettings = Studio.client.getBackglassServiceClient().getServerSettings(emulatorRepresentation.getId());

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
    } catch (Exception e) {
      LOG.info("Failed to initialize backglass setting preferences: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to initialize backglass setting preferences: " + e.getMessage());
    }
  }

  private void refresh() {
    saveEnabled = false;
    backglassServerSettings = Studio.client.getBackglassServiceClient().getServerSettings(emulatorRepresentation.getId());
    backglassMissingCheckbox.setSelected(backglassServerSettings.isShowStartupError());
    pluginsCheckbox.setSelected(backglassServerSettings.isPluginsOn());
    fuzzyMatchingCheckbox.setSelected(backglassServerSettings.isDisableFuzzyMatching());
    saveEnabled = true;
  }

  private void saveSettings() {
    try {
      if (saveEnabled) {
        Studio.client.getBackglassServiceClient().saveServerSettings(emulatorRepresentation.getId(), backglassServerSettings);
      }
    } catch (Exception e) {
      LOG.error("Failed to save backglass server settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save backglass server settings: " + e.getMessage());
    }
  }
}
