package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.*;

public class TableRatingHelper {
  private final static Logger LOG = LoggerFactory.getLogger(TableRatingHelper.class);

  public static void ratingClicked(@NonNull GameRepresentation game, int rating) {
    try {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideRatingSyncInfo()) {
        writeLocalTableRating(game, rating);
        return;
      }

      Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
      if (cabinet != null) {
        ManiaSettings maniaSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
        if (!maniaSettings.isSubmitRatings()) {
          ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(stage,
              "Enable VPin Mania statistics synchronization?", "Enable Statistics Synchronization",
              "This will push your table statistics anonymously to VPin Mania.",
              "You can change this setting in the preferences for your VPin Mania account.", "Do not show again", false);

          if (confirmationResult.isChecked()) {
            uiSettings.setHideRatingSyncInfo(true);
            client.getPreferenceService().setJsonPreference(uiSettings);
          }

          if (confirmationResult.isOkClicked()) {
            maniaSettings.setSubmitRatings(true);
            maniaSettings.setSubmitPlayed(true);
            client.getPreferenceService().setJsonPreference(maniaSettings);
            ManiaHelper.runSynchronization(false);
          }
        }
      }

      writeLocalTableRating(game, rating);
    }
    catch (Exception e) {
      LOG.error("Failed to apply game rating: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to apply game rating: " + e.getMessage());
    }
  }

  private static void writeLocalTableRating(@NotNull GameRepresentation game, int rating) {
    try {
      TableDetails tableDetails = client.getFrontendService().getTableDetails(game.getId());
      if (tableDetails != null) {
        tableDetails.setGameRating((rating + 1));
        client.getFrontendService().saveTableDetails(tableDetails, game.getId());
        EventManager.getInstance().notifyTableChange(game.getId(), null, null);
      }
    }
    catch (Exception e) {
      LOG.error("Rating update failed: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Rating update failed: " + e.getMessage());
    }
  }
}
