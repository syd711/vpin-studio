package de.mephisto.vpin.server.competitions;

import com.drew.metadata.StringValue;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CompetitionIdHelper {

  @NonNull
  public static List<CompetitionType> getCompetitionTypes(@Nullable String value) {
    List<CompetitionType> types = new ArrayList<>();
    if (!StringUtils.isEmpty(value)) {
      for (CompetitionType competitionType : CompetitionType.values()) {
        if (value.contains(competitionType.name().toLowerCase())) {
          types.add(competitionType);
        }
      }
    }
    return types;
  }
}
