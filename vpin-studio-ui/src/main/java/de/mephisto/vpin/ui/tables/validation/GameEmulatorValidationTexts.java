package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode.*;

/**
 * See GameEmulatorValidator
 */
public class GameEmulatorValidationTexts {

  @Nullable
  public static LocalizedValidation validate(@NonNull GameEmulatorRepresentation emulator) {
    String label = null;
    String text = null;

    List<ValidationState> states = emulator.getValidationStates();
    if (states.isEmpty()) {
      return null;
    }
    return getValidationResult(emulator, states.get(0));
  }

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull GameEmulatorRepresentation emulator, ValidationState state) {
    Frontend frontend = Studio.client.getFrontendService().getFrontendCached();

    String text;
    String label;
    int code = state.getCode();
    switch (code) {
      case CODE_NO_INSTALLATION_DIRECTORY: {
        label = "The emulator has no or an invalid launch folder set.";
        text = "Fix the configuration by setting a valid launch folder.";
        break;
      }
      case CODE_NO_GAMES_FOLDER: {
        label = "The emulator has no or an invalid games folder set.";
        text = "Fix the configuration by setting a valid games folder.";
        break;
      }
      case CODE_NO_GAME_EXTENSION: {
        label = "The emulator has no game extensions suffix set.";
        text = "Fix the configuration by setting the extension type of the games.";
        break;
      }
      case CODE_INVALID_ROMS_FOLDER: {
        label = "The ROMs folder is invalid.";
        text = "Fix the configuration by setting a valid ROMs folder.";
        break;
      }
      case CODE_INVALID_MEDIA_FOLDER: {
        label = "The media folder is invalid.";
        text = "Fix the configuration by setting or creating a valid media folder.";
        break;
      }
      case CODE_NO_GAMES_FOUND: {
        label = "No games found.";
        text = "No matching games have been found for the selected games folder and file extension.";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }


    return new LocalizedValidation(label, text);
  }
}
