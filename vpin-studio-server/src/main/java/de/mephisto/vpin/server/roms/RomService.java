package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RomService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(RomService.class);

  private Map<String, String> aliasMapping = new HashMap<>();

  public RomService() {
  }

  @Autowired
  private SystemService systemService;

  @NonNull
  public ScanResult scanGameFile(@NonNull Game game) {
    if (game.getEmulator().getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL_X) || game.getEmulator().getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL)) {
      if (game.getGameFile().exists()) {
        return VPXFileScanner.scan(game.getGameFile());
      }

      LOG.info("Skipped reading of " + game.getGameDisplayName() + ", VPX file '" + game.getGameFile().getAbsolutePath() + "' does not exist.");
      return new ScanResult();
    }
    LOG.info("Skipped reading of " + game.getGameDisplayName() + ", only VPX tables can be scanned.");
    return new ScanResult();
  }

  @Nullable
  public String getOriginalRom(@Nullable String rom) {
    if (rom != null && this.aliasMapping.containsKey(rom)) {
      return aliasMapping.get(rom);
    }
    return null;
  }

  public boolean clearCache() {
    this.aliasMapping.clear();
    this.loadAliasMapping();
    return true;
  }

  public boolean saveAliasMapping(Map<String, Object> values) throws IOException {
    Set<Map.Entry<String, Object>> entries = values.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String alias = entry.getKey();
      String romName = (String) entry.getValue();

      if (!aliasMapping.containsValue(alias)) {
        aliasMapping.put(alias.trim(), romName.trim());
      }
    }

    String mapAsString = aliasMapping.keySet().stream().map(key -> key.trim() + "," + aliasMapping.get(key).trim()).sorted().collect(Collectors.joining("\n"));
    File vpmAliasFile = systemService.getVPMAliasFile();
    FileUtils.writeStringToFile(vpmAliasFile, mapAsString, Charset.defaultCharset());
    loadAliasMapping();
    return true;
  }

  private void loadAliasMapping() {
    File vpmAliasFile = systemService.getVPMAliasFile();
    try {
      if (vpmAliasFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(vpmAliasFile);
        List<String> mappings = IOUtils.readLines(fileInputStream, "utf8");
        fileInputStream.close();

        for (String mapping : mappings) {
          if (mapping.contains(",")) {
            String[] split = mapping.split(",");
            String[] aliases = Arrays.copyOfRange(split, 0, split.length - 1);
            String originalRom = split[split.length - 1];

            for (String alias : aliases) {
              aliasMapping.put(alias, originalRom);
            }
          }
        }
        LOG.info("Loaded " + aliasMapping.size() + " alias mappings.");
      }
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() {
    this.loadAliasMapping();
  }
}
