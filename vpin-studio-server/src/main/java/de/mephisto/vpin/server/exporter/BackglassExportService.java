package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.util.ImageUtil;
import javafx.scene.image.Image;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
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

  @Autowired
  private DefaultPictureService defaultPictureService;

  private final boolean forceBackglassExtraction = false;

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

      headers.add("backgroundWidth");
      headers.add("backgroundHeight");
      headers.add("dmdWidth");
      headers.add("dmdHeight");

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
              records.add(String.valueOf(tableSettings.isStartBackground()));
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

            exportBackgroundData(game, records, game.getEmulatorId());
            exportDMDData(game, directB2SData, records, game.getEmulatorId());

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

  private void exportBackgroundData(Game game, List<String> records, int emulatorId) throws IOException {
    if (!forceBackglassExtraction) {
      File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
      if (!rawDefaultPicture.exists()) {
        defaultPictureService.extractDefaultPicture(game);
        rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
      }

      if (rawDefaultPicture.exists()) {
        BufferedImage image = ImageUtil.loadImage(rawDefaultPicture);
        int backgroundWidth = (int) image.getWidth();
        int backgroundHeight = (int) image.getHeight();
        records.add(String.valueOf(backgroundWidth));
        records.add(String.valueOf(backgroundHeight));
      }
      else {
        records.add(String.valueOf(0));
        records.add(String.valueOf(0));
      }
    }
    else {
      String filename = FilenameUtils.getBaseName(game.getGameFileName()) + ".directb2s";
      String backgroundBase64 = backglassService.getBackgroundBase64(emulatorId, filename);
      if (backgroundBase64 != null) {
        byte[] imageData = DatatypeConverter.parseBase64Binary(backgroundBase64);
        Image image = new Image(new ByteArrayInputStream(imageData));
        int backgroundWidth = (int) image.getWidth();
        int backgroundHeight = (int) image.getHeight();
        records.add(String.valueOf(backgroundWidth));
        records.add(String.valueOf(backgroundHeight));
      }
      else {
        records.add(String.valueOf(0));
        records.add(String.valueOf(0));
      }
    }
  }

  private void exportDMDData(Game game, DirectB2SData data, List<String> records, int emulatorId) throws IOException {
    if (!forceBackglassExtraction) {
      File picture = defaultPictureService.getDMDPicture(game);
      if (picture.exists()) {
        BufferedImage image = ImageUtil.loadImage(picture);
        int backgroundWidth = (int) image.getWidth();
        int backgroundHeight = (int) image.getHeight();
        records.add(String.valueOf(backgroundWidth));
        records.add(String.valueOf(backgroundHeight));
      }
      else {
        records.add(String.valueOf(0));
        records.add(String.valueOf(0));
      }
    }
    else if (data.isDmdImageAvailable()) {
      String filename = FilenameUtils.getBaseName(game.getGameFileName()) + ".directb2s";

      String dmdBase64 = backglassService.getDmdBase64(emulatorId, filename);
      if (dmdBase64 != null) {
        byte[] dmdData = DatatypeConverter.parseBase64Binary(dmdBase64);
        Image dmdImage = new Image(new ByteArrayInputStream(dmdData));
        int dmdWidth = (int) dmdImage.getWidth();
        int dmdHeight = (int) dmdImage.getHeight();
        records.add(String.valueOf(dmdWidth));
        records.add(String.valueOf(dmdHeight));
      }
      else {
        records.add(String.valueOf(0));
        records.add(String.valueOf(0));
      }
    }
  }
}
