package de.mephisto.vpin.server.highscores.parsing.listadapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.highscores.Score;

public class ScoreListAdapterBase {

  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  protected long toNumericScore(@NonNull String score, @Nullable String source) {
    try {
      String cleanScore = ScoreFormatUtil.cleanScore(score);
      return Long.parseLong(cleanScore);
    }
    catch (NumberFormatException e) {
      LOG.warn("Failed to parse numeric highscore string '{}', ignoring segment '{}', source: {}", score, score, source);
      return -1;
    }
  }

  protected List<Score> filterDuplicates(List<Score> scores) {
    List<Score> scoreList = new ArrayList<>();
    int pos = 1;
    for (Score s : scores) {
      Optional<Score> match = scoreList.stream().filter(score -> score.getFormattedScore().equals(s.getFormattedScore()) && String.valueOf(score.getPlayerInitials()).equals(s.getPlayerInitials())).findFirst();
      if (match.isPresent()) {
        continue;
      }
      s.setPosition(pos);
      scoreList.add(s);
      pos++;
    }
    return scoreList;
  }
}
