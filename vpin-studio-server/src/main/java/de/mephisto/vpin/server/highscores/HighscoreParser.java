package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.PreferenceNames;
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

import java.util.*;
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

  @NonNull
  public List<Score> parseScores(@NonNull Date createdAt, @NonNull String raw, int gameId, @Nullable String displayName) {
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
          Score score = createTitledScore(createdAt, scoreLine, gameId, displayName);
          if (score != null) {
            scores.add(score);
          }
          //do not increase index, as we still search for #1
          continue;
        }

        if (line.startsWith(index + ")") || line.startsWith("#" + index) || line.startsWith(index + "#")) {
          Score score = createScore(createdAt, line, gameId, displayName);
          if (score != null) {
            score.setPosition(scores.size() + 1);
            scores.add(score);
          }

          index++;
        }

        if (scores.size() == 3) {
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to parse highscore: " + e.getMessage() + "\nRaw Data:\n==================================\n" + raw, e);
    }
    return scores;
  }

  @NonNull
  private Score createTitledScore(@NonNull Date createdAt, @NonNull String line, int gameId, @Nullable String displayName) {
    String initials = line.trim().substring(0, 3);
    String scoreString = line.substring(4).trim();
    double scoreValue = toNumericScore(scoreString);

    Player p = null;
    Optional<Player> player = playerService.getPlayerForInitials(initials);
    if (player.isPresent()) {
      p = player.get();
    }
    return new Score(createdAt, gameId, initials, p, scoreString, scoreValue, 1, displayName);
  }

  @Nullable
  private Score createScore(@NonNull Date createdAt, @NonNull String line, int gameId, @Nullable String displayName) {
    List<String> collect = Arrays.stream(line.trim().split(" ")).filter(s -> s.trim().length() > 0).collect(Collectors.toList());
    String indexString = collect.get(0).replaceAll("[^0-9]", "");

    Player p = null;
    if (collect.size() == 2) {
      String score = collect.get(1);
      return new Score(createdAt, gameId, "", null, score, toNumericScore(score), -1, displayName);
    }
    else if (collect.size() == 3) {
      String score = collect.get(2);
      String initials = collect.get(1);
      Optional<Player> player = playerService.getPlayerForInitials(initials);
      if (player.isPresent()) {
        p = player.get();
      }
      return new Score(createdAt, gameId, initials, p, score, toNumericScore(score), -1, displayName);
    }
    else if (collect.size() > 3) {
      StringBuilder initials = new StringBuilder();
      for (int i = 1; i < collect.size() - 1; i++) {
        initials.append(collect.get(i));
        initials.append(" ");
      }
      String score = collect.get(collect.size() - 1);
      String playerInitials = initials.toString().trim();
      Optional<Player> player = playerService.getPlayerForInitials(playerInitials);
      if (player.isPresent()) {
        p = player.get();
      }
      return new Score(createdAt, gameId, playerInitials, p, score, toNumericScore(score), -1, displayName);
    }
    else {
      throw new UnsupportedOperationException("Could parse score line for game " + gameId + " '" + line + "'");
    }
  }

  private List<String> getTitleList() {
    String titles = (String) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_TITLES);
    if (StringUtils.isEmpty(titles)) {
      titles = "GRAND CHAMPION"; //always valid
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
    return titleList;
  }

  private static double toNumericScore(String score) {
    try {
      String cleanScore = score.trim().replaceAll("\\.", "").replaceAll(",", "");
      return Double.parseDouble(cleanScore);
    } catch (NumberFormatException e) {
      LOG.error("Failed to parse highscore string '" + score + "'");
      return 0;
    }
  }
}
