package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentPreferencesController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox preferencesPanel;

  @FXML
  private CheckBox tournamentsCheckbox;

  @FXML
  private TextField dashboardUrl;

  @FXML
  private TextField discordLink;

  @FXML
  private TextField websiteLink;

  @FXML
  private TextArea descriptionText;

  @FXML
  private Pane notRegisteredPane;

  private TournamentSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());

    Cabinet cabinet = null;
    try {
      cabinet = maniaClient.getCabinetClient().getCabinet();
    }
    catch (Exception e) {
      LOG.error("Failed to load cabinet setting: {}", e.getMessage());
    }
    preferencesPanel.setVisible(cabinet != null);
    notRegisteredPane.setVisible(cabinet == null);
    if (cabinet == null) {
      return;
    }

    settings = client.getTournamentsService().getSettings();


    dashboardUrl.setText(settings.getDefaultDashboardUrl());
    discordLink.setText(settings.getDefaultDiscordLink());
    websiteLink.setText(settings.getDefaultWebsite());
    descriptionText.setText(settings.getDefaultDescription());

    dashboardUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("dashboardUrl", () -> {
      try {
        settings.setDefaultDashboardUrl(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    tournamentsCheckbox.setSelected(settings.isTournamentsEnabled());
    tournamentsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setTournamentsEnabled(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
        }
        catch (Exception e) {
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        }
      }
    });

    discordLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("discordLink", () -> {
      try {
        settings.setDefaultDiscordLink(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      }
      catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    websiteLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("websiteLink", () -> {
      try {
        settings.setDefaultWebsite(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      }
      catch (Exception e) {
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
      }
      catch (Exception e) {
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
