package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DataExporterService extends ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(DataExporterService.class);

  private final List<String> SYSTEM_IGNORE_LIST = Arrays.asList("class", "defaultBackgroundAvailable", "directB2SPath", "gameFilePath", "gameFileSize", "disabled",
      "eventLogAvailable", "foundControllerStop", "dateAdded", "dateUpdated", "launcherList", "romExists", "foundTableExit", "gameMedia", "ignoredValidations",
      "playlists", "pupPack",
      "romRequired", "wheelImage", "vpxGame", "vpsUpdates", "validationState", "updateAvailable", "templateId");

  private final List<String> FRONTEND_IGNORE_LIST = Arrays.asList("emulatorId");

  private final static String PARAM_SOURCE = "source";
  private final static String PARAM_SOURCE_STUDIO = "studio";
  private final static String PARAM_SOURCE_FRONTEND = "frontend";

  private final static String PARAM_IGNORE_LIST = "ignoreList";
  private final static String PARAM_EMULATOR_ID = "emulatorId";
  private final static String PARAM_GAME_ID = "gameId";

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;

  public String export(Map<String, String> customQuery) throws Exception {
    try {
      List<String> ignoredList = updateIgnoreList(customQuery);
      List<String> sources = resolveSources(customQuery);

      List<String> studioFieldHeaders = new ArrayList<>();
      if (sources.contains(PARAM_SOURCE_STUDIO)) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(Game.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
          String name = propertyDescriptor.getName();
          if (ignoredList.contains(name)) {
            continue;
          }
          studioFieldHeaders.add(name);
        }
      }

      List<String> frontendFieldHeaders = new ArrayList<>();
      if (sources.contains(PARAM_SOURCE_FRONTEND)) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(TableDetails.class);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
          String name = propertyDescriptor.getName();
          if (ignoredList.contains(name) || FRONTEND_IGNORE_LIST.contains(name)) {
            continue;
          }
          frontendFieldHeaders.add(name);
        }
      }

      StringBuilder builder = new StringBuilder();
      List<String> allHeaders = new ArrayList<>(studioFieldHeaders);
      allHeaders.addAll(frontendFieldHeaders);
      final CSVPrinter printer = createPrinter(customQuery, allHeaders, builder);

      List<Integer> emulatorIds = getEmulatorIds(customQuery);
      List<Integer> gameIds = getGameIds(customQuery);
      for (Integer emulatorId : emulatorIds) {
        List<Game> knownGames = frontendService.getGamesByEmulator(emulatorId);
        for (Game knownGame : knownGames) {
          if (!gameIds.isEmpty() && !gameIds.contains(knownGame.getId())) {
            continue;
          }

          List<Object> records = new ArrayList<>();
          exportGame(knownGame, studioFieldHeaders, records);
          exportTableDetails(knownGame, frontendFieldHeaders, records);

          if (!records.isEmpty()) {
            printer.printRecord(records);
          }
        }
      }
      return builder.toString();
    }
    catch (Exception e) {
      LOG.error("Export failed: " +e.getMessage(), e);
      return "Export failed: " + e.getMessage();
    }
  }

  @NotNull
  private List<Integer> getEmulatorIds(Map<String, String> customQuery) {
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

  @NotNull
  private List<Integer> getGameIds(Map<String, String> customQuery) {
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

  private void exportTableDetails(Game knownGame, List<String> frontendFieldHeaders, List<Object> records) {
    TableDetails tableDetails = frontendService.getTableDetails(knownGame.getId());
    for (String header : frontendFieldHeaders) {
      try {
        Object property = PropertyUtils.getProperty(tableDetails, header);
        if (property == null) {
          property = "";
        }
        else {
          property = ExportEntityConverter.convert(header, property);
        }
        records.add(property);
      }
      catch (Exception e) {
      }
    }
  }

  private static void exportGame(Game knownGame, List<String> studioFieldHeaders, List<Object> records) {
    for (String header : studioFieldHeaders) {
      try {
        Object property = PropertyUtils.getProperty(knownGame, header);
        if (property == null) {
          property = "";
        }
        else {
          property = ExportEntityConverter.convert(header, property);
        }
        records.add(property);
      }
      catch (Exception e) {
//            LOG.info("Last value of games reached: " + e.getMessage());
        break;
      }
    }
  }

  private List<String> updateIgnoreList(Map<String, String> customQuery) {
    List<String> ignoreList = new ArrayList<>(SYSTEM_IGNORE_LIST);

    if (customQuery.containsKey(PARAM_IGNORE_LIST)) {
      String ignoreListValue = customQuery.get(PARAM_IGNORE_LIST);
      String[] split = ignoreListValue.split(",");
      for (String s : split) {
        if (!StringUtils.isEmpty(s)) {
          ignoreList.add(s.trim());
        }
      }
    }
    return ignoreList;
  }

  private static List<String> resolveSources(Map<String, String> customQuery) {
    List<String> sources = new ArrayList<>();
    if (customQuery.containsKey(PARAM_SOURCE)) {
      String sourceString = customQuery.get(PARAM_SOURCE);
      if (sourceString.toLowerCase().contains(PARAM_SOURCE_STUDIO)) {
        sources.add(PARAM_SOURCE_STUDIO);
      }
      if (sourceString.toLowerCase().contains(PARAM_SOURCE_FRONTEND)) {
        sources.add(PARAM_SOURCE_FRONTEND);
      }
    }

    if (sources.isEmpty()) {
      sources.add(PARAM_SOURCE_STUDIO);
      sources.add(PARAM_SOURCE_FRONTEND);
    }
    return sources;
  }
}
