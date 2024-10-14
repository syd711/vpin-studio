package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(ExporterService.class);

  private final static String PARAM_DELIMITER = "delimiter";
  private final static String PARAM_ESCAPE = "escape";
  private final static String PARAM_QUOTE = "quote";

  private final static String PARAM_EMULATOR_ID = "emulatorId";
  private final static String PARAM_GAME_ID = "gameId";

  protected String delimiter = ";";
  protected char escape = '\\';

  @Autowired
  protected FrontendService frontendService;

  @NonNull
  protected CSVPrinter createPrinter(Map<String, String> customQuery, List<String> headers, StringBuilder builder) throws IOException {
    if (customQuery.containsKey(PARAM_DELIMITER)) {
      delimiter = customQuery.get(PARAM_DELIMITER);
    }
    char quote = '\"';
    if (customQuery.containsKey(PARAM_QUOTE)) {
      quote = customQuery.get(PARAM_QUOTE).charAt(0);
    }

    escape = '\\';
    if (customQuery.containsKey(PARAM_ESCAPE)) {
      escape = customQuery.get(PARAM_ESCAPE).charAt(0);
    }

    return CSVFormat.DEFAULT.builder()
        .setHeader(headers.toArray(new String[0]))
        .setDelimiter(delimiter)
        .setTrailingDelimiter(true)
        .setEscape(escape)
        .setQuote(quote)
        .build().print(builder);
  }


  @NonNull
  protected List<Integer> getEmulatorIds(Map<String, String> customQuery) {
    List<Integer> emulatorIds = new ArrayList<>();
    if (customQuery.containsKey(PARAM_EMULATOR_ID)) {
      String s = customQuery.get(PARAM_EMULATOR_ID);
      String[] split = s.split(",");
      for (String string : split) {
        try {
          int i = Integer.parseInt(string);
          emulatorIds.add(i);
        }
        catch (NumberFormatException e) {
          //ignore
        }
      }
    }

    if (emulatorIds.isEmpty()) {
      frontendService.getGameEmulators().stream().map(GameEmulator::getId).forEach(emulatorIds::add);
    }

    return emulatorIds;
  }

  @NonNull
  protected List<Integer> getGameIds(Map<String, String> customQuery) {
    List<Integer> gameIds = new ArrayList<>();
    if (customQuery.containsKey(PARAM_GAME_ID)) {
      String s = customQuery.get(PARAM_GAME_ID);
      String[] split = s.split(",");
      for (String string : split) {
        try {
          int i = Integer.parseInt(string);
          gameIds.add(i);
        }
        catch (NumberFormatException e) {
          //ignore
        }
      }
    }

    return gameIds;
  }
}
