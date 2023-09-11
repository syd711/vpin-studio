package de.mephisto.vpin.ui.competitions.validation;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;

import static de.mephisto.vpin.restclient.CompetitionValidationCode.*;

public class CompetitionValidationTexts {

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull CompetitionRepresentation competition) {
    String text;
    String label;
    int code = competition.getValidationState().getCode();
    String typeName = "competition";
    if(competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      typeName = "subscription";
    }

    switch (code) {
      case DISCORD_SERVER_NOT_FOUND: {
        label = "Invalid Discord server.";
        text = "The Discord server configured for this " + typeName + " was not found.";
        break;
      }
      case DISCORD_CHANNEL_NOT_FOUND: {
        label = "Invalid Discord channel.";
        text = "The Discord channel configured for this " + typeName + " was not found.";
        break;
      }
      case GAME_NOT_FOUND: {
        label = "No matching table found.";
        text = "The configured table for this " + typeName + " does not exist anymore.";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped competition validation state");
      }

    }

    return new LocalizedValidation(label, text);
  }
}
