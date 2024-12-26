package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.CabinetSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaRegistration {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaRegistration.class);

  public static boolean register() {
    ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "VPin Mania Registration", "Cancel", "Register", "This registers your cabinet for the online service \"VPin Mania\".", "The account is bound to your cabinet.", "I understand, register my cabinet.");
    if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
      try {
        TournamentSettings settings = client.getTournamentsService().getSettings();
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

        if (registeredCabinet != null) {
          settings.setEnabled(true);
          client.getTournamentsService().saveSettings(settings);
          return true;
        }
      }
      catch (Exception e) {
        LOG.error("Failed to save tournament settings: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Registration failed! Please contact the administrator (see preference footer for details).");
      }
    }
    return false;
  }

  public static boolean deregister() {
    ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, "Delete VPin Mania Account", "Cancel", "Delete", "Delete your VPin Mania account?",
        "This will delete all active tournaments and recorded highscores.", "I understand, delete my account.");
    if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
      maniaClient.getCabinetClient().deleteCabinet();

      List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
      for (PlayerRepresentation player : players) {
        if (player.getTournamentUserUuid() != null) {
          player.setTournamentUserUuid(null);
          try {
            client.getPlayerService().savePlayer(player);
            LOG.info("Resetted VPin Mania account for " + player);
          }
          catch (Exception e) {
            LOG.error("Failed to de-register player account: " + e.getMessage(), e);
          }

        }
      }
      ManiaAvatarCache.clear();
      ManiaPermissions.invalidate();
      return true;
    }
    return false;
  }
}
