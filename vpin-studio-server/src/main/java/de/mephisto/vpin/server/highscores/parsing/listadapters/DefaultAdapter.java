package de.mephisto.vpin.server.highscores.parsing.listadapters;

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

public class DefaultAdapter extends ScoreListAdapterBase implements ScoreListAdapter {

  @Override
  public boolean isApplicable(@NonNull Game game) {
    return true;
  }

  @NonNull
  public List<Score> getScores(@NonNull Game game, @NonNull Date createdAt, @NonNull List<String> lines,List<String> titles) {
    List<Score> scores = new ArrayList<>();
    int gameId = -1;

    if (game != null) {
      gameId = game.getId();
    }

    int index = 1;
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (titles.contains(line.trim())) {
        String scoreLine = lines.get(i + 1);
        Score score = createTitledScore(createdAt, scoreLine, gameId);
        if (score != null) {
          scores.add(score);
        }
        // do not increase index, as we still search for #1
        continue;
      }

      if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#")
          || line.indexOf(".:") == 1) {
        Score score = createScore(createdAt, line, gameId);
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

  @Nullable
  private static Score createTitledScore(@NonNull Date createdAt, @NonNull String line, int gameId) {
    String initials = "???";
    if (line.trim().length() >= 3) {
      initials = line.trim().substring(0, 3);

      String scoreString = line.substring(4).trim();
      double scoreValue = toNumericScore(scoreString);
      if (scoreValue == -1) {
        return null;
      }

      return new Score(createdAt, gameId, initials, null, scoreString, scoreValue, 1);
    }

    double scoreValue = toNumericScore(line.trim());
    if (scoreValue == -1) {
      return null;
    }

    return new Score(createdAt, gameId, initials, null, line.trim(), scoreValue, 1);
  }

  @Nullable
  private static Score createScore(@NonNull Date createdAt, @NonNull String line, int gameId) {
    List<String> scoreLineSegments = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0)
        .collect(Collectors.toList());
    if (scoreLineSegments.size() == 2) {
      String score = scoreLineSegments.get(1);
      double v = toNumericScore(score);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, "", null, score, v, -1);
    }

    if (scoreLineSegments.size() == 3) {
      String score = scoreLineSegments.get(2);
      String initials = scoreLineSegments.get(1);
      double v = toNumericScore(score);
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
      double v = toNumericScore(score);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, playerInitials, null, score, v, -1);
    }

    throw new UnsupportedOperationException("Could parse score line for game " + gameId + " '" + line + "'");
  }
}
