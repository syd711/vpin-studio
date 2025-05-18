package de.mephisto.vpin.server.mame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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

  public TextFile loadAliasFile(@NonNull GameEmulator emulator) {
    TextFile textFile = new TextFile();
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      if (vpmAliasFile.exists()) {
        textFile.setSize(vpmAliasFile.length());
        textFile.setPath(vpmAliasFile.getAbsolutePath());
        textFile.setLastModified(new Date(vpmAliasFile.lastModified()));
        textFile.setContent(FileUtils.readFileToString(vpmAliasFile, Charset.defaultCharset()));
      }
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return textFile;
  }

  public void saveAliasFile(@NonNull GameEmulator emulator, @NonNull String text) {
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      File backup = new File(vpmAliasFile.getParentFile(), vpmAliasFile.getName() + ".bak");
      if (!backup.exists()) {
        FileUtils.copyFile(vpmAliasFile, backup);
      }

      if (vpmAliasFile.exists() && vpmAliasFile.delete()) {
        text = text.replaceAll("\n", "\r\n");
        FileUtils.writeStringToFile(vpmAliasFile, text, Charset.defaultCharset());
        LOG.info("Written " + vpmAliasFile.getAbsolutePath());
      }
    } catch (IOException e) {
      LOG.error("Error saving " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  private Map<String, String> loadAliasMapping(@NonNull GameEmulator emulator) {
    Map<String, String> aliasToRomMapping = new HashMap<>();
    File vpmAliasFile = getVPMAliasFile(emulator);
    try {
      if (vpmAliasFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(vpmAliasFile);
        List<String> mappings = IOUtils.readLines(fileInputStream, "utf8");
        fileInputStream.close();

        for (String mapping : mappings) {
          if (mapping.contains(",")) {
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
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return aliasToRomMapping;
  }

  public boolean clearCache(List<GameEmulator> gameEmulators) {
    aliasNamToRom.clear();
    for (GameEmulator gameEmulator : gameEmulators) {
      if (gameEmulator.isVpxEmulator()) {
        aliasNamToRom.put(gameEmulator.getId(), loadAliasMapping(gameEmulator));
      }
    }
    LOG.info("Loaded Alias Mappings:");
    Set<Map.Entry<Integer, Map<String, String>>> entries = aliasNamToRom.entrySet();
    for (Map.Entry<Integer, Map<String, String>> entry : entries) {
      LOG.info("Alias Mappings for emulator " + entry.getKey() + ": " + entry.getValue().size());
    }
    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }
}
