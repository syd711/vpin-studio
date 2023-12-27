package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ManiaAccountPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountPreferencesController.class);

  @FXML
  private VBox preferencesPanel;

  @FXML
  private VBox registrationPanel;

  @FXML
  private CheckBox registrationCheckbox;

  @FXML
  private TextField dashboardUrl;

  @FXML
  private TextField discordLink;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.TOURNAMENTS_ENABLED);
    preferencesPanel.setVisible(preference.getBooleanValue());
    registrationCheckbox.setSelected(preference.getBooleanValue());

    registrationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_ENABLED, newValue));

    preference = client.getPreference(PreferenceNames.TOURNAMENTS_DASHBOARD_URL);
    dashboardUrl.setText(preference.getValue());

    preference = client.getPreference(PreferenceNames.TOURNAMENTS_DISCORD_LINK);
    discordLink.setText(preference.getValue());

    dashboardUrl.textProperty().addListener((observable, oldValue, newValue) -> client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_DASHBOARD_URL, newValue));
    discordLink.textProperty().addListener((observable, oldValue, newValue) -> client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_DISCORD_LINK, newValue));

    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.TOURNAMENTS_ENABLED.equals(key)) {
      preferencesPanel.setVisible((Boolean) value);
    }
  }
}
