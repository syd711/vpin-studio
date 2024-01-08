package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.PreferenceChangeListener;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
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

public class TournamentsPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsPreferencesController.class);

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
  private TournamentSettings settings;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());

    settings = client.getTournamentsService().getSettings();
    preferencesPanel.setVisible(settings.isEnabled());
    registrationCheckbox.setSelected(settings.isEnabled());

    registrationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setEnabled(newValue);
      try {
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    });
    dashboardUrl.setText(settings.getDefaultDashboardUrl());
    discordLink.setText(settings.getDefaultDiscordLink());
    descriptionText.setText(settings.getDefaultDescription());

    dashboardUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("dashboardUrl", () -> {
      try {
        settings.setDefaultDashboardUrl(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    discordLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("discordLink", () -> {
      try {
        settings.setDefaultDiscordLink(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    descriptionText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("descriptionText", () -> {
      String value = String.valueOf(t1);
      if (!StringUtils.isEmpty(String.valueOf(value)) && value.length() > 4096) {
        value = value.substring(0, 4000);
      }
      try {
        settings.setDefaultDescription(value);
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    client.getPreferenceService().addListener(this);
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.TOURNAMENTS_SETTINGS.equals(key)) {
      preferencesPanel.setVisible(settings.isEnabled());
    }
  }
}
