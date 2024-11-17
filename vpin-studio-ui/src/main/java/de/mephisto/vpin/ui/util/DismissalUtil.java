package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class DismissalUtil {
  public static void dismissValidation(GameRepresentation game, ValidationState validationState) {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    if (uiSettings.isHideDismissConfirmations()) {
      dismiss(game, validationState);
    }
    else {
      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(Studio.stage, "Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?", "Dismiss",
        "The warning can be re-enabled by validating the table again.", null, "Do not show again", false);
      if (!confirmationResult.isApplyClicked()) {
        dismiss(game, validationState);
      }

      if (!confirmationResult.isApplyClicked() && confirmationResult.isChecked()) {
        uiSettings.setHideDismissConfirmations(true);
        client.getPreferenceService().setJsonPreference(uiSettings);
      }
    }
  }

  private static void dismiss(GameRepresentation game, ValidationState validationState) {
    List<Integer> ignoredValidations = game.getIgnoredValidations();
    if (ignoredValidations == null) {
      ignoredValidations = new ArrayList<>();
    }
    else {
      ignoredValidations = new ArrayList<>(ignoredValidations);
    }

    if (!ignoredValidations.contains(validationState.getCode())) {
      ignoredValidations.add(validationState.getCode());
    }

    game.setIgnoredValidations(ignoredValidations);

    try {
      client.getGameService().saveGame(game);
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
    EventManager.getInstance().notifyTableChange(game.getId(), null);
  }

  public static void dismissSelection(GameRepresentation game, List<Integer> codes) {
    List<Integer> ignoredValidations = new ArrayList<>(game.getIgnoredValidations());
    for (Integer code : codes) {
      if (!ignoredValidations.contains(code)) {
        ignoredValidations.add(code);
      }
    }

    game.setIgnoredValidations(ignoredValidations);
    try {
      client.getGameService().saveGame(game);
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
  }
}
