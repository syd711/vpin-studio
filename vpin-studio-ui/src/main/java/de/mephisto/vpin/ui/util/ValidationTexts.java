package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * See GameValidator
 */
public class ValidationTexts {

  @Nullable
  public static String getValidationMessage(@NonNull GameRepresentation game) {
    int code = game.getValidationState();
    if(code == -1) {
      return null;
    }

    if (code == 1) {
      return "No ROM name could be resolved.";
    }
    else if (code == 2) {
      return "Another table with the same ROM exists. Consider setting a NVOffset for this table.";
    }
    else if (code == 3) {
      return "ROM file does not exist.";
    }
    else if (code == 20) {
      return "No PUP pack and no directb2s file found.";
    }
    else if (code == 60) {
      return "No highscore files found. Maybe this table has not been played yet?";
    }
    else if (code == 30) {
      return "No audio media set.";
    }
    else if (code == 31) {
      return "No audio launch media set.";
    }
    else if (code == 32) {
      return "No apron media set.";
    }
    else if (code == 33) {
      return "No info card set.";
    }
    else if (code == 34) {
      return "No help card set.";
    }
    else if (code == 35) {
      return "No topper media set.";
    }
    else if (code == 36) {
      return "No backglass media set.";
    }
    else if (code == 37) {
      return "No DMD media set.";
    }
    else if (code == 38) {
      return "No loading video set.";
    }
    else if (code == 39) {
      return "No playfield video set.";
    }
    else if (code == 40) {
      return "No media for 'Other2' set.";
    }
    else if (code == 41) {
      return "No wheel icon set.";
    }

    return null;
  }
}
