package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.ManiaRegistration;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaPreferencesController.class);

  @FXML
  private VBox preferencesPanel;

  @FXML
  private VBox registrationPanel;

  @FXML
  private CheckBox registrationCheckbox;

  @FXML
  private CheckBox submitAllCheckbox;

  @FXML
  private Label idLabel;

  private TournamentSettings settings;

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
  private void onAccountDelete() {
    boolean deregistered = ManiaRegistration.deregister();
    if(deregistered) {
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
      registrationPanel.setVisible(cabinet == null);
      preferencesPanel.setVisible(cabinet != null);

      settings.setEnabled(false);
      try {
        settings = client.getTournamentsService().saveSettings(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save tournament settings: " + e.getMessage());
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
    }

    settings = client.getTournamentsService().getSettings();
    preferencesPanel.setVisible(cabinet != null);
    registrationCheckbox.setSelected(false);

    registrationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        return;
      }
      boolean register = ManiaRegistration.register();
      if(register) {
        registrationPanel.setVisible(true);
        Cabinet cab = maniaClient.getCabinetClient().getCabinet();
        idLabel.setText(cab.getUuid());
        try {
          settings = client.getTournamentsService().saveSettings(settings);
        }
        catch (Exception e) {
          registrationCheckbox.setSelected(false);
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Registration failed! Please contact the administrator (see preference footer for details).");
        }
        registrationCheckbox.setSelected(false);
      }
    });

    submitAllCheckbox.setSelected(settings.isSubmitAllScores());
    submitAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setSubmitAllScores(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        }
      }
    });
    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.TOURNAMENTS_SETTINGS.equals(key)) {
      preferencesPanel.setVisible(settings.isEnabled());
    }
  }
}
