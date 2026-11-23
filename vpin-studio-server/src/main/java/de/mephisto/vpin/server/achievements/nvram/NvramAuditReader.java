package de.mephisto.vpin.server.achievements.nvram;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import net.nvrams.mapping.map.NVRamGameState;
import net.nvrams.mapping.map.NVRamMap;
import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.map.NVRamMapping;
import net.nvrams.mapping.map.SparseMemory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Reads the audits of a .nv file the way the Achievement Watcher sees them (labels from {@link MapFieldExtractor}),
 * but decodes the values with the java-pinmame-nvmaps library, which respects the platform memory layout.
 */
public class NvramAuditReader {
  private static final Logger LOG = LoggerFactory.getLogger(NvramAuditReader.class);

  private static final Set<String> TEXT_ENCODINGS = Set.of("ch", "wpc_rtc", "raw");

  private final ObjectMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
  private final File mapFolder;
  private final NVRamMapParser parser;
  private final RomMapResolver resolver;
  private final Map<String, List<MapField>> fieldCache = new ConcurrentHashMap<>();

  public NvramAuditReader(@NonNull File mapFolder) {
    this.mapFolder = mapFolder;
    this.parser = new NVRamMapParser(mapFolder.getAbsolutePath() + File.separator);
    Set<String> roms = parser.getSupportedRoms().stream().filter(r -> !r.startsWith("_")).collect(Collectors.toSet());
    this.resolver = new RomMapResolver(roms);
  }

  /**
   * @return the ROM whose map is used for the given ROM (exact or family fallback), null if not supported.
   */
  @Nullable
  public String resolveMapRom(@Nullable String rom) {
    return resolver.resolve(rom);
  }

  public boolean isSupported(@Nullable String rom) {
    return resolveMapRom(rom) != null;
  }

  /**
   * The fields of the map used for the ROM, in Watcher order.
   */
  @NonNull
  public List<MapField> getFields(@NonNull String rom) {
    String mapRom = resolveMapRom(rom);
    if (mapRom == null) {
      return Collections.emptyList();
    }
    return fieldCache.computeIfAbsent(mapRom, r -> {
      try {
        String mapPath = parser.mapPathForRom(r);
        if (mapPath == null) {
          return Collections.emptyList();
        }
        JsonNode json = mapper.readTree(new File(mapFolder, mapPath));
        return MapFieldExtractor.extract(json);
      }
      catch (Exception e) {
        LOG.error("Failed to read NVRAM map fields for {}: {}", r, e.getMessage());
        return Collections.emptyList();
      }
    });
  }

  /**
   * Reads all field values of the .nv file. Numbers are returned as {@link Long}, text fields as {@link String}.
   */
  @NonNull
  public NvramSnapshot read(@NonNull String rom, @Nullable File nvFile) {
    String mapRom = resolveMapRom(rom);
    if (mapRom == null || nvFile == null || !nvFile.exists()) {
      return NvramSnapshot.empty();
    }
    try {
      byte[] data = Files.readAllBytes(nvFile.toPath());
      return read(mapRom, data);
    }
    catch (IOException e) {
      LOG.error("Failed to read {}: {}", nvFile.getAbsolutePath(), e.getMessage());
      return NvramSnapshot.empty();
    }
  }

  @NonNull
  public NvramSnapshot read(@NonNull String mapRom, byte[] data) {
    try {
      NVRamMap map = parser.getMap(mapRom);
      if (map == null) {
        return NvramSnapshot.empty();
      }
      SparseMemory memory = parser.getMemory(map, data);
      Map<String, Object> values = new LinkedHashMap<>();
      for (MapField field : getFields(mapRom)) {
        Object value = decode(field.node(), memory);
        if (value != null) {
          values.put(field.label(), value);
        }
      }
      return new NvramSnapshot(values, readPlayerCount(map, memory));
    }
    catch (Exception e) {
      LOG.error("Failed to decode NVRAM for {}: {}", mapRom, e.getMessage(), e);
      return NvramSnapshot.empty();
    }
  }

  @Nullable
  private Object decode(@NonNull JsonNode node, @NonNull SparseMemory memory) {
    try {
      JsonNode n = node;
      String encoding = node.has("encoding") ? node.get("encoding").asString() : null;
      if (encoding == null) {
        // the Watcher decodes fields without encoding as unsigned integers
        ObjectNode copy = ((ObjectNode) node).deepCopy();
        copy.put("encoding", "int");
        n = copy;
        encoding = "int";
      }
      NVRamMapping mapping = mapper.treeToValue(n, NVRamMapping.class);
      if (TEXT_ENCODINGS.contains(encoding)) {
        String text = "ch".equals(encoding) ? mapping.getTextValue(memory) : mapping.formatEntry(memory, Locale.ENGLISH);
        return text != null ? text.trim() : null;
      }
      return mapping.getValue(memory);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  private Integer readPlayerCount(NVRamMap map, SparseMemory memory) {
    try {
      NVRamGameState gameState = map.getGameState();
      if (gameState != null && gameState.getPlayerCount() != null) {
        Long v = gameState.getPlayerCount().getValue(memory);
        return v != null ? v.intValue() : null;
      }
    }
    catch (Exception e) {
      //ignore
    }
    return null;
  }
}
