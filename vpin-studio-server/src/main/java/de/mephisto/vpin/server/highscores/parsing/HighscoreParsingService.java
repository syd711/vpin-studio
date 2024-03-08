package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class HighscoreParsingService {

  @Autowired
  private PlayerService playerService;

  @Autowired
  private PreferencesService preferencesService;

  @NonNull
  public List<Score> parseScores(@NonNull Date createdAt, @NonNull String raw, int gameId, long serverId) {
    RawScoreParser parser = new RawScoreParser(raw, createdAt, gameId, getTitleList());
    List<Score> scores = parser.parse();

    for (Score score : scores) {
      Player player = playerService.getPlayerForInitials(serverId, score.getPlayerInitials());
      score.setPlayer(player);
    }

    return scores;
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
}
