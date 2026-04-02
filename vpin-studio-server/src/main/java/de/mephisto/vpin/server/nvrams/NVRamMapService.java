package de.mephisto.vpin.server.nvrams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.server.nvrams.map.BcdUtils;
import de.mephisto.vpin.server.nvrams.map.ChecksumMapping;
import de.mephisto.vpin.server.nvrams.map.NVRamMap;
import de.mephisto.vpin.server.nvrams.map.NVRamMapping;
import de.mephisto.vpin.server.nvrams.map.NVRamMetadata;
import de.mephisto.vpin.server.nvrams.map.NVRamPlatform;
import de.mephisto.vpin.server.nvrams.map.NVRamRegion;
import de.mephisto.vpin.server.nvrams.map.NVRamScore;
import de.mephisto.vpin.server.nvrams.map.SparseMemory;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Main class for parsing PinMAME .nv files using NVRAM maps.
 * This is a port in java of https://github.com/tomlogic/py-pinmame-nvmaps/blob/main/nvram_parser.py 
 * 
 * How to use:
 * - create NVRamParser
 * - use getMap(nvfile, rom) to retrieve a NVRamMap definition that is the downloaded and parsed NVRAm Map
 * - use setNvram(mapJson, bytes) to retrieve a SparseMemory object that is the NV file parsed
 * - use tools like NVRamToolDump to dump the content of the NVRam
 * 
 * see NVRam Map structure
 * https://github.com/tomlogic/pinmame-nvram-maps/blob/main/README.md?plain=1
 */
@Service
public class NVRamMapService {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamMapService.class);

  // the root where map file can be downloaded
  public String mapRoot = "https://github.com/tomlogic/pinmame-nvram-maps/raw/refs/heads/main/";
  // An alternative to get files from a local folder. When set, it overrides mapRoot
  public File mapFolder = null;

  private Map<String, String> _cacheMapForRom;
  private Map<String, String> cacheRomNames;

  private Map<String, NVRamMap> cacheNVRamMap = new HashMap<>();
  private Map<String, NVRamPlatform> cachePlatform = new HashMap<>();

  /**
   * A service that uses head version of json maps
   */
  public NVRamMapService() {
  }

  /**
   * A service that uses a specific URL to download json maps
   */
  public NVRamMapService(String root) {
    this.mapRoot = root;
  }
  
  /**
   * A service that uses a local folder to access json maps
   */
  public NVRamMapService(File root) {
    this.mapFolder = root;
  }
  
  private void initMap(NVRamMap mapJson) throws IOException {
    NVRamMetadata metadata = mapJson.getMetadata();
    if (metadata == null) {
      throw new IllegalArgumentException("Unsupported map file format -- update to v0.6 or later");
    }

		NVRamPlatform platform = loadPlatform(mapJson.getMetadata());
    mapJson.setNVRamPlatform(platform);

    // checksums
    createChecksum(mapJson, mapJson.getChecksum8(), false);
    createChecksum(mapJson, mapJson.getChecksum16(), true);
  }    

  public NVRamPlatform loadPlatform(NVRamMetadata metadata) throws IOException {
    String platformName = metadata.getPlatform();
    NVRamPlatform platform;
    if (platformName != null) {
      platform = cachePlatform.get(platformName);
      if (platform == null) {
        String platformUrl = "platforms/" + platformName + ".json";
        platform = getStream(platformUrl, in -> {
          ObjectMapper objectMapper = new ObjectMapper();
          return objectMapper.readValue(in, NVRamPlatform.class);
        });
      }
      cachePlatform.put(platformName, platform);
    } 
    else {
		  platform = new NVRamPlatform();
      platform.setName("auto-generated");
      platform.setCpu("unknown");
      platform.setEndian(metadata.getEndian());

      Integer size = null;
      if (StringUtils.isNotEmpty(metadata.getRamSize())) {
        size = BcdUtils.toInt(metadata.getRamSize());
      }
      NVRamRegion region = NVRamRegion.createDefault("undefined", size);
      platform.addLayout(region);
    }
    return platform;
  }
   
  private void createChecksum(NVRamMap mapJson, List<NVRamMapping> checksums, boolean is16) {
    if (checksums != null) {
      for (NVRamMapping c : checksums) {
        int start = c.getStart();
        int end;
        if (c.getEnd() != null) {
          end = c.getEnd();
        } else {
          end = start + c.getLength() - 1;
        }
        int grouping = ObjectUtils.defaultIfNull(c.getGroupings(), end - start + 1);
        while (start <= end) {
          int entryEnd = start + grouping - 1;
          mapJson.addChecksumEntry(new ChecksumMapping(start, entryEnd, c.getChecksum(),
              c.getLabel(), is16, mapJson.isBigEndian()));
          start = entryEnd + 1;
        }
      }
    }
  }

  //----------------------------------------

  public SparseMemory getMemory(NVRamMap mapJson, byte[] nvData) {
    NVRamRegion nvramMem = mapJson.getMemoryArea(null, "nvram");
    int base = nvramMem != null ? nvramMem.getAddress() : 0;
    int length = ObjectUtils.defaultIfNull(nvramMem != null ? nvramMem.getSize() : null, nvData.length);
    if (length > nvData.length) length = nvData.length;

    SparseMemory memory = new SparseMemory();

    memory.updateMemory(base, Arrays.copyOf(nvData, length));
    if (length < nvData.length) {
      byte[] extra = new byte[nvData.length - length];
      System.arraycopy(nvData, length, extra, 0, extra.length);
      memory.setPinmameData(extra);
    }
    return memory;
  }

  public byte[] getDotNv(NVRamMap mapJson, SparseMemory memory) {
    NVRamRegion nvramArea = mapJson.getMemoryArea(null, "nvram");
    int address = BcdUtils.toInt(nvramArea.getAddress());
    SparseMemory.MemoryRegion nvramMem = memory.findRegion(address);

    byte[] dotNv = Arrays.copyOf(nvramMem.data, nvramMem.data.length);
    byte[] pinmameData = memory.getPinmameData();
    if (pinmameData != null) {
      byte[] combined = new byte[dotNv.length + pinmameData.length];
      System.arraycopy(dotNv, 0, combined, 0, dotNv.length);
      System.arraycopy(pinmameData, 0, combined, dotNv.length, pinmameData.length);
      return combined;
    }
    return dotNv;
  }

  public String lastPlayed(NVRamMap mapJson, SparseMemory memory, Locale locale) {
    NVRamMapping lp = mapJson.getLastPlayed();
    if (lp == null) return null;
    return lp.formatEntry(mapJson, memory, locale);
  }

  public List<String> highScores(NVRamMap mapJson, SparseMemory memory, boolean useShortLabels, Locale locale) {
    List<String> scores = new ArrayList<>();
    for (NVRamScore entry : mapJson.getHighScores()) {
      String score = entry.formatHighScore(mapJson, memory, locale);
      if (score != null) {
        scores.add(entry.formatLabel(useShortLabels) + ": " + score);
      }
    }
    return scores;
  }

  public int nvramBaseAddress(NVRamMap mapJson) {
    NVRamPlatform platform = mapJson.getRamPlatform();
    List<NVRamRegion> layout = platform.getMemoryLayout();
    for (NVRamRegion region : layout) {
      if ("nvram".equals(region.getType())) {
        return BcdUtils.toInt(region.getAddress());
      }
    }
    return 0;
  }
  //============================================ Downloaders ======

  public List<String> getSupportedNVRams() {
    try {
      ensureCacheMapForRom();
      return new ArrayList<>(_cacheMapForRom.keySet());
    }
    catch (IOException ioe) {
      LOG.error("Cannot get supported NVRams: {}", ioe.getMessage());
      return Collections.emptyList();
    }
  }

  private void ensureCacheMapForRom() throws IOException {
    if (_cacheMapForRom == null) {
      String indexUrl = "index.json";
      LOG.info("Load cache of rom map from {}", indexUrl);

      _cacheMapForRom  = getStream(indexUrl, in -> {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, new TypeReference<Map<String, String>>() {});
      });
      LOG.info("Rom Cache loaded with {} roms", _cacheMapForRom.size());
    }
  }

  public String romFromNv(File nvPath) throws IOException {
    String rom = nvPath.getName();
    int dotIndex = rom.lastIndexOf('.');
    if (dotIndex > 0) rom = rom.substring(0, dotIndex);
    int hyphenIndex = rom.indexOf('-');
    if (hyphenIndex > 0) rom = rom.substring(0, hyphenIndex);
    return rom;
  }


  public String mapPathForRom(String rom) throws IOException {
    ensureCacheMapForRom();
    return _cacheMapForRom.get(rom);
  }

  /**
   * A replacement method to getMap() that take a local map File and returns the NVramMap
   */
  public NVRamMap getLocalMap(File map, String rom) throws IOException {
    if (map.exists()) {
      ObjectMapper mapper = new ObjectMapper();
      try (FileInputStream in = new FileInputStream(map)) {
        NVRamMap mapJson = mapper.readValue(in, NVRamMap.class);

        // initiate mappings
        if (mapJson != null) {
          initMap(mapJson);

          String romname = romName(rom);
          mapJson.setRom(rom, romname);
          mapJson.setMapPath(map.getAbsolutePath());
          return mapJson;
        }
      }
    }
    // else not found
    return null;
  }

  /**
   * Download the map of the given rom
   */
 public NVRamMap getMap(@NonNull String rom) throws IOException {
    NVRamMap mapJson = cacheNVRamMap.get(rom);
    if (mapJson != null) {
      return mapJson;
    }
    String mapPath = mapPathForRom(rom);
    if (mapPath != null) {
      mapJson= _getMapFromPath(mapPath);
      String romname = romName(rom);
      mapJson.setRom(rom, romname);
      mapJson.setMapPath(mapPath);

      cacheNVRamMap.put(rom, mapJson);
      return mapJson;
    } 
    else {
      LOG.warn("Couldn't find a map for rom {}", rom);
      return null;
    }
  }

  public NVRamMap _getMapFromPath(String mapPath) throws IOException {
    NVRamMap mapJson =  getStream(mapPath, in -> {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(in, NVRamMap.class);
    });

    // initiate mappings
    if (mapJson != null) {
      initMap(mapJson);
    }

    return mapJson;
}

	/**
	 * Take a rom and return its name from romnames.json
	 */
  public String romName(String rom) {
    if (cacheRomNames == null) {
      try {
        String romnames = "romnames.json";
        cacheRomNames = getStream(romnames, in -> {
          ObjectMapper mapper = new ObjectMapper();
          return mapper.readValue(in, new TypeReference<Map<String, String>>() {});
        });
      }
      catch (IOException ioe) {
        return rom;
      }
    }
    return cacheRomNames.getOrDefault(rom, "(Unknown ROM " + rom + ")");
  }

  private <T> T getStream(String u, ProcessStream<T> consumer) throws IOException {
    if (mapFolder != null) {
      try (FileInputStream in = new FileInputStream(new File(mapFolder, u))) {
        return consumer.process(in);
      }
    }
    else {
      return download(mapRoot + u, consumer);
    }
  }

  public <T> T download(String u, ProcessStream<T> consumer) throws IOException {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(u);
      connection = (HttpURLConnection) url.openConnection();
      int code = connection.getResponseCode();
      if (code==200) {
        InputStream in = url.openStream();
        return consumer.process(in);
      }
      return null;
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @FunctionalInterface
  public static interface ProcessStream<T> {
    public T process(InputStream in) throws IOException;
  }

}
