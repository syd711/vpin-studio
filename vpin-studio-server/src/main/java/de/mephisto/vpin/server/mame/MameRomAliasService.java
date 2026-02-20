package de.mephisto.vpin.server.mame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameCachingService;
import de.mephisto.vpin.server.games.GameEmulator;
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

@Service
public class MameRomAliasService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(MameRomAliasService.class);

  private final static String VPM_ALIAS = VPinFile.VPMAliasTxt.toString();

  private final Map<Integer, Map<String, String>> aliasNamToRom = new HashMap<>();

  private GameCachingService gameCachingService;

  @Autowired
  private EmulatorService emulatorService;

  @NonNull
  @JsonIgnore
  public File getVPMAliasFile(@NonNull GameEmulator emulator) {
    return new File(emulator.getMameFolder(), VPM_ALIAS);
  }

  @Nullable
  public String getRomForAlias(@NonNull GameEmulator emulator, @Nullable String romAlias) {
    if (romAlias == null) {
      return null;
    }

    Map<String, String> aliasToRomMapping = aliasNamToRom.get(emulator.getId());
    if (aliasToRomMapping != null) {
      Set<Map.Entry<String, String>> entries = aliasToRomMapping.entrySet();
      for (Map.Entry<String, String> entry : entries) {
        String alias = entry.getKey();
        String romName = entry.getValue();

        if (alias.trim().equals(romAlias.trim())) {
          return romName.trim();
        }
      }
    }

    return null;
  }


  public boolean deleteAlias(GameEmulator gameEmulator, String romAlias) {
    if (!StringUtils.isEmpty(romAlias)) {
      Map<String, String> mapping = loadAliasMapping(gameEmulator, romAlias);
      saveAliasFile(gameEmulator, mapping);
      clearCache(gameEmulator);
      LOG.info("Removed alias entry {}", romAlias);
    }
    return true;
  }

  public MonitoredTextFile loadAliasFile(@NonNull GameEmulator emulator) {
    MonitoredTextFile monitoredTextFile = new MonitoredTextFile();
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      if (vpmAliasFile.exists()) {
        monitoredTextFile.setSize(vpmAliasFile.length());
        monitoredTextFile.setPath(vpmAliasFile.getAbsolutePath());
        monitoredTextFile.setLastModified(new Date(vpmAliasFile.lastModified()));
        monitoredTextFile.setContent(FileUtils.readFileToString(vpmAliasFile, Charset.defaultCharset()));
      }
    }
    catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return monitoredTextFile;
  }

  public void saveAliasFile(@NonNull GameEmulator emulator, @NonNull Map<String, String> mapping) {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, String> set : mapping.entrySet()) {
      builder.append(set.getKey().trim());
      builder.append(",");
      builder.append(set.getValue().trim());
      builder.append("\n");
    }
    saveAliasFile(emulator, builder.toString());
  }

  public void saveAliasFile(@NonNull GameEmulator emulator, @NonNull String text) {
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      File backup = new File(vpmAliasFile.getParentFile(), vpmAliasFile.getName() + ".bak");
      if (backup.exists() && backup.delete()) {
        FileUtils.copyFile(vpmAliasFile, backup);
      }

      if (vpmAliasFile.exists() && vpmAliasFile.delete()) {
        text = text.replaceAll("\n", "\r\n");
        FileUtils.writeStringToFile(vpmAliasFile, text, Charset.defaultCharset());
        LOG.info("Written alias file " + vpmAliasFile.getAbsolutePath());

        clearCache(emulatorService.getVpxGameEmulators());
        invalidateAliasMappings();
      }
    }
    catch (IOException e) {
      LOG.error("Error saving " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  private void invalidateAliasMappings() {
    for (Map.Entry<Integer, Map<String, String>> entry : aliasNamToRom.entrySet()) {
      int emulatorId = entry.getKey();
      Map<String, String> mapping = entry.getValue();
      for (String s : mapping.keySet()) {
        gameCachingService.invalidateByRom(emulatorId, s);
      }
      for (String s : mapping.values()) {
        gameCachingService.invalidateByRom(emulatorId, s);
      }
    }
  }

  private Map<String, String> loadAliasMapping(@NonNull GameEmulator emulator, @Nullable String skipEntry) {
    Map<String, String> aliasToRomMapping = new HashMap<>();
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      if (vpmAliasFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(vpmAliasFile);
        List<String> mappings = IOUtils.readLines(fileInputStream, "utf8");
        fileInputStream.close();

        for (String mapping : mappings) {
          if (mapping.contains(",")) {
            if (skipEntry != null && mapping.contains(skipEntry)) {
              continue;
            }

            //the format is <Alias_Name>,<Real_ROM_Name>
            String[] split = mapping.split(",");
            String[] aliases = Arrays.copyOfRange(split, 0, split.length - 1);
            String originalRom = split[split.length - 1];
            for (String alias : aliases) {
              aliasToRomMapping.put(alias, originalRom);
            }
          }
        }
      }
    }
    catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return aliasToRomMapping;
  }

  public boolean clearCache(List<GameEmulator> gameEmulators) {
    for (GameEmulator gameEmulator : gameEmulators) {
      clearCache(gameEmulator);
    }
    return true;
  }

  private void clearCache(GameEmulator gameEmulator) {
    aliasNamToRom.remove(gameEmulator.getId());
    if (gameEmulator.isVpxEmulator()) {
      aliasNamToRom.put(gameEmulator.getId(), loadAliasMapping(gameEmulator, null));
    }
//    LOG.info("Loaded Alias Mappings:");
    Set<Map.Entry<Integer, Map<String, String>>> entries = aliasNamToRom.entrySet();
    for (Map.Entry<Integer, Map<String, String>> entry : entries) {
//      LOG.info("Alias Mappings for emulator " + entry.getKey() + ": " + entry.getValue().size());
    }
  }

  public void writeAlias(@NonNull GameEmulator gameEmulator, String rom, String alias) {
    if (!StringUtils.isEmpty(rom) && !StringUtils.isEmpty(alias)) {
      Map<String, String> mapping = loadAliasMapping(gameEmulator, null);
      mapping.put(alias, rom);
      saveAliasFile(gameEmulator, mapping);
      LOG.info("Written alias mapping {},{}", alias, rom);
    }
  }

  public void setGameCachingService(GameCachingService gameCachingService) {
    this.gameCachingService = gameCachingService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }
}
