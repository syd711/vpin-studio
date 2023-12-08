package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class DismissalUtil {
  public static void dismissValidation(GameRepresentation game, ValidationState validationState) {
    List<String> csvValue = client.getPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS).getCSVValue();
    if (csvValue.contains(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_CONFIRM_DISMISSALS)) {
      dismiss(game, validationState);
    }
    else {
      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckbox(Studio.stage, "Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?", "Dismiss",
        "The warning can be re-enabled by validating the table again.", null, "Do not show again", false);
      if (!confirmationResult.isApplied()) {
        dismiss(game, validationState);
      }

      if (!confirmationResult.isApplied() && confirmationResult.isChecked()) {
        if (!csvValue.contains(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_CONFIRM_DISMISSALS)) {
          csvValue.add(PreferenceNames.UI_DO_NOT_SHOW_AGAIN_CONFIRM_DISMISSALS);
          client.getPreferenceService().setPreference(PreferenceNames.UI_DO_NOT_SHOW_AGAINS, String.join(",", csvValue));
        }
      }
    }
  }

  private static void dismiss(GameRepresentation game, ValidationState validationState) {
    List<Integer> ignoredValidations = game.getIgnoredValidations();
    if (ignoredValidations == null) {
      ignoredValidations = new ArrayList<>();
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
    List<Integer> ignoredValidations =  new ArrayList<>(game.getIgnoredValidations());
    for (Integer code : codes) {
      if(!ignoredValidations.contains(code)) {
        ignoredValidations.add(code);
      }
    }

    game.setIgnoredValidations(ignoredValidations);
    try {
      client.getGameService().saveGame(game);
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
    EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
  }

  private static void dismissAll(GameRepresentation game) {
    List<Integer> ignoredValidations =  new ArrayList<>();
    ignoredValidations.add(-1);
    game.setIgnoredValidations(ignoredValidations);

    try {
      client.getGameService().saveGame(game);
    } catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
    EventManager.getInstance().notifyTableChange(game.getId(), null);
  }
}
