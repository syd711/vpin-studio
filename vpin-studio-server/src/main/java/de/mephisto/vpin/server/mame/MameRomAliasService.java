package de.mephisto.vpin.server.mame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MameRomAliasService {
  private final static Logger LOG = LoggerFactory.getLogger(MameRomAliasService.class);

  private final static String VPM_ALIAS = VPinFile.VPMAliasTxt.toString();

  @NonNull
  @JsonIgnore
  public File getVPMAliasFile(@NonNull GameEmulator emulator) {
    return new File(emulator.getMameFolder(), VPM_ALIAS);
  }

  public String getRomForAlias(@NonNull GameEmulator emulator, @Nullable String romAlias) {
    if (romAlias == null) {
      return null;
    }

    Map<String, String> aliasToRomMapping = loadAliasMapping(emulator);
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

  public boolean deleteAliasMapping(@NonNull GameEmulator emulator, @Nullable String alias) throws IOException {
    Map<String, String> aliasToRomMapping = loadAliasMapping(emulator);
    if (!StringUtils.isEmpty(alias) && aliasToRomMapping.containsKey(alias)) {
      aliasToRomMapping.remove(alias);
      LOG.info("Removed alias mapping '" + alias + "'");
//      saveMapping(emulator, aliasToRomMapping);
    }
    return false;
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
            String[] split = mapping.split(",");
            String[] aliases = Arrays.copyOfRange(split, 0, split.length - 1);
            String originalRom = split[split.length - 1];

            for (String alias : aliases) {
              aliasToRomMapping.put(alias, originalRom);
            }
          }
        }
//        LOG.info("Loaded " + aliasToRomMapping.size() + " alias mappings for " + emulator.getMameFolder().getAbsolutePath());
      }
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return aliasToRomMapping;
  }
}
