package de.mephisto.vpin.ui.mania.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.User;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.VPinManiaScoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.VPinManiaTablesSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.dialogs.ManiaDialogs;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class ManiaHelper {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaHelper.class);

  public static String getCabinetAvatarUrl(@NonNull Cabinet cabinet) {
    return "https://vpin-mania.net/api/cabinet/avatar/" + cabinet.getUuid();
  }

  public static boolean isRegistered() {
    try {
      client.getPreferenceService().clearCache(PreferenceNames.MANIA_SETTINGS);
      ManiaSettings maniaSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (!StringUtils.isEmpty(maniaSettings.getCabinetUuid()) && !StringUtils.isEmpty(maniaSettings.getApiKey())) {
        maniaClient.getRestClient().setApiKey(maniaSettings.getApiKey());
        Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached(maniaSettings.getCabinetUuid());
        return cabinet != null;
      }
    }
    catch (Exception e) {
      LOG.error("VPin Mania registration check failed: {}", e.getMessage());
    }
    return false;
  }

  public static boolean register() {
    String apiKey = getApiKey();

    if (apiKey == null) {
      return false;
    }

    ManiaRegistration registration = ManiaDialogs.openRegistrationDialog(apiKey);
    if (registration != null) {
      try {
        //this is the server side registration where the API key is set server side, not here yet....
        ManiaRegistration completedRegistration = client.getManiaService().register(registration);
        if (!StringUtils.isEmpty(completedRegistration.getResult())) {
          String result = completedRegistration.getResult();
          if (result != null && result.contains("Token not found")) {
            WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.registration_failed"), Messages.get("dialog.the_api_key_is_invalid"));
          }
          else {
            WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.registration_failed"), completedRegistration.getResult());
          }

          LOG.error("VPin Mania registration failed: {}", completedRegistration.getResult());
          return false;
        }

        client.getPreferenceService().clearCache(PreferenceNames.MANIA_SETTINGS);
        ManiaSettings maniaSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

        maniaClient.getRestClient().setApiKey(maniaSettings.getApiKey());
        Cabinet registeredCabinet = maniaClient.getCabinetClient().getDefaultCabinetCached(maniaSettings.getCabinetUuid());
        if (registeredCabinet != null) {
          if (!registration.getPlayerIds().isEmpty()) {
            runScoreSynchronization(false);
          }
          if (registration.isSubmitTables()) {
            runTablesSynchronization();
          }

          client.getPreferenceService().notifyPreferenceChange(PreferenceNames.MANIA_SETTINGS, null);
          return true;
        }
        else {
          WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.the_registration_failed"), Messages.get("dialog.the_cabinet_may_have_been_registered_but"));
        }
      }
      catch (Exception e) {
        LOG.error("Failed to finish registration: " + e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.registration_failed_2"), Messages.get("dialog.please_contact_the_administrator_see_preference_footer"));
      }
    }
    return false;
  }

  private static String getApiKey() {
    User currentUser = null;
    try {
      currentUser = Studio.maniaClient.getUserClient().getCurrentUser();
      return currentUser.getApiKey();
    }
    catch (Exception e) {
      LOG.info("Client authentication check for mania failed, requesting API key.");
    }

    String s = WidgetFactory.showInputDialog(Studio.stage, Messages.get("dialog.api_key"), Messages.get("dialog.enter_you_vpin_mania_api_key_here"), Messages.get("dialog.the_api_key_is_required_for_the"), null, null);
    if (s != null) {
      maniaClient.getRestClient().setApiKey(s);
      return getApiKey();
    }
    return null;
  }

  public static boolean deregister() {
    ConfirmationResult confirmationResult = WidgetFactory.showAlertOptionWithMandatoryCheckbox(Studio.stage, Messages.get("dialog.delete_cabinet_data"), Messages.get("common.cancel"), Messages.get("common.delete"), Messages.get("dialog.delete_this_cabinet_from_your_vpin_mania"),
        Messages.get("dialog.this_will_delete_the_cabinet_and_all"), Messages.get("dialog.i_understand_delete_my_account"));
    if (confirmationResult.isChecked() && !confirmationResult.isApplyClicked()) {
      Boolean deleted = client.getManiaService().deleteCabinet();
      if (!deleted) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.failed_to_delete_the_cabinet_please_write"));
        return false;
      }

      List<PlayerRepresentation> players = client.getPlayerService().getPlayers();
      for (PlayerRepresentation player : players) {
        if (player.getManiaAccountUuid() != null) {
          player.setManiaAccountUuid(null);
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
      client.getManiaService().clearCache();

      client.getPreferenceService().clearCache(PreferenceNames.MANIA_SETTINGS);
      client.getPreferenceService().notifyPreferenceChange(PreferenceNames.MANIA_SETTINGS, null);

      maniaClient.getRestClient().setApiKey(null);
      return true;
    }
    return false;
  }

  public static void runScoreSynchronization(boolean showScoreSummary) {
    Cabinet defaultCabinetCached = maniaClient.getCabinetClient().getDefaultCabinetCached();
    if (defaultCabinetCached != null) {
      List<Account> accounts = maniaClient.getAccountClient().getAccounts(defaultCabinetCached.getId());
      if (accounts.isEmpty()) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.highscore_synchronization"), Messages.get("dialog.the_synchronization_has_been_cancelled_no_registered"), Messages.get("dialog.register_a_local_player_to_synchronize_their"));
        return;
      }

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new VPinManiaScoreSynchronizeProgressModel());
      if (showScoreSummary) {
        List<ManiaTableSyncResult> results = (List<ManiaTableSyncResult>) (List<?>) progressDialog.getResults();
        ManiaDialogs.openTableSyncResult(results);
      }
    }
    else {
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_cabinet_found"), Messages.get("dialog.no_default_cabinet_selected_check_your_registration"));
    }
  }

  public static void runTablesSynchronization() {
    ProgressDialog.createProgressDialog(new VPinManiaTablesSynchronizeProgressModel());
  }

}
