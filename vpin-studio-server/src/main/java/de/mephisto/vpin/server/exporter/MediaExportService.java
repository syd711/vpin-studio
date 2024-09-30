package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.games.Game;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class MediaExportService extends ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(MediaExportService.class);

  public String export(Map<String, String> customQuery) throws IOException {
    try {
      StringBuilder builder = new StringBuilder();
      List<String> headers = new ArrayList<>(Arrays.asList("gameId", "gameDisplayName"));

      List<VPinScreen> screens = frontendService.getFrontend().getSupportedScreens();
      for (VPinScreen screen : screens) {
        headers.add(screen.name());
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

            List<String> records = new ArrayList<>();

            records.add(String.valueOf(game.getId()));
            records.add(game.getGameDisplayName());

            FrontendMedia gameMedia = frontendService.getGameMedia(game);

            for (VPinScreen screen : screens) {
              List<FrontendMediaItem> mediaItems = gameMedia.getMediaItems(screen);
              records.add(String.valueOf(mediaItems.size()));
            }

            printer.printRecord(records);
          }
          catch (Exception e) {
            LOG.error("Export failed for table \"" + game.getGameDisplayName() + "\":" + e.getMessage(), e);
          }
        }
      }

      return builder.toString();
    }
    catch (Exception e) {
      LOG.error("Failed to export media data: " + e.getMessage(), e);
      return "Failed to export media data: " + e.getMessage();
    }
  }
}
