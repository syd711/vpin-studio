package de.mephisto.vpin.ui.mania.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class ManiaUrlFactory {

  private final static String BASE_URL = "https://app.vpin-mania.net/";

  public static String createTableUrl(@NonNull String vpsTableId, @Nullable String vpsVersionId) {
    if (vpsVersionId != null) {
      return BASE_URL + "table/" + vpsTableId + "/" + vpsVersionId;
    }
    return BASE_URL + "table/" + vpsTableId;
  }
}
