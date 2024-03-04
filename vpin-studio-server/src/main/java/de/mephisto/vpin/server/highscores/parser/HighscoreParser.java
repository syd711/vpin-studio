package de.mephisto.vpin.server.highscores.parser;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * e.g.:
 * <p>
 * CANNON BALL CHAMPION
 * TEX - 50
 * <p>
 * GRAND CHAMPION
 * RRR      60.000.000
 * <p>
 * HIGHEST SCORES
 * 1) POP      45.000.000
 * 2) LTD      40.000.000
 * 3) ROB      35.000.000
 * 4) ZAB      30.000.000
 * <p>
 * PARTY CHAMPION
 * PAB      20.000.000
 */
@Service
public class HighscoreParser {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreParser.class);

  @Autowired
  private PlayerService playerService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private List<HighscoreAdapter> customParsers;

  @NonNull
  public List<Score> parseScores(@NonNull Date createdAt, @NonNull String raw, int gameId, long serverId) {
    List<String> titles = getTitleList();
    List<Score> scores = new ArrayList<>();

    try {
      LOG.debug("Parsing Highscore text: " + raw);
      String[] lines = raw.split("\\n");
      if (lines.length == 2) {
        return scores;
      }

      int index = 1;
      for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        if (titles.contains(line.trim())) {
          String scoreLine = lines[i + 1];
          Score score = createTitledScore(createdAt, scoreLine, gameId, serverId);
          if (score != null) {
            scores.add(score);
          }
          //do not increase index, as we still search for #1
          continue;
        }

        if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#") || line.indexOf(".:") == 1) {
          Score score = createScore(createdAt, line, gameId, serverId);
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
  private Score createTitledScore(@NonNull Date createdAt, @NonNull String line, int gameId, long serverId) {
    String initials = "???";
    if (line.trim().length() >= 3) {
      initials = line.trim().substring(0, 3);

      String scoreString = line.substring(4).trim();
      double scoreValue = toNumericScore(scoreString);
      if (scoreValue == -1) {
        return null;
      }

      Player player = playerService.getPlayerForInitials(serverId, initials);
      return new Score(createdAt, gameId, initials, player, scoreString, scoreValue, 1);
    }

    double scoreValue = toNumericScore(line.trim());
    if (scoreValue == -1) {
      return null;
    }

    Player player = playerService.getPlayerForInitials(serverId, initials);
    return new Score(createdAt, gameId, initials, player, line.trim(), scoreValue, 1);
  }

  @Nullable
  private Score createScore(@NonNull Date createdAt, @NonNull String line, int gameId, long serverId) {
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
      Player player = playerService.getPlayerForInitials(serverId, initials);
      double v = toNumericScore(score);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, initials, player, score, v, -1);
    }

    if (scoreLineSegments.size() > 3) {
      StringBuilder initials = new StringBuilder();
      for (int i = 1; i < scoreLineSegments.size() - 1; i++) {
        initials.append(scoreLineSegments.get(i));
        initials.append(" ");
      }
      String score = scoreLineSegments.get(scoreLineSegments.size() - 1);
      String playerInitials = initials.toString().trim();
      Player player = playerService.getPlayerForInitials(serverId, playerInitials);
      double v = toNumericScore(score);
      if (v == -1) {
        return null;
      }
      return new Score(createdAt, gameId, playerInitials, player, score, v, -1);
    }

    for (HighscoreAdapter customParser : customParsers) {
      Score customScore = customParser.parse(playerService, createdAt, line, gameId, serverId);
      if (customScore != null) {
        return customScore;
      }
    }


    throw new UnsupportedOperationException("Could parse score line for game " + gameId + " '" + line + "'");
  }

  private List<String> getTitleList() {
    String titles = (String) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_TITLES);
    if (StringUtils.isEmpty(titles)) {
      titles = "";
    }

    List<String> titleList = new ArrayList<>();
    if (!StringUtils.isEmpty(titles)) {
      String[] split = titles.split(",");
      for (String title : split) {
        if (title.length() > 0) {
          titleList.add(title);
        }
      }
    }

    for (String defaultTitle : DefaultHighscoresTitles.DEFAULT_TITLES) {
      if (!titleList.contains(defaultTitle)) {
        titleList.add(defaultTitle);
      }
    }
    return titleList;
  }

  public static double toNumericScore(String score) {
    try {
      String cleanScore = score.trim().replaceAll("\\.", "").replaceAll(",", "");
      return Double.parseDouble(cleanScore);
    } catch (NumberFormatException e) {
      LOG.info("Failed to parse highscore string '" + score + "', ignoring segment '" + score + "'");
      return -1;
    }
  }
}
