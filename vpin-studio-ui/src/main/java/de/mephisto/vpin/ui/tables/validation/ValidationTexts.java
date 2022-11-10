package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.restclient.representations.GameRepresentation;

/**
 * See GameValidator
 */
public class ValidationTexts {

  private final static String NO_MEDIA_TEXT = "Dismiss this message if the table does not support this media or disable the screen in the preferences.";

  public static TableValidation validate(GameRepresentation game) {
    String label = null;
    String text = null;

    int code = game.getValidationState();
    switch (code) {
      case 1: {
        label = "No ROM name could be resolved.";
        text = "Consider setting the ROM name in the \"Table Metadata\" section or PinUP popper. Otherwise no highscore can be evaluated for this table.";
        break;
      }
      case 2: {
        label = "Another table with the same ROM exists.";
        text = "If you are playing different versions of the same table, consider setting a NVOffset for each table.";
        break;
      }
      case 3: {
        label = "ROM file does not exist.";
        text = "Dismiss this message if this table does not require a ROM. Otherwise search for a download of this ROM file.";
        break;
      }
      case 20: {
        label = "No PUP pack and no directb2s file found.";
        text = "No additional media has been found for this table. Consider at least downloading a 'directb2s' file for this table.";
        break;
      }
      case 60: {
        label = "No highscore files found.";
        text = "Maybe this table has not been played yet.";
        break;
      }
      case 30: {
        label = "No audio media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 31: {
        label = "No audio launch media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 32: {
        label = "No apron media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 33: {
        label = "No info card set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 34: {
        label = "No help card set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 35: {
        label = "No topper media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 36: {
        label = "No backglass media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 37: {
        label = "No DMD media set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 38: {
        label = "No loading video set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 39: {
        label = "No playfield video set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 40: {
        label = "No media for 'Other2' set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      case 41: {
        label = "No wheel icon set.";
        text = NO_MEDIA_TEXT;
        break;
      }
      default: {
        return null;
      }
    }


    return new TableValidation(label, text);
  }
}
