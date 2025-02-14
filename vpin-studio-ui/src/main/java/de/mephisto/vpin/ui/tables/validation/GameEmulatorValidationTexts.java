package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameEmulatorValidationCode.CODE_NO_INSTALLATION_DIRECTORY;
import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;

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
        label = "The emulator has no or an invalid installation directory set.";
        text = "Fix the configuration by setting a valid installation directory.";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }


    return new LocalizedValidation(label, text);
  }
}
