package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class HighscoreExportService extends ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreExportService.class);

  @Autowired
  private GameService gameService;

  public String export(Map<String, String> customQuery) throws IOException {
    try {
      StringBuilder builder = new StringBuilder();
      List<String> headers = new ArrayList<>(Arrays.asList("gameId", "gameDisplayName", "createdAt"));
      for (int i = 0; i < 10; i++) {
        headers.add("playerInitials" + (i + 1));
        headers.add("score" + (i + 1));
      }

      CSVPrinter printer = createPrinter(customQuery, headers, builder);

      List<Integer> emulatorIds = getEmulatorIds(customQuery);
      List<Integer> gameIds = getGameIds(customQuery);
      for (Integer emulatorId : emulatorIds) {
        List<Game> knownGames = frontendService.getGamesByEmulator(emulatorId);
        for (Game game : knownGames) {
          try {
            if (!gameIds.isEmpty() && !gameIds.contains(game.getId())) {
              continue;
            }
            if (!game.isVpxGame()) {
              continue;
            }

            ScoreSummary scores = gameService.getScores(game.getId());
            if (scores != null) {
              List<String> records = new ArrayList<>();

              records.add(String.valueOf(game.getId()));
              records.add(String.valueOf(game.getGameDisplayName()));
              records.add(DateUtil.formatDateTime(scores.getCreatedAt()));

              List<Score> scoresEntries = scores.getScores();
              int count = 0;
              for (Score scoresEntry : scoresEntries) {
                records.add(scoresEntry.getPlayerInitials());
                records.add(String.valueOf(scoresEntry.getFormattedScore()));
                count++;
                if (count == 10) {
                  break;
                }
              }

              printer.printRecord(records);
            }
          }
          catch (Exception e) {
            LOG.error("Export failed for table \"" + game.getGameDisplayName() + "\":" + e.getMessage(), e);
          }
        }
      }

      return builder.toString();
    }
    catch (Exception e) {
      LOG.error("Failed to export highscore data: " + e.getMessage(), e);
      return "Failed to export highscore data: " + e.getMessage();
    }
  }
}
