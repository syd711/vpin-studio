package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

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

  @FXML
  private void onAccountDelete() {

  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());
    registrationPanel.managedProperty().bindBidirectional(registrationPanel.visibleProperty());

    SystemSummary systemSummary = client.getSystemService().getSystemSummary();

    System.out.println(systemSummary.getSystemId());
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet(systemSummary.getSystemId());
    registrationPanel.setVisible(cabinet == null);

    settings = client.getTournamentsService().getSettings();
    preferencesPanel.setVisible(cabinet != null);
    registrationCheckbox.setSelected(false);

    registrationCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        return;
      }

      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "VPin Mania Registration", "Cancel", "Register", "This registers your cabinet for the online service \"VPin Mania\".", "The account is bound to your cabinet.", "I understand, register my cabinet.");
      if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
        try {
          PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
          Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
          if (!StringUtils.isEmpty(avatarEntry.getValue())) {
            image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
          }

          BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

          Cabinet newCab = new Cabinet();
          newCab.setCreationDate(new Date());
          newCab.setSettings(new CabinetSettings());
          Cabinet registeredCabinet = maniaClient.getCabinetClient().create(newCab, systemSummary.getSystemId(), bufferedImage, null);

          settings.setEnabled(true);
          settings = client.getTournamentsService().saveSettings(settings);
        } catch (Exception e) {
          registrationCheckbox.setSelected(false);
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Registration failed! Please contact the administrator (see preference footer for details).");
        }
      }
      registrationCheckbox.setSelected(false);
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
