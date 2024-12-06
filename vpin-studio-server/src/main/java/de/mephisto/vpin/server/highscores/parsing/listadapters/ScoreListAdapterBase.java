package de.mephisto.vpin.server.highscores.parsing.listadapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.highscores.Score;

public class ScoreListAdapterBase {

  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  protected static double toNumericScore(String score) {
    try {
      String cleanScore = ScoreFormatUtil.cleanScore(score);
      return Double.parseDouble(cleanScore);
    } catch (NumberFormatException e) {
      LOG.info("Failed to parse highscore string '" + score + "', ignoring segment '" + score + "'");
      return -1;
    }
  }

  protected static List<Score> filterDuplicates(List<Score> scores) {
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
