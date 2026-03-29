package de.mephisto.vpin.server.nvrams.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.server.nvrams.parser.NVRamParser;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for parsing PinMAME .nv files using NVRAM maps.
 * This is a port in java of https://github.com/tomlogic/py-pinmame-nvmaps/blob/main/nvram_parser.py 
 * 
 * How to use:
 * - create NVRamParser
 * - use getMap(nvfile, rom) to retrieve a NVRamMap definition that is the downloaded and parsed NVRAm Map
 * - use setNvram(mapJson, bytes) to retrieve a SparseMemory object that is the NV file parsed
 * - use NVRamToolDump to dump the content of the NVRam
 * 
 * see NVRam Map structure
 * https://github.com/tomlogic/pinmame-nvram-maps/blob/main/README.md?plain=1
 */
public class NVRamParser {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamParser.class);

  public String mapRoot = "https://github.com/tomlogic/pinmame-nvram-maps/raw/refs/heads/main/";
  public String platformRoot = "c:/temp/_NVRAMS/";

  private Map<String, String> cacheMapForRom;
  private Map<String, String> cacheMapForPlatform;
  private Map<String, String> cacheRomNames;

  private Map<String, NVRamMap> cacheNVRamMap = new HashMap<>();
  private Map<String, NVRamPlatform> cachePlatform = new HashMap<>();

  /**
   * A parser that uses head version of json maps
   */
  public NVRamParser() {
  }

  /**
   * A parser that uses a specific URL to download json maps
   */
  public NVRamParser(String root) {
    this.mapRoot = root;
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
        String platformUrl = mapRoot + "platforms/" + platformName + ".json";
        platform = download(platformUrl, in -> {
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

  public SparseMemory setNvram(NVRamMap mapJson, byte[] nvData) {
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
      return new ArrayList<>(cacheMapForRom.keySet());
    }
    catch (IOException ioe) {
      LOG.error("Cannot get supported NVRams: {}", ioe.getMessage());
      return Collections.emptyList();
    }
  }

  private void ensureCacheMapForRom() throws IOException {
    if (cacheMapForRom == null) {
      String indexUrl = mapRoot + "index.json";
      LOG.info("Load cache of rom map from {}", indexUrl);

      cacheMapForRom  = download(indexUrl, in -> {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, new TypeReference<Map<String, String>>() {});
      });
      LOG.info("Rom Cache loaded with {} roms", cacheMapForRom.size());
    }
  }

  private void ensureCacheMapForPlatform() throws IOException {
    if (cacheMapForPlatform == null) {
      String indexUrl = platformRoot + "platforms.json";
      LOG.info("Load cache of platform map from {}", indexUrl);

      //TODO move to real donwload
      //cacheMapForPlatform  = download(indexUrl, in -> {
      cacheMapForPlatform  = process(new File(indexUrl), in -> {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, new TypeReference<Map<String, String>>() {});
      });
      LOG.info("Platform Cache loaded with {} platforms", cacheMapForPlatform.size());
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
    return cacheMapForRom.get(rom);
  }

  public String mapPathForPlatform(String platformname) throws IOException {
    ensureCacheMapForPlatform();
    return cacheMapForPlatform.entrySet().stream()
        .filter(e -> StringUtils.equalsIgnoreCase(e.getKey(), platformname))
        .map(e -> e.getValue())
        .findFirst().orElse(null);
  }

  public NVRamMap getLocalMap(String rom) throws IOException {
    File map = new File(NVRamToolMapGenerator.DECODED_ROOT, rom + ".map.json");
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
      mapJson= getMapFromPath(mapPath);
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

  public NVRamMap getMapFromPath(String mapPath) throws IOException {
    String pathUrl = mapRoot + mapPath;
    NVRamMap mapJson =  download(pathUrl, in -> {
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
        String romnames = mapRoot + "romnames.json";
        cacheRomNames = download(romnames, in -> {
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

  public <T> T process(File f, ProcessStream<T> consumer) throws IOException {
    if (f != null && f .exists()) {
      try (FileInputStream in = new FileInputStream(f)) {
        return consumer.process(in);
      }
    }
    return null;
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

	//==============================================================================
  // ---- main entry point ----

	public static void main(String[] args) throws IOException {
  
		File testFolder = new File("./testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
		File nvramFile = new File(testFolder, "afm_113b.nv");

		String[] theargs = new String[] { "--dump", "--nvram", nvramFile.getAbsolutePath() };
		_main(theargs);
	}

  public static void _main(String[] args) throws IOException {
    String nvramPath = null;
    String rom = null;
    boolean dump = false;

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--nvram": nvramPath = args[++i]; break;
        case "--rom": rom = args[++i]; break;
        case "--dump": dump = true; break;
      }
    }

    if (!dump || nvramPath == null) {
      System.out.println("Usage: ParseNVRAM --dump --nvram <file.nv> [--rom <romname>]");
      return;
    }
    if (!nvramPath.contains(".nv")) {
      System.out.println("Usage: ParseNVRAM --dump --nvram <file.nv>");
      return;
    }

    File nvramFile = new File(nvramPath);
    if (!nvramFile.exists()) {
      System.out.println("nvram is not present: " + nvramFile.getAbsolutePath());
      return;
    }

    byte[] nvram = Files.readAllBytes(nvramFile.toPath());

    Locale locale = Locale.getDefault();

    NVRamParser p = new NVRamParser();

    if (rom == null) {
      rom = p.romFromNv(nvramFile);
    }
    NVRamMap mapJson = p.getMap(rom);
    SparseMemory memory = p.setNvram(mapJson, nvram);

    NVRamToolDump tool = new NVRamToolDump();
    String txt = tool.dump(mapJson, memory, locale, true);
		System.out.println(txt);

		System.out.println("-----------------");
		System.out.println("last Played = " + p.lastPlayed(mapJson, memory, locale));
  }
}
