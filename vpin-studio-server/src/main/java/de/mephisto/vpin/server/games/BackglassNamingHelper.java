package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.directb2s.BackglassFileType;
import de.mephisto.vpin.server.doflinx.B2SMapping;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

public class BackglassNamingHelper {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final List<B2SMapping> b2sFx3Mapping;
  private static final List<B2SMapping> b2sFxMapping;
  private static final List<B2SMapping> b2sMMapping;

  static {
    b2sFxMapping = readB2SMapping("pinball_fx_b2s_mapping.json");
    b2sFx3Mapping = readB2SMapping("pinball_fx3_b2s_mapping.json");
    b2sMMapping = readB2SMapping("pinball_m_b2s_mapping.json");
  }

  public static String getBackglassFileName(@NonNull Game game) {
    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    String fileName = baseName + BackglassFileType.DIRECTB2S.getSuffix();
    if (game.isZenGame()) {
      B2SMapping backglassName = findBackglassName(game);
      if (backglassName != null) {
        return backglassName.getDirectb2s();
      }
      return fileName;
    }

    // VPX 10.8.1 looks for the backglass named after the table file first, then named after the table's folder
    File gameFile = game.getGameFile();
    File folder = gameFile != null ? gameFile.getParentFile() : null;
    if (folder != null) {
      String existing = findExistingBackglassFileName(folder, baseName);
      if (existing != null) {
        return existing;
      }
      if (!folder.getName().isEmpty()) {
        existing = findExistingBackglassFileName(folder, folder.getName());
        if (existing != null) {
          return existing;
        }
      }
    }
    return fileName;
  }

  /**
   * Returns the name of the existing backglass file for the given base name, checking all backglass types by priority.
   */
  @Nullable
  public static String findExistingBackglassFileName(@NonNull File folder, @NonNull String baseName) {
    for (BackglassFileType type : BackglassFileType.values()) {
      String name = baseName + type.getSuffix();
      if (new File(folder, name).exists()) {
        return name;
      }
    }
    return null;
  }

  /**
   * Same as FileUtils.uniqueFile() but checks all backglass types, so that "Twister (1).B2SPro" is returned
   * when "Twister.directb2s" exists.
   */
  public static File uniqueBackglassFile(@NonNull File target) {
    File folder = target.getParentFile();
    String baseName = FilenameUtils.getBaseName(target.getName());
    String suffix = "." + FilenameUtils.getExtension(target.getName());
    String candidate = baseName;
    int index = 1;
    while (findExistingBackglassFileName(folder, candidate) != null) {
      candidate = baseName + " (" + index + ")";
      index++;
    }
    return new File(folder, candidate + suffix);
  }

  /**
   * Returns the backglass file of the other backglass type(s) that exist next to the given backglass file,
   * e.g. "Twister.directb2s" for "Twister.B2SPro".
   */
  public static List<File> getSiblingBackglassFiles(@NonNull File backglassFile) {
    List<File> result = new ArrayList<>();
    String baseName = FilenameUtils.getBaseName(backglassFile.getName());
    for (BackglassFileType type : BackglassFileType.values()) {
      File sibling = new File(backglassFile.getParentFile(), baseName + type.getSuffix());
      if (!sibling.getName().equalsIgnoreCase(backglassFile.getName()) && sibling.exists()) {
        result.add(sibling);
      }
    }
    return result;
  }

  public static File getBackglassFile(@NonNull Game game) {
    String fileName = getBackglassFileName(game);
    if (game.isZenGame() && game.getEmulator().getBackglassDirectory() != null) {
      return new File(game.getEmulator().getBackglassDirectory(), fileName);
    }
    return new File(game.getGameFile().getParentFile(), fileName);
  }

  @Nullable
  public static B2SMapping findBackglassName(Game game) {
    GameEmulator emulator = game.getEmulator();
    List<B2SMapping> mapping = null;
    switch (emulator.getType()) {
      case ZenFX3: {
        mapping = b2sFx3Mapping;
        break;
      }
      case ZenFX: {
        mapping = b2sFxMapping;
        break;
      }
      case PinballM: {
        mapping = b2sMMapping;
        break;
      }
    }

    if (mapping != null) {
      Optional<B2SMapping> first = mapping.stream().filter(m -> {
        boolean equals = m.getGameName().equals(game.getGameName());
        boolean contains = m.getGameDisplayName().contains(game.getGameDisplayName());
        return equals || contains;
      }).findFirst();
      return first.orElse(null);
    }
    return null;
  }

  public static List<B2SMapping> readB2SMapping(String s) {
    try {
      List<B2SMapping> list = Arrays.asList(JsonSettings.objectMapper.readValue(new File(RESOURCES, "pupgames/" + s), B2SMapping[].class));
      LOG.info("Loaded .directb2s mapping {}", s);
      return list;
    }
    catch (Exception e) {
      LOG.error("Failed to load b2s mapping: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }
}
