package de.mephisto.vpin.ui.mania.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.events.EventManager;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.*;

public class TableRatingHelper {
  private final static Logger LOG = LoggerFactory.getLogger(TableRatingHelper.class);

  public static void ratingClicked(@NonNull GameRepresentation game, int rating) {
    try {
      writeLocalTableRating(game, rating);
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isHideRatingSyncInfo()) {
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

          boolean checked = confirmationResult.isChecked();
          boolean okClicked = confirmationResult.isOkClicked();

          JFXFuture.supplyAsync(() -> {
            if (checked) {
              uiSettings.setHideRatingSyncInfo(true);
              client.getPreferenceService().setJsonPreference(uiSettings);
            }

            if (okClicked) {
              maniaSettings.setSubmitRatings(true);
              maniaSettings.setSubmitPlayed(true);
              client.getPreferenceService().setJsonPreference(maniaSettings);
            }
            return true;
          }).thenAcceptLater((result) -> {
            ManiaHelper.runScoreSynchronization(false);
          });
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to apply game rating: {}", e.getMessage(), e);
      WidgetFactory.showAlert(stage, "Error", "Failed to apply game rating: " + e.getMessage());
    }
  }

  private static void writeLocalTableRating(@NonNull GameRepresentation game, int rating) {
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
