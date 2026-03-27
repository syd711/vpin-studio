package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.server.doflinx.B2SMapping;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
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
    String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + "." + AssetType.DIRECTB2S.name().toLowerCase();
    if (game.isZenGame()) {
      B2SMapping backglassName = findBackglassName(game);
      if (backglassName != null) {
        return backglassName.getDirectb2s();
      }
    }
    return fileName;
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
    catch (IOException e) {
      LOG.error("Failed to load b2s mapping: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }
}
