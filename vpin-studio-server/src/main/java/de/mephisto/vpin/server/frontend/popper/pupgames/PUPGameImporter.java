package de.mephisto.vpin.server.frontend.popper.pupgames;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PUPGameImporter {
  private final static Logger LOG = LoggerFactory.getLogger(PUPGameImporter.class);

  private final static Map<String, List<TableDetails>> pupGames = new HashMap<>();

  public static List<TableDetails> read(EmulatorType emulatorType, int emulatorId) {
    List<TableDetails> result = new ArrayList<>();
    switch (emulatorType) {
      case Zaccaria: {
        result.addAll(importPupGames("zaccaria.json"));
        break;
      }
      case ZenFX: {
        result.addAll(importPupGames("pinball_fx.json"));
        break;
      }
      case ZenFX3: {
        result.addAll(importPupGames("pinball_fx3.json"));
        break;
      }
      case PinballM: {
        result.addAll(importPupGames("pinball_m.json"));
        break;
      }
    }

    result.stream().forEach(g -> g.setEmulatorId(emulatorId));
    return result;
  }

  private static List<TableDetails> importPupGames(String filename) {
    if (pupGames.containsKey(filename)) {
      return pupGames.get(filename);
    }

    LOG.info("Running pupgames importer for {}", filename);
    List<TableDetails> result = new ArrayList<>();
    try {
      File file = new File(SystemInfo.RESOURCES, "pupgames/" + filename);
      if (file.exists()) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        PUPGameExport pupGameExport = mapper.readValue(s, PUPGameExport.class);
        List<PUPGame> gameExport = pupGameExport.getGameExport();
        for (PUPGame pupGame : gameExport) {
          result.add(pupGame.toTableDetails());
        }
      }
      else {
        LOG.warn("pupgame file {} not found.", file.getAbsolutePath());
      }
    }
    catch (IOException e) {
      LOG.error("Failed to read pupgame file {}: {}", filename, e.getMessage(), e);
    }
    pupGames.put(filename, result);
    return result;
  }
}
