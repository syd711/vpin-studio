package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaPreferencesController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaPreferencesController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox preferencesPanel;

  @FXML
  private VBox registrationPanel;

  @FXML
  private CheckBox registrationCheckbox;

  @FXML
  private CheckBox submitAllCheckbox;

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
  private Label idLabel;

  private TournamentSettings settings;

  @FXML
  private void onIdCopy() {
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    if(cabinet != null) {
      Clipboard clipboard = Clipboard.getSystemClipboard();
      ClipboardContent content = new ClipboardContent();
      content.putString(cabinet.getUuid());
      clipboard.setContent(content);
    }
  }

  @FXML
  private void onAccountDelete() {
    ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "Delete VPin Mania Account", "Cancel", "Delete", "Delete your VPin Mania account?", "This will delete all active tournaments and recorded highscores.", "I understand, delete my account.");
    if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
      maniaClient.getCabinetClient().deleteCabinet();

      List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
      for (PlayerRepresentation player : players) {
        if (player.getTournamentUserUuid() != null) {
          player.setTournamentUserUuid(null);
          try {
            client.getPlayerService().savePlayer(player);
            LOG.info("Resetted VPin Mania account for " + player);
          } catch (Exception e) {
            LOG.error("Failed to de-register player account: " + e.getMessage(), e);
          }

        }
      }

      Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
      registrationPanel.setVisible(cabinet == null);
      preferencesPanel.setVisible(cabinet != null);

      settings.setEnabled(false);
      try {
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save tournament settings: " + e.getMessage());
      }
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    preferencesPanel.managedProperty().bindBidirectional(preferencesPanel.visibleProperty());
    registrationPanel.managedProperty().bindBidirectional(registrationPanel.visibleProperty());

    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
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

      ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "VPin Mania Registration", "Cancel", "Register", "This registers your cabinet for the online service \"VPin Mania\".", "The account is bound to your cabinet.", "I understand, register my cabinet.");
      if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
        try {
          PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
          PreferenceEntryRepresentation systemName = client.getPreference(PreferenceNames.SYSTEM_NAME);
          Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
          if (!StringUtils.isEmpty(avatarEntry.getValue())) {
            image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
          }

          BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

          Cabinet newCab = new Cabinet();
          newCab.setCreationDate(new Date());
          newCab.setSettings(new CabinetSettings());
          newCab.setDisplayName(systemName.getValue() != null ? systemName.getValue() : "My VPin");
          Cabinet registeredCabinet = maniaClient.getCabinetClient().create(newCab, bufferedImage, null);
          registrationPanel.setVisible(registeredCabinet == null);

          if (registeredCabinet != null) {
            idLabel.setText(registeredCabinet.getUuid());
          }

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
    websiteLink.setText(settings.getDefaultWebsite());
    descriptionText.setText(settings.getDefaultDescription());

    dashboardUrl.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("dashboardUrl", () -> {
      try {
        settings.setDefaultDashboardUrl(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    submitAllCheckbox.setSelected(settings.isSubmitAllScores());
    submitAllCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setSubmitAllScores(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
        } catch (Exception e) {
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        }
      }
    });

    tournamentsCheckbox.setSelected(settings.isTournamentsEnabled());
    tournamentsCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          settings.setTournamentsEnabled(newValue);
          settings = client.getTournamentsService().saveSettings(settings);
        } catch (Exception e) {
          LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        }
      }
    });

    discordLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("discordLink", () -> {
      try {
        settings.setDefaultDiscordLink(t1);
        settings = client.getTournamentsService().saveSettings(settings);
      } catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
      }
    }, 300));

    websiteLink.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("websiteLink", () -> {
      try {
        settings.setDefaultWebsite(t1);
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
