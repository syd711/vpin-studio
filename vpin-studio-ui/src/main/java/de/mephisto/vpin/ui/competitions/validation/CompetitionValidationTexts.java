package de.mephisto.vpin.ui.competitions.validation;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import org.jspecify.annotations.NonNull;

import static de.mephisto.vpin.restclient.competitions.CompetitionValidationCode.*;

public class CompetitionValidationTexts {

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull CompetitionRepresentation competition) {
    String text;
    String label;
    int code = competition.getValidationState().getCode();
    String typeName = Messages.get("validation.competition.type.competition");
    if(competition.getType().equals(CompetitionType.ISCORED.name())) {
      typeName = Messages.get("validation.competition.type.iscored_subscription");
    }

    switch (code) {
      case GAME_NOT_FOUND: {
        label = Messages.get("validation.competition.game_not_found.label");
        text = Messages.get("validation.competition.game_not_found.text", typeName);
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped competition validation state");
      }

    }

    return new LocalizedValidation(label, text);
  }
}
