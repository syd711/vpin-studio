package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.ui.Studio.client;

public class DismissalUtil {
  public static void dismissValidation(GameRepresentation game, ValidationState validationState) {
    List<String> csvValue = client.getPreference(PreferenceNames.UI_SETTINGS).getCSVValue();
    if(csvValue.contains(PreferenceNames.UI_HIDE_CONFIRM_DISMISSALS)) {
      dismiss(game, validationState);
    }
    else {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Ignore this warning for future validations of table '" + game.getGameDisplayName() + "?",
        "The warning can be re-enabled by validating the table again.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        dismiss(game, validationState);
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
}
