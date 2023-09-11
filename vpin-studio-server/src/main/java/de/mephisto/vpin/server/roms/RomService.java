package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.restclient.popper.EmulatorType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.VPXFileScanner;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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

  private Map<String, String> aliasToRomMapping = new HashMap<>();

  public RomService() {
  }

  @Autowired
  private SystemService systemService;

  @NonNull
  public ScanResult scanGameFile(@NonNull Game game) {
    if (Emulator.isVisualPinball(game.getEmulator().getName())) {
      if (game.getGameFile().exists()) {
        return VPXFileScanner.scan(game.getGameFile());
      }

      LOG.info("Skipped reading of " + game.getGameDisplayName() + ", VPX file '" + game.getGameFile().getAbsolutePath() + "' does not exist.");
      return new ScanResult();
    }
    LOG.info("Skipped reading of " + game.getGameDisplayName() + " (emulator '" + game.getEmulator() + "'), only VPX tables can be scanned.");
    return new ScanResult();
  }

  @Nullable
  public String getRomAlias(@Nullable String rom) {
    if (rom == null) {
      return null;
    }

    Set<Map.Entry<String, String>> entries = aliasToRomMapping.entrySet();
    for (Map.Entry<String, String> entry : entries) {
      String alias = entry.getKey();
      String romName = entry.getValue();

      if (romName.equals(rom)) {
        return alias;
      }
    }
    return null;
  }

  public String getRomForAlias(@Nullable String romAlias) {
    if (romAlias == null) {
      return null;
    }

    Set<Map.Entry<String, String>> entries = aliasToRomMapping.entrySet();
    for (Map.Entry<String, String> entry : entries) {
      String alias = entry.getKey();
      String romName = entry.getValue();

      if (alias.equals(romAlias)) {
        return romName;
      }
    }
    return null;
  }

  public boolean clearCache() {
    this.aliasToRomMapping.clear();
    this.loadAliasMapping();
    return true;
  }

  public boolean deleteAliasMapping(String alias) throws IOException {
    if (!StringUtils.isEmpty(alias) && aliasToRomMapping.containsKey(alias)) {
      aliasToRomMapping.remove(alias);
      LOG.info("Removed alias mapping '" + alias + "'");
      saveMapping();
    }
    return false;
  }

  public boolean saveAliasMapping(Map<String, Object> values) throws IOException {
    String oldValue = (String) values.get("#oldValue");
    System.out.println(values);
    if (!StringUtils.isEmpty(oldValue) && aliasToRomMapping.containsKey(oldValue)) {
      aliasToRomMapping.remove(oldValue);
      LOG.info("Removed old alias mapping '" + oldValue + "'");
    }
    values.remove("#oldValue");


    Set<Map.Entry<String, Object>> entries = values.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String alias = entry.getKey();
      String romName = (String) entry.getValue();

      if (!aliasToRomMapping.containsValue(alias)) {
        aliasToRomMapping.put(alias.trim(), romName.trim());
      }
    }

    saveMapping();
    return true;
  }

  private void saveMapping() throws IOException {
    String mapAsString = aliasToRomMapping.keySet().stream().map(key -> key.trim() + "," + aliasToRomMapping.get(key).trim()).sorted().collect(Collectors.joining("\n"));
    File vpmAliasFile = systemService.getVPMAliasFile();
    FileUtils.writeStringToFile(vpmAliasFile, mapAsString, Charset.defaultCharset());
    loadAliasMapping();
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
              aliasToRomMapping.put(alias, originalRom);
            }
          }
        }
        LOG.info("Loaded " + aliasToRomMapping.size() + " alias mappings.");
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
