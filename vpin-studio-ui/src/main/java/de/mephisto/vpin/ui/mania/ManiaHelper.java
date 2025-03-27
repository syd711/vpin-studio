package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.system.SystemId;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class ManiaHelper {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaHelper.class);

  public static boolean register() {
    SystemId systemId = client.getSystemService().getSystemId();
    if (StringUtils.isEmpty(systemId.getSystemId())) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to retrieve unique system id. Please report this problem.");
      return false;
    }

    ManiaRegistration registration = ManiaDialogs.openRegistrationDialog();
    if (registration != null) {
      try {
        ManiaRegistration register = client.getManiaService().register(registration);
        if (!StringUtils.isEmpty(register.getResult())) {
          WidgetFactory.showAlert(Studio.stage, "Registration Failed", "The registration failed: " + register.getResult());
          LOG.error("VPin Mania registration failed: {}", register.getResult());
          return false;
        }

        Cabinet registeredCabinet = maniaClient.getCabinetClient().getCabinet();
        if (registeredCabinet != null) {
          ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

          if (!registration.getPlayerIds().isEmpty() || settings.isSubmitPlayed() || settings.isSubmitRatings()) {
            runSynchronization();
          }
          return true;
        }
      }
      catch (Exception e) {
        LOG.error("Failed to finish registration: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Registration failed! Please contact the administrator (see preference footer for contact details).");
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

  public static void runSynchronization() {
    List<VpsTable> vpsTables = Studio.client.getGameService().getInstalledVpsTables();
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new VPinManiaSynchronizeProgressModel(vpsTables));
    List<Object> results = progressDialog.getResults();
    int count = 0;
    String msg = null;
    for (Object result : results) {
      ManiaTableSyncResult syncResult = (ManiaTableSyncResult) result;
      count += syncResult.getTableScores().size();
      msg = syncResult.getResult();
    }
    WidgetFactory.showInformation(Studio.stage, "Synchronization Result", count + " highscore(s) have been submitted to vpin-mania.net.", msg);
  }
}
