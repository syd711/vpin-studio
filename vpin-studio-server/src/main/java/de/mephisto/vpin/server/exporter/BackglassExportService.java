package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.csv.CSVPrinter;
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
public class BackglassExportService extends ExporterService {
  private final static Logger LOG = LoggerFactory.getLogger(BackglassExportService.class);

  private final List<String> IGNORE_LIST = Arrays.asList("class", "filename", "filesize", "gameId", "name", "emulatorId");

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private GameService gameService;


  public String export(Map<String, String> customQuery) throws IOException {
    try {
      StringBuilder builder = new StringBuilder();
      List<String> headers = new ArrayList<>();
      headers.add("gameId");
      headers.add("vpxFile");
      headers.add("backglassHidden");
      headers.add("dmdHidden");
      headers.add("startInBackground");
      headers.add("hideGrill");

      PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(DirectB2SData.class);
      for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
        String name = propertyDescriptor.getName();
        if (!IGNORE_LIST.contains(name)) {
          headers.add(name);
        }
      }

      //headers.add("backgroundWidth");
      //headers.add("backgroundHeight");
      //headers.add("dmdWidth");
      //headers.add("dmdHeight");

      CSVPrinter printer = createPrinter(customQuery, headers, builder);

      List<Integer> emulatorIds = getEmulatorIds(customQuery);
      List<Integer> gameIds = getGameIds(customQuery);

      List<Game> allGames = gameService.getKnownGames(-1);
      for (Game game : allGames) {
        try {
          if (!gameIds.isEmpty() && !gameIds.contains(game.getId())) {
            continue;
          }
          if (!emulatorIds.isEmpty() && !emulatorIds.contains(game.getEmulatorId())) {
            continue;
          }
          if (!game.isVpxGame()) {
            continue;
          }
          if (!game.getDirectB2SFile().exists()) {
            continue;
          }


          DirectB2SData directB2SData = backglassService.getDirectB2SData(game.getId());
          DirectB2STableSettings tableSettings = backglassService.getTableSettings(game.getId());
          if (directB2SData != null) {
            List<String> records = new ArrayList<>();
            records.add(String.valueOf(game.getId()));
            records.add(game.getGameFileName());

            if (tableSettings != null) {
              records.add(String.valueOf(tableSettings.isHideB2SBackglass()));
              records.add(String.valueOf(tableSettings.isHideB2SDMD()));
              records.add(String.valueOf(tableSettings.getStartBackground()));
              records.add(String.valueOf(tableSettings.getHideGrill()));
            }
            else {
              records.add("");
              records.add("");
              records.add("");
              records.add("");
            }

            for (int i = 2; i < headers.size() - 4; i++) {
              String header = headers.get(i);
              try {
                Object property = PropertyUtils.getProperty(directB2SData, header);
                if (property == null) {
                  property = "";
                }
                else {
                  property = ExportEntityConverter.convert(header, property);
                }
                records.add(String.valueOf(property));
              }
              catch (Exception e) {

              }
            }

            printer.printRecord(records);
            LOG.info("Finished backglass export of " + game);
          }
        }
        catch (Exception e) {
          LOG.error("Export failed for table \"" + game.getGameDisplayName() + "\":" + e.getMessage(), e);
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
