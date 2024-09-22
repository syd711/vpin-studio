package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class TableExporterService extends ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(TableExporterService.class);

  private final List<String> SYSTEM_IGNORE_LIST = Arrays.asList("class", "defaultBackgroundAvailable", "directB2SPath", "gameFilePath", "gameFileSize", "disabled",
      "eventLogAvailable", "foundControllerStop", "dateAdded", "dateUpdated", "launcherList", "romExists", "foundTableExit", "gameMedia", "ignoredValidations",
      "playlists", "pupPack",
      "romRequired", "wheelImage", "vpxGame", "vpsUpdates", "validationState", "updateAvailable", "templateId");

  private final List<String> FRONTEND_IGNORE_LIST = Arrays.asList("emulatorId");

  private final static String PARAM_SOURCE = "source";
  private final static String PARAM_SOURCE_STUDIO = "studio";
  private final static String PARAM_SOURCE_FRONTEND = "frontend";

  private final static String PARAM_IGNORE_LIST = "ignoreList";

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
        for (Game game : knownGames) {
          try {
            if (!gameIds.isEmpty() && !gameIds.contains(game.getId())) {
              continue;
            }

            List<Object> records = new ArrayList<>();
            exportGame(game, studioFieldHeaders, records);
            exportTableDetails(game, frontendFieldHeaders, records);

            if (!records.isEmpty()) {
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
      LOG.error("Export failed: " +e.getMessage(), e);
      return "Export failed: " + e.getMessage();
    }
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
