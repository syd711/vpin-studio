package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class HighscoreExportService extends ExporterService {

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;


  public String export(Map<String, String> customQuery) throws IOException {
    StringBuilder builder = new StringBuilder();
    List<String> headers = Arrays.asList("gameId", "gameDisplayName", "createdAt", "position", "playerInitials", "score", "scoreFormatted");
    CSVPrinter printer = createPrinter(customQuery, headers, builder);

    List<Game> knownGames = gameService.getKnownGames(-1);
    for (Game game : knownGames) {
      ScoreSummary scores = gameService.getScores(game.getId());
      if (scores != null) {
        List<String> records = new ArrayList<>();

        records.add(String.valueOf(game.getId()));
        records.add(String.valueOf(game.getGameDisplayName()));
        records.add(String.valueOf(game.getDateUpdated()));
        records.add(String.valueOf(scores.getCreatedAt()));

        List<Score> scoresEntries = scores.getScores();
        int count = 0;
        for (Score scoresEntry : scoresEntries) {
          records.add(String.valueOf(scoresEntry.getPosition()));
          records.add(scoresEntry.getPlayerInitials());
          records.add(String.valueOf(scoresEntry.getScore()));
          records.add(scoresEntry.getFormattedScore());
          count++;
          if(count == 10) {
            break;
          }
        }

        printer.printRecord(records);
      }
    }


    return builder.toString();
  }
}
