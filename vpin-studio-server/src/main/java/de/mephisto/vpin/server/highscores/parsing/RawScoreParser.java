package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.server.highscores.Score;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RawScoreParser {
  private final static Logger LOG = LoggerFactory.getLogger(RawScoreParser.class);

  @NonNull
  private final String raw;
  @NonNull
  private final Date createdAt;
  private final int gameId;
  private final List<String> titles;

  public RawScoreParser(@NonNull String raw, @NonNull Date createdAt, int gameId, List<String> titles) {
    this.raw = raw;
    this.createdAt = createdAt;
    this.gameId = gameId;
    this.titles = titles;
  }

  public List<Score> parse() {
    List<Score> scores = new ArrayList<>();

    try {
      LOG.debug("Parsing Highscore text: " + raw);
      List<String> lines = Arrays.asList(raw.split("\\n"));
      if (lines.isEmpty()) {
        return scores;
      }

      int index = 0;
      for (String line : new ArrayList<>(lines)) {
        index++;
        if (line.trim().startsWith("HIGHEST SCORES")) {
          lines = lines.subList(index-1, lines.size());
          break;
        }
      }


      index = 1;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        if (titles.contains(line.trim())) {
          String scoreLine = lines.get(i + 1);
          Score score = createTitledScore(createdAt, scoreLine, gameId);
          if (score != null) {
            scores.add(score);
          }
          //do not increase index, as we still search for #1
          continue;
        }

        if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#") || line.indexOf(".:") == 1) {
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
    } catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores;
  }


  @Nullable
  private Score createTitledScore(@NonNull Date createdAt, @NonNull String line, int gameId) {
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
  private Score createScore(@NonNull Date createdAt, @NonNull String line, int gameId) {
    List<String> scoreLineSegments = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
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


  private static double toNumericScore(String score) {
    try {
      String cleanScore = score.trim().replaceAll("\\.", "").replaceAll(",", "");
      return Double.parseDouble(cleanScore);
    } catch (NumberFormatException e) {
      LOG.info("Failed to parse highscore string '" + score + "', ignoring segment '" + score + "'");
      return -1;
    }
  }
}