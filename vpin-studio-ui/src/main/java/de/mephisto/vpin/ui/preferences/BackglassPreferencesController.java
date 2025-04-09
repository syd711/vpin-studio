package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.models.B2SFormPosition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
  private CheckBox hideGrillCheckbox;

  @FXML
  private CheckBox hideB2SDMDCheckbox;

  @FXML
  private CheckBox hideDMDCheckbox;

  @FXML
  private ComboBox<B2SFormPosition> formToPosition;

  @FXML
  private Label backglassServerFolder;

  @FXML
  private Label b2STableSettingsFile;

  @FXML
  private Label noMatchFound;

  private DirectB2ServerSettings backglassServerSettings;

  private boolean saveEnabled = false;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      backglassServerSettings = Studio.client.getBackglassServiceClient().getServerSettings();
      boolean serverInstalled = backglassServerSettings != null;

      backglassServerFolder.setVisible(serverInstalled);
      b2STableSettingsFile.setVisible(serverInstalled);
      noMatchFound.setVisible(!serverInstalled);
      pluginsCheckbox.setDisable(!serverInstalled);
      backglassMissingCheckbox.setDisable(!serverInstalled);
      fuzzyMatchingCheckbox.setDisable(!serverInstalled);
      startModeCheckbox.setDisable(!serverInstalled);
      hideGrillCheckbox.setDisable(!serverInstalled);
      hideB2SDMDCheckbox.setDisable(!serverInstalled);
      hideDMDCheckbox.setDisable(!serverInstalled);
      formToPosition.setDisable(!serverInstalled);

      if (serverInstalled) {

        backglassServerFolder.setText(backglassServerSettings.getBackglassServerFolder());
        b2STableSettingsFile.setText(backglassServerSettings.getB2STableSettingsFile());

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

        hideGrillCheckbox.setSelected(backglassServerSettings.isHideGrill());
        hideGrillCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setHideGrillBoolean(newValue);
          saveSettings();
        });
        hideB2SDMDCheckbox.setSelected(backglassServerSettings.isHideB2SDMD());
        hideB2SDMDCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setHideB2SDMD(newValue);
          saveSettings();
        });
        hideDMDCheckbox.setSelected(backglassServerSettings.isHideDMD());
        hideDMDCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setHideDMDBoolean(newValue);
          saveSettings();
        });

        formToPosition.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.FORM_POSITIONS));
        formToPosition.setValue(TablesSidebarDirectB2SController.FORM_POSITIONS.stream().filter(v -> v.getId() == backglassServerSettings.getFormToPosition()).findFirst().orElse(null));
        formToPosition.valueProperty().addListener((observable, oldValue, newValue) -> {
          backglassServerSettings.setFormToPosition(newValue.getId());
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
