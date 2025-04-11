package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaAccountSettingsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountSettingsController.class);

  @FXML
  private VBox preferencesPanel;

  @FXML
  private VBox registrationPanel;


  @FXML
  private Label idLabel;

  @FXML
  private Label systemIdLabel;


  @FXML
  private void onIdCopy() {
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    if (cabinet != null) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(cabinet.getUuid());
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onRegister() {
    boolean registered = ManiaHelper.register();
    if (registered) {
      registrationPanel.setVisible(false);
      preferencesPanel.setVisible(true);
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
      idLabel.setText(cabinet.getUuid());
    }
  }

  @FXML
  private void onSystemIdCopy() {
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
    if (cabinet != null) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(cabinet.getSystemId());
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onAccountDelete() {
    boolean deregistered = ManiaHelper.deregister();
    if (deregistered) {
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
      registrationPanel.setVisible(cabinet == null);
      preferencesPanel.setVisible(cabinet != null);


      ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      settings.setEnabled(false);
      try {
        client.getPreferenceService().setJsonPreference(settings);
        client.getManiaService().clearCache();
      }
      catch (Exception e) {
        LOG.error("Failed to save mania settings: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save VPin Mania settings: " + e.getMessage());
      }
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());
    registrationPanel.managedProperty().bindBidirectional(registrationPanel.visibleProperty());

    Cabinet cabinet = null;
    try {
      cabinet = maniaClient.getCabinetClient().getCabinet();
    }
    catch (Exception e) {
      LOG.error("Failed to load cabinet setting: {}", e.getMessage());
    }
    registrationPanel.setVisible(cabinet == null);

    if (cabinet != null) {
      idLabel.setText(cabinet.getUuid());
      systemIdLabel.setText(cabinet.getSystemId());
    }

    preferencesPanel.setVisible(cabinet != null);
  }
}
