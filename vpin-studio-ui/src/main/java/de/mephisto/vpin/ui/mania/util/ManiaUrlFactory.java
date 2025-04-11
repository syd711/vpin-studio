package de.mephisto.vpin.ui.mania.util;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ManiaUrlFactory {

  private final static String BASE_URL = "https://app.vpin-mania.net/";

  public static String createTableUrl(@NonNull String vpsTableId) {
    return BASE_URL + "tables/" + vpsTableId;
  }
}
