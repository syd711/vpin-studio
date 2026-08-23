package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
    return getValidationResult(emulator, states.getFirst());
  }

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull GameEmulatorRepresentation emulator, ValidationState state) {
    Frontend frontend = Studio.client.getFrontendService().getFrontendCached();

    String text;
    String label;
    int code = state.getCode();
    switch (code) {
      case CODE_NO_INSTALLATION_DIRECTORY: {
        label = Messages.get("validation.emulator.no_installation_directory.label");
        text = Messages.get("validation.emulator.no_installation_directory.text");
        break;
      }
      case CODE_NO_GAMES_FOLDER: {
        label = Messages.get("validation.emulator.no_games_folder.label");
        text = Messages.get("validation.emulator.no_games_folder.text");
        break;
      }
      case CODE_NO_GAME_EXTENSION: {
        label = Messages.get("validation.emulator.no_game_extension.label");
        text = Messages.get("validation.emulator.no_game_extension.text");
        break;
      }
      case CODE_INVALID_ROMS_FOLDER: {
        label = Messages.get("validation.emulator.invalid_roms_folder.label");
        text = Messages.get("validation.emulator.invalid_roms_folder.text");
        break;
      }
      case CODE_INVALID_MEDIA_FOLDER: {
        label = Messages.get("validation.emulator.invalid_media_folder.label");
        text = Messages.get("validation.emulator.invalid_media_folder.text");
        break;
      }
      case CODE_NO_GAMES_FOUND: {
        label = Messages.get("validation.emulator.no_games_found.label");
        text = Messages.get("validation.emulator.no_games_found.text");
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }


    return new LocalizedValidation(label, text);
  }
}
