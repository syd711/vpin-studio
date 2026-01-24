package de.mephisto.vpin.server.highscores.parsing.listadapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAdapter extends ScoreListAdapterBase implements ScoreListAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  @Override
  public boolean isApplicable(@NonNull Game game) {
    return true;
  }

  @NonNull
  public List<Score> getScores(@Nullable Game game, @NonNull Date createdAt, @NonNull List<String> lines, List<String> titles) {
    try {
      List<Score> scores = new ArrayList<>();

      int gameId = -1;
      String source = null;
      if (game != null) {
        gameId = game.getId();
        source = game.getGameDisplayName() + "/" + game.getRom() + "/" + game.getHsFileName();
      }

      int index = 1;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);

        //Check if there is a highscore title, in that case...
        if (titles.contains(line.trim()) && ((i + 1) < titles.size())) {
          if (i + 1 >= lines.size()) {
            continue;
          }

          String scoreLine = lines.get(i + 1);

          //the next line could be a raw score without a positions
          if (!isScoreLine(scoreLine, (i + 1))) {
            Score score = createTitledScore(createdAt, scoreLine, source, gameId);
            if (score != null) {
              scores.add(score);
            }
            //do not increase index, as we still search for #1
            continue;
          }
        }

        if (isScoreLine(line, index)) {
          Score score = createScore(createdAt, line, source, gameId);
          if (score != null) {
            score.setPosition(scores.size() + 1);
            scores.add(score);
            index++;
          }
        }

        if (scores.size() >= 3 && StringUtils.isEmpty(line)) {
          break;
        }
      }

      return filterDuplicates(scores);
    }
    catch (Exception e) {
      if (game != null) {
        LOG.error("Score parsing failed for \"" + game.getGameDisplayName() + "\": {}", e.getMessage(), e);
      }
      else {
        LOG.error("Score parsing failed: {}", e.getMessage(), e);
      }
      throw e;
    }
  }

  protected static boolean isScoreLine(String line, int index) {
    return line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#") || line.indexOf(".:") == 1;
  }

  /**
   * Parses score that are shown right behind a possible title.
   * These scores do not have a leading position number.
   */
  @Nullable
  protected Score createTitledScore(@NonNull Date createdAt, @NonNull String line, @Nullable String source, int gameId) {
    String initials = "???";
    if (line.trim().length() >= 3) {
      initials = line.trim().substring(0, 3);

      String scoreString = line.substring(4).trim();
      long scoreValue = toNumericScore(scoreString, source, false);
      if (scoreValue == -1) {
        return null;
      }

      return new Score(createdAt, gameId, initials, null, scoreString, scoreValue, 1);
    }

    long scoreValue = toNumericScore(line.trim(), source, false);
    if (scoreValue == -1) {
      return null;
    }

    return new Score(createdAt, gameId, initials, null, line.trim(), scoreValue, 1);
  }

  @Nullable
  protected Score createScore(@NonNull Date createdAt, @NonNull String line, @Nullable String source, int gameId) {
    List<String> scoreLineSegments = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
    if (scoreLineSegments.size() == 2) {
      String score = scoreLineSegments.get(1);
      long v = toNumericScore(score, source, true);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, "", null, score, v, -1);
    }

    if (scoreLineSegments.size() == 3) {
      String score = scoreLineSegments.get(2);
      long v = toNumericScore(score, source, true);
      String initials = scoreLineSegments.get(1);
      initials = ScoreFormatUtil.cleanInitials(initials);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, initials, null, score, v, -1);
    }

    if (scoreLineSegments.size() > 3) {
      StringBuilder initials = new StringBuilder();
      for (int i = 1; i < scoreLineSegments.size() - 1; i++) {
        initials.append(scoreLineSegments.get(i));
        initials.append(" ");
      }
      String score = scoreLineSegments.get(scoreLineSegments.size() - 1);
      String playerInitials = initials.toString().trim();
      long v = toNumericScore(score, source, true);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, playerInitials, null, score, v, -1);
    }

    throw new UnsupportedOperationException("Could parse score line for game " + gameId + " '" + line + "'");
  }

}