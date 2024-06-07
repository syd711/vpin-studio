package de.mephisto.vpin.server.competitions;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CompetitionIdFactory {

  @NonNull
  public static String createId(@NonNull Competition competition) {
    return "vps://competition/" + competition.getType().toLowerCase() + "/" + competition.getId();
  }
}
