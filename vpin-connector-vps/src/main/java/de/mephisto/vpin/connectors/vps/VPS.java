package de.mephisto.vpin.connectors.vps;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.connectors.vps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class VPS {
  private final static Logger LOG = LoggerFactory.getLogger(VPS.class);

  public final static String URL = "https://virtualpinballspreadsheet.github.io/vps-db/db/vpsdb.json";
  public final static String BASE_URL = "https://virtualpinballspreadsheet.github.io";

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private List<VpsSheetChangedListener> listeners = new ArrayList<>();

  private final List<String> versionIndicators = Arrays.asList("vpw", "bigus", "salas", "tasty", "thalamus", "VPinWorkshop", "Paulie");

  private List<VpsTable> tables;

  private static VPS instance;

  public void addChangeListener(VpsSheetChangedListener listener) {
    this.listeners.add(listener);
  }

  public static VPS loadInstance(InputStream in) {
    VPS instance = new VPS();
    try {
      instance.tables = loadTables(in);
      return instance;
    } catch (Exception e) {
      LOG.error("Failed to load VPS stream: " + e.getMessage(), e);
    }
    return null;
  }

  public static String getVpsTableUrl(String tableId, String versionId) {
    String url = getVpsTableUrl(tableId);
    if (versionId != null) {
      url += "#" + versionId;
    }
    return url;
  }

  public static String getVpsTableUrl(String tableId) {
    return BASE_URL + "/?game=" + tableId + "&fileType=table";
  }

  public static VPS getInstance() {
    if (instance == null) {
      instance = new VPS();
    }
    return instance;
  }

  public VPS() {
    if (!this.getVpsDbFile().exists()) {
      try {
        if (this.getVpsDbFile().getParentFile().exists()) {
          this.getVpsDbFile().getParentFile().mkdirs();
        }
        update();
      } catch (Exception e) {
        LOG.error("Failed to initialize VPS db: " + e.getMessage(), e);
      }
    }
    this.tables = loadTables(null);
  }

  public VpsTable getTableById(String id) {
    return this.tables.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  public VpsTableVersion getTableVersionById(VpsTable table, String id) {
    List<VpsTableVersion> tableFiles = table.getTableFiles();
    return tableFiles.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
  }

  public List<VpsTable> getTables() {
    return tables;
  }

  /* moved to TableMatcher
  public VpsTableVersion findVersion(VpsTable table, String tableFileName, String tableName, String version) {
    List<VpsTableVersion> tableFiles = table.getTableFiles();
    if (tableFiles.size() == 1) {
      return tableFiles.get(0);
    }

    for (VpsTableVersion tableVersion : tableFiles) {
      if (tableVersion.getTableFormat() != null && tableVersion.getTableFormat().equalsIgnoreCase("FP")) {
        continue;
      }

      if (version != null && tableVersion.toString().toLowerCase().contains(version.toLowerCase())) {
        return tableVersion;
      }

      String versionString = tableVersion.toString();
      for (String versionIndicator : versionIndicators) {
        if (versionMatches(versionString, tableFileName, tableName, versionIndicator)) {
          return tableVersion;
        }
      }
    }

    for (VpsTableVersion tableVersion : tableFiles) {
      if (tableVersion.getTableFormat() == null || !tableVersion.getTableFormat().equalsIgnoreCase("FP")) {
        return tableVersion;
      }
    }

    if (!tableFiles.isEmpty()) {
      return tableFiles.get(0);
    }
    return null;
  }

  private boolean versionMatches(String version, String tableFileName, String tableName, String term) {
    if (version.toLowerCase().contains(term.toLowerCase())) {
      if (tableFileName.toLowerCase().contains(term) || tableName.toLowerCase().contains(term)) {
        return true;
      }
    }
    return false;
  }
  */

  public List<VpsTable> find(String searchTerm) {
    return find(searchTerm, null);
  }

  public List<VpsTable> find(String searchTerm, String rom) {
    String term = searchTerm;
    term = term.replaceAll("_", " ");
    term = term.replaceAll("'", " ");
    term = term.replaceAll("-", " ");
    term = term.replaceAll("\\.", " ");
    term = term.replaceAll("The ", "");
    term = term.replaceAll(", The", "");
    if (term.contains("(")) {
      term = term.substring(0, term.indexOf("("));
    }
    term = term.toLowerCase().trim();

    List<VpsTable> results = findInternal(term, rom);

    while (results.isEmpty()) {
      if (term.contains(" ")) {
        term = term.substring(0, term.lastIndexOf(" "));
      }
      else {
        break;
      }
      results = findInternal(term, rom);
    }

    return results;
  }

  private List<VpsTable> findInternal(String term, String rom) {
    List<VpsTable> results = new ArrayList<>();
    for (VpsTable table : this.tables) {
      List<VpsAuthoredUrls> romFiles = table.getRomFiles();
      if (romFiles != null) {
        for (VpsAuthoredUrls romFile : romFiles) {
          if (romFile.getVersion() != null && romFile.getVersion().equalsIgnoreCase(rom)) {
            results.add(table);
            return results;
          }
        }
      }

      String name = table.getName().toLowerCase();
      name = name.replaceAll("-", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("'", " ");
      name = name.replaceAll("&", "and");
      if (!name.contains(term)) {
        continue;
      }
      results.add(table);
    }

    return results;
  }

  public static File getVpsDbFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "vpsdb.json");
  }

  private static List<VpsTable> loadTables(InputStream in) {
    try {
      if (in == null) {
        in = new FileInputStream(getVpsDbFile());
      }

      VpsTable[] vpsTables = objectMapper.readValue(in, VpsTable[].class);
      return Arrays.stream(vpsTables)
        .filter(t -> t.getFeatures() == null || t.getFeatures().isEmpty() || t.getFeatures().contains(VpsFeatures.VPX) || !t.getFeatures().contains(VpsFeatures.FP))
        .collect(Collectors.toList());
    } catch (Exception e) {
      LOG.error("Failed to load VPS json: " + e.getMessage(), e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        //ignore
      }
    }
    return Collections.emptyList();
  }

  public List<VpsDiffer> update() {
    try {
      LOG.info("Downloading " + VPS.URL);
      java.net.URL url = new URL(VPS.URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = new File(getVpsDbFile().getParentFile(), getVpsDbFile().getName() + ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete existing tmp file vpsdb.json.tmp");
      }
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();

      long oldSize = 0;
      if (getVpsDbFile().exists()) {
        oldSize = getVpsDbFile().length();
      }

      if (getVpsDbFile().exists() && !getVpsDbFile().delete()) {
        LOG.error("Failed to delete vpsdb.json");
      }

      if (!tmp.renameTo(getVpsDbFile())) {
        LOG.error("Failed to rename vpsdb.json");
        return Collections.emptyList();
      }

      LOG.info("Written " + getVpsDbFile().getAbsolutePath() + ", (" + oldSize + " vs " + getVpsDbFile().length() + " bytes)");

      VPS newInstance = new VPS();
      List<VpsDiffer> diffs = this.diff(this.tables, newInstance.getTables());
      if (!diffs.isEmpty()) {
        LOG.info("VPS download detected " + diffs.size() + " changes, notifying " + listeners.size() + " listeners...");
        for (VpsSheetChangedListener listener : listeners) {
          listener.vpsSheetChanged(diffs);
          LOG.info("Notified VPS change listener \"" + listener.getClass().getName() + "\"");
        }
      }
      else {
        LOG.info("VPS had no changes, skipped update listeners.");
      }

      this.reload();
      return diffs;
    } catch (IOException e) {
      LOG.error("VPS download failed: " + e.getMessage());
    }
    return Collections.emptyList();
  }

  public Date getChangeDate() {
    return new Date(getVpsDbFile().lastModified());
  }

  public void reload() {
    this.tables = loadTables(null);
  }

  public VpsDiffer diffById(List<VpsTable> oldTables, List<VpsTable> newTables, String id) {
    Optional<VpsTable> oldTable = oldTables.stream().filter(t -> t.getId().equals(id)).findFirst();
    Optional<VpsTable> newTable = newTables.stream().filter(t -> t.getId().equals(id)).findFirst();

    return new VpsDiffer(newTable.orElse(null), oldTable.orElse(null));
  }

  public List<VpsDiffer> diff(List<VpsTable> oldTables, List<VpsTable> newTables) {
    if (oldTables == null || newTables == null) {
      LOG.info("Skipping VPS diff, because lists are empty.");
      return Collections.emptyList();
    }

    LOG.info("Differing " + oldTables.size() + " old tables against " + newTables.size() + " new tables.");
    List<VpsDiffer> diff = new ArrayList<>();
    for (VpsTable newTable : newTables) {
      Optional<VpsTable> oldTable = oldTables.stream().filter(t -> t.getId().equalsIgnoreCase(newTable.getId())).findFirst();
      VpsDiffer tableDiff = new VpsDiffer(newTable, oldTable.orElse(null));
      VPSChanges changes = tableDiff.getChanges();
      if (!changes.isEmpty()) {
        LOG.info("Updates for \"" + newTable.getDisplayName() + "\": " + changes.getChanges().stream().map(c -> c.getDiffType().toString()).collect(Collectors.joining(", ")));
        diff.add(tableDiff);
      }
    }

    Collections.sort(diff, Comparator.comparing(VpsDiffer::getDisplayName));
    return diff;
  }
}
