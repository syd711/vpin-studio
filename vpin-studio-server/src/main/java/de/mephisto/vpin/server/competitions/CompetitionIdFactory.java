package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CompetitionIdFactory {

  @NonNull
  public static List<CompetitionType> getCompetitionTypes(@Nullable String value) {
    List<CompetitionType> types = new ArrayList<>();
    if (!StringUtils.isEmpty(value)) {
      for (CompetitionType competitionType : CompetitionType.values()) {
        if (value.toLowerCase().contains(competitionType.name().toLowerCase())) {
          types.add(competitionType);
        }
      }
    }
    return types;
  }

  @NonNull
  public static String createId(@NonNull Competition competition, boolean isOwner) {
    if (!isOwner) {
      return "vps://competition/" + competition.getType().toLowerCase() + "/remote/" + competition.getId();
    }
    return "vps://competition/" + competition.getType().toLowerCase() + "/local/" + competition.getId();
  }
}
