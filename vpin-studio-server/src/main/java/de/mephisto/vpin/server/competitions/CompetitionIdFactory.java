package de.mephisto.vpin.server.competitions;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CompetitionIdFactory {

  @NonNull
  public static String createId(@NonNull Competition competition, boolean isOwner) {
    if (!isOwner) {
      return "vps://competition/" + competition.getType().toLowerCase() + "/remote/" + competition.getId();
    }
    return "vps://competition/" + competition.getType().toLowerCase() + "/local/" + competition.getId();
  }
}
