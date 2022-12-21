package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.restclient.ValidationCode;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;

import static de.mephisto.vpin.restclient.ValidationCode.*;

/**
 * See GameValidator
 */
public class ValidationTexts {

  private final static String NO_MEDIA_TEXT = "Dismiss this message if the table does not support this media or disable the screen in the preferences.";

  public static ValidationResult validate(@NonNull GameRepresentation game) {
    String label = null;
    String text = null;

    int code = game.getValidationState();
    switch (code) {
      case CODE_VPX_NOT_EXISTS: {
        label = "VPX file '" + game.getGameFileName() + "' does not exist.";
        text = "Fix the configuration for this table in PinUP Popper.";
        break;
      }
      case CODE_NO_ROM: {
        label = "No ROM name could be resolved.";
        text = "Consider setting the ROM name in the \"Table Metadata\" section or PinUP popper. Otherwise no highscore can be evaluated for this table.";
        break;
      }
      case CODE_ROM_NOT_EXISTS: {
        label = "ROM file does not exist.";
        text = "Dismiss this message if this table does not require a ROM. Otherwise upload the required ROM under \"Table Metadata\".";
        break;
      }
      case CODE_NO_DIRECTB2S_OR_PUPPACK: {
        label = "No PUP pack and no directb2s file found.";
        text = "No additional media has been found for this table. Download a 'directb2s' file for this table.";
        break;
      }
      case CODE_NO_AUDIO: {
        label = "No audio media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_AUDIO_LAUNCH: {
        label = "No audio launch media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_APRON: {
        label = "No apron media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_INFO: {
        label = "No info card set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_HELP: {
        label = "No help card set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_TOPPER: {
        label = "No topper media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_BACKGLASS: {
        label = "No backglass media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_DMD: {
        label = "No DMD media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_LOADING: {
        label = "No loading video set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_PLAYFIELD: {
        label = "No playfield video set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_OTHER2: {
        label = "No media for 'Other2' set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_WHEEL_IMAGE: {
        label = "No wheel icon set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      default: {
        return null;
      }
    }


    return new ValidationResult(label, text);
  }
}
