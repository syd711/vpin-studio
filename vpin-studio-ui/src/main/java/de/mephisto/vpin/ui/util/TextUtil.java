package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class TextUtil {

  @Nullable
  public static String getValidationMessage(@NonNull GameRepresentation game) {
    int code = game.getValidationState();
    if(code == 1) {
      return "No ROM found.";
    }

    if(code == 2) {
      return "No PUP pack and no directb2s file found.";
    }

    if(code == 3) {
      return "No wheel icon set.";
    }

    return null;
  }
}
