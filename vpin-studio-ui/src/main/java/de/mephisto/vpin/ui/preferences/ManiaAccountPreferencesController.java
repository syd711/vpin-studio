package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ManiaAccountPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaAccountPreferencesController.class);

  public static final int DEBOUNCE_MS = 500;

  private final Debouncer debouncer = new Debouncer();

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

  @FXML
  private TextArea descriptionText;


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

    preference = client.getPreference(PreferenceNames.TOURNAMENTS_DESCRIPTION);
    descriptionText.setText(preference.getValue());

    dashboardUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("dashboardUrl", () -> {
      client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_DASHBOARD_URL, t1);
    }, 300));

    discordLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("discordLink", () -> {
      client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_DISCORD_LINK, t1);
    }, 300));

    descriptionText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("descriptionText", () -> {
      String value = String.valueOf(t1);
      if (!StringUtils.isEmpty(String.valueOf(value)) && value.length() > 4096) {
        value = value.substring(0, 4000);
      }
      client.getPreferenceService().setPreference(PreferenceNames.TOURNAMENTS_DESCRIPTION, value);
    }, 300));

    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.TOURNAMENTS_ENABLED.equals(key)) {
      preferencesPanel.setVisible((Boolean) value);
    }
  }
}
