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
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;

  private DirectB2ServerSettings serverSettings;

  private GameEmulatorRepresentation emulatorRepresentation;

  private boolean saveEnabled = false;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<GameEmulatorRepresentation> gameEmulators = Studio.client.getPinUPPopperService().getBackglassGameEmulators();
    emulatorRepresentation = gameEmulators.get(0);
    ObservableList<GameEmulatorRepresentation> emulators = FXCollections.observableList(gameEmulators);
    emulatorCombo.setItems(emulators);
    emulatorCombo.setValue(emulatorRepresentation);
    emulatorCombo.valueProperty().addListener((observableValue, gameEmulatorRepresentation, t1) -> {
      emulatorRepresentation = t1;
      refresh();
    });

    serverSettings = Studio.client.getBackglassServiceClient().getServerSettings(emulatorRepresentation.getId());

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

    fuzzyMatchingCheckbox.setSelected(serverSettings.isDisableFuzzyMatching());
    fuzzyMatchingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      serverSettings.setDisableFuzzyMatching(newValue);
      saveSettings();
    });

    saveEnabled = true;
  }

  private void refresh() {
    saveEnabled = false;
    serverSettings = Studio.client.getBackglassServiceClient().getServerSettings(emulatorRepresentation.getId());
    backglassMissingCheckbox.setSelected(serverSettings.isShowStartupError());
    pluginsCheckbox.setSelected(serverSettings.isPluginsOn());
    fuzzyMatchingCheckbox.setSelected(serverSettings.isDisableFuzzyMatching());
    saveEnabled = true;
  }

  private void saveSettings() {
    try {
      if(saveEnabled) {
        Studio.client.getBackglassServiceClient().saveServerSettings(emulatorRepresentation.getId(), serverSettings);
      }
    } catch (Exception e) {
      LOG.error("Failed to save backglass server settings: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save backglass server settings: " + e.getMessage());
    }
  }
}
